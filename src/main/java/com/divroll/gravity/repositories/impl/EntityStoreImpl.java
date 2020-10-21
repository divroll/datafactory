/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2020, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.divroll.gravity.repositories.impl;

import com.divroll.gravity.Constants;
import com.divroll.gravity.properties.EmbeddedArrayIterable;
import com.divroll.gravity.properties.EmbeddedEntityIterable;
import com.divroll.gravity.Marshaller;
import com.divroll.gravity.actions.BlobRemoveAction;
import com.divroll.gravity.actions.BlobRenameAction;
import com.divroll.gravity.actions.BlobRenameRegexAction;
import com.divroll.gravity.actions.LinkAction;
import com.divroll.gravity.actions.LinkRemoveAction;
import com.divroll.gravity.actions.OppositeLinkAction;
import com.divroll.gravity.actions.OppositeLinkRemoveAction;
import com.divroll.gravity.actions.PropertyCopyAction;
import com.divroll.gravity.actions.PropertyIndexAction;
import com.divroll.gravity.actions.PropertyRemoveAction;
import com.divroll.gravity.actions.PropertyRenameAction;
import com.divroll.gravity.builders.BlobQuery;
import com.divroll.gravity.builders.LinkQuery;
import com.divroll.gravity.builders.RemoteEntities;
import com.divroll.gravity.builders.RemoteEntitiesBuilder;
import com.divroll.gravity.builders.RemoteEntity;
import com.divroll.gravity.builders.RemoteEntityProperty;
import com.divroll.gravity.builders.RemoteEntityQuery;
import com.divroll.gravity.builders.RemoteEntityUpdate;
import com.divroll.gravity.builders.RemoteTransactionFilter;
import com.divroll.gravity.repositories.EntityStore;
import com.divroll.gravity.database.DatabaseManager;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.collect.FluentIterable;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import io.vavr.control.Try;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentStoreTransaction;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.boon.Lists;
import org.jetbrains.annotations.NotNull;
import util.ComparableHashMap;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EntityStoreImpl extends BaseRespositoryImpl implements EntityStore {

  private static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);

  private DatabaseManager manager;

  public EntityStoreImpl(DatabaseManager databaseManager)
      throws NotBoundException, RemoteException {
    this.manager = databaseManager;
  }

  @Override public Optional<RemoteEntity> saveEntity(@NotNull RemoteEntity entity)
      throws NotBoundException, RemoteException {
    RemoteEntities remoteEntities = saveEntities(new RemoteEntity[] {entity}).get();
    return remoteEntities.entities().stream().findFirst();
  }

  @Override public Optional<RemoteEntities> saveEntities(@NotNull RemoteEntity[] entities)
      throws NotBoundException, RemoteException {
    Map<String, List<RemoteEntity>> envIdOrderedEntities = sort(entities);
    Iterator<String> it = envIdOrderedEntities.keySet().iterator();
    AtomicReference<RemoteEntities> finalResult = new AtomicReference<>(null);
    while (it.hasNext()) {
      String dir = it.next();
      List<RemoteEntity> remoteEntityList = envIdOrderedEntities.get(dir);
      manager.transactPersistentEntityStore(dir, false, txn -> {
        List<RemoteEntity> resultEntities = new ArrayList<>();
        remoteEntityList.forEach(entity -> {

          /**
           * Make a referenced scoped entities based on the {@code namespace}
           */

          final AtomicReference<EntityIterable> reference = new AtomicReference<>();
          if (entity.nameSpace() != null) {
            reference.set(
                txn.getAll(entity.entityType())
                    .intersect(
                        txn.find(entity.entityType(), Constants.NAMESPACE_PROPERTY,
                            entity.nameSpace())));
          } else {
            reference.set(txn.getAll(entity.entityType()));
          }

          Entity entityInContext = entity.entityId() != null ?
              txn.getEntity(txn.toEntityId(entity.entityId())) : txn.newEntity(entity.entityType());
          if (reference.get().indexOf(entityInContext) == -1 && entity.nameSpace() != null) {
            throw new IllegalArgumentException(
                "Entity " + entity.entityId() + " not found in namespace " + entity.nameSpace());
          }

          /**
           * Filter context
           */
          reference.set(processFilter(txn, reference, entity.filters(), entity.entityType()));

          /**
           * Process entity actions
           */

          if (entity.entityActions() != null) {
            Lists.list(entity.entityActions()).forEach(
                action -> {
                  if (action instanceof LinkAction) {
                    LinkAction linkAction = (LinkAction) action;
                    String targetId = linkAction.otherEntityId();
                    String linkName = linkAction.linkName();
                    Boolean isSet = linkAction.isSet();

                    if (targetId != null && linkName != null) {

                      EntityId targetEntityId = txn.toEntityId(targetId);
                      Entity targetEntity = txn.getEntity(targetEntityId);

                      if (isSet) {
                        Entity otherEntity = entityInContext.getLink(linkName);
                        if (otherEntity != null) {
                          otherEntity.deleteLink(linkName, entityInContext);
                        }
                        entityInContext.deleteLink(linkName, targetEntity);
                        entityInContext.setLink(linkName, targetEntity);
                      } else {
                        entityInContext.addLink(linkName, targetEntity);
                      }
                      //targetEntity.setProperty(Constants.RESERVED_FIELD_DATE_UPDATED, getISODate());
                    }
                  } else if (action instanceof OppositeLinkAction) {
                    OppositeLinkAction oppositeLinkAction = (OppositeLinkAction) action;
                    String linkName = oppositeLinkAction.linkName();
                    String sourceId = oppositeLinkAction.oppositeEntityId();
                    String oppositeLinkName = oppositeLinkAction.oppositeLinkName();
                    Boolean isSet = oppositeLinkAction.isSet();

                    if (sourceId != null && linkName != null && oppositeLinkName != null) {
                      EntityId sourceEntityId = txn.toEntityId(sourceId);
                      Entity sourceEntity = txn.getEntity(sourceEntityId);
                      if (isSet) {
                        Entity otherEntity = sourceEntity.getLink(oppositeLinkName);
                        if (otherEntity != null) {
                          otherEntity.deleteLink(linkName, sourceEntity);
                        }
                        sourceEntity.deleteLink(oppositeLinkName, otherEntity);
                        sourceEntity.setLink(oppositeLinkName, entityInContext);
                        entityInContext.setLink(linkName, sourceEntity);
                      } else {
                        sourceEntity.addLink(oppositeLinkName, entityInContext);
                        entityInContext.addLink(linkName, sourceEntity);
                      }
                    }
                  } else if (action instanceof LinkRemoveAction) {

                    LinkRemoveAction linkRemoveAction = (LinkRemoveAction) action;
                    String targetId = linkRemoveAction.otherEntityId();
                    String linkName = linkRemoveAction.linkName();

                    Entity sourceEntity = txn.getEntity(txn.toEntityId(entity.entityId()));
                    EntityId targetEntityId = txn.toEntityId(targetId);
                    Entity targetEntity = txn.getEntity(targetEntityId);
                    sourceEntity.deleteLink(linkName, targetEntity);
                  } else if (action instanceof OppositeLinkRemoveAction) {
                    OppositeLinkRemoveAction removeAction = (OppositeLinkRemoveAction) action;
                    String linkName = removeAction.linkName();
                    String oppositeEntityType = removeAction.oppositeEntityType();
                    String oppositeLinkName = removeAction.oppositeLinkName();
                    entityInContext.getLinks(linkName)
                        .intersect(txn.findWithLinks(entityInContext.getType(), linkName,
                            oppositeEntityType, oppositeLinkName))
                        .forEach(linkedEntities -> {
                          linkedEntities.delete();
                        });
                  } else if (action instanceof BlobRenameAction) {
                    BlobRenameAction blobRenameAction = (BlobRenameAction) action;
                    String blobName = blobRenameAction.blobName();
                    String newBlobName = blobRenameAction.newBlobName();
                    InputStream oldBlob = entityInContext.getBlob(blobName);
                    entityInContext.deleteBlob(blobName);
                    entityInContext.setBlob(newBlobName, oldBlob);
                  } else if (action instanceof BlobRenameRegexAction) {
                    BlobRenameRegexAction blobRenameRegexAction = (BlobRenameRegexAction) action;
                    String replacement = blobRenameRegexAction.replacement();
                    String regexPattern = blobRenameRegexAction.regexPattern();
                    entityInContext.getBlobNames().forEach(blobName -> {
                      InputStream blobStream = entityInContext.getBlob(blobName);
                      entityInContext.deleteBlob(blobName);
                      blobName = blobName.replaceAll(regexPattern, replacement);
                      entityInContext.setBlob(blobName, blobStream);
                    });
                  } else if (action instanceof BlobRemoveAction) {
                    BlobRemoveAction blobRemoveAction = (BlobRemoveAction) action;
                    blobRemoveAction.blobNames().forEach(blobName -> {
                      entityInContext.deleteBlob(blobName);
                    });
                  } else if (action instanceof PropertyCopyAction) {
                    PropertyCopyAction propertyCopyAction = (PropertyCopyAction) action;
                    String copyProperty = propertyCopyAction.propertyName();
                    Boolean copyFirst = propertyCopyAction.first();
                    if (copyFirst) {
                      Entity first =
                          reference.get().intersect(txn.getAll(entity.entityType())).getFirst();
                      if (first != null) {
                        entityInContext.setProperty(copyProperty, first.getProperty(copyProperty));
                      }
                    } else {
                      Entity last =
                          reference.get().intersect(txn.getAll(entity.entityType())).getLast();
                      if (last != null) {
                        entityInContext.setProperty(copyProperty, last.getProperty(copyProperty));
                      }
                    }
                  } else if (action instanceof PropertyIndexAction) {
                    PropertyIndexAction propertyIndexAction = (PropertyIndexAction) action;
                    String propertyName = propertyIndexAction.propertyName();
                    Comparable propertyValue = entity.propertyMap().get(propertyName);
                    if (!txn.find(entity.entityType(), propertyName, propertyValue).isEmpty()) {
                      throw new IllegalArgumentException("Unique property violation");
                    }
                  } else if (action instanceof PropertyRemoveAction) {
                    PropertyRemoveAction propertyRemoveAction = (PropertyRemoveAction) action;
                    String propertyName = propertyRemoveAction.propertyName();
                    entityInContext.deleteProperty(propertyName);
                  } else {
                    throw new IllegalArgumentException("Invalid entity action");
                  }
                });
          }
          entity.blobs().forEach(remoteBlob -> {
            entityInContext.setBlob(remoteBlob.blobName(),
                Try.of(() -> RemoteInputStreamClient.wrap(remoteBlob.blobStream())).getOrNull());
          });

          /**
           * Process properties to save or update.
           * Saving null value for a property effectively deletes the property
           */

          Iterator<String> propertyIterator = entity.propertyMap().keySet().iterator();
          while (propertyIterator.hasNext()) {
            String key = propertyIterator.next();
            Comparable value = entity.propertyMap().get(key);
            entityInContext.setProperty(key, value);
          }

          resultEntities.add(new Marshaller().with(entityInContext)
              .build());
        });
        finalResult.set(new RemoteEntitiesBuilder()
            .entities(resultEntities)
            .build());
      });
    }
    return Optional.ofNullable(finalResult.get());
  }

  @Override public Optional<RemoteEntity> getEntity(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException {
    Optional<RemoteEntities> optional = getEntities(query);
    if(optional.isPresent()) {
      return optional.get().entities().stream().findFirst();
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<RemoteEntities> getEntities(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException {

    String dir = query.environment();
    String entityType = query.entityType();
    String nameSpace = query.nameSpace();

    List<RemoteEntity> remoteEntities = new ArrayList<>();

    AtomicReference<Long> count = new AtomicReference<>(0L);

    manager.transactPersistentEntityStore(dir, true, txn -> {

      AtomicReference<EntityIterable> result = null;
      if (nameSpace != null && !nameSpace.isEmpty() && entityType != null) {
        result.set(txn.findWithProp(entityType, Constants.NAMESPACE_PROPERTY)
            .intersect(txn.find(entityType, Constants.NAMESPACE_PROPERTY, nameSpace)));
      } else if(entityType != null){
        result.set(txn.getAll(entityType)
            .minus(txn.findWithProp(entityType, Constants.NAMESPACE_PROPERTY)));
      }

      if(query.entityId() == null && entityType == null) {
        throw new IllegalArgumentException("Either entity ID or entity type must be present");
      }

      if (query.entityId() != null) {
        // Query by id encompasses name spacing
        EntityId idOfEntity = txn.toEntityId(query.entityId());
        final Entity entity = txn.getEntity(idOfEntity);
        remoteEntities.add(new Marshaller().with(entity)
            .with(FluentIterable.from(query.blobQueries()).toArray(BlobQuery.class))
            .with(FluentIterable.from(query.linkQueries()).toArray(LinkQuery.class))
            .build());
        count.set(1L);
      } else {
        result.set(processFilter(txn, result, query.transactionFilters(), entityType));
        count.set(result.get().count());
        result.set(result.get()
            .skip(query.offset().intValue())
            .take(query.max().intValue()));

        for (Entity entity : result.get()) {
          final Map<String, Comparable> comparableMap = new LinkedHashMap<>();
          for (String property : entity.getPropertyNames()) {
            Comparable value = entity.getProperty(property);
            if (value != null) {
              if (value != null) {
                if (value instanceof EmbeddedEntityIterable) {
                  comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
                } else if (value instanceof EmbeddedArrayIterable) {
                  comparableMap.put(
                      property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
                } else {
                  comparableMap.put(property, value);
                }
              }
            }
          }
          remoteEntities.add(new Marshaller().with(entity)
              .with(FluentIterable.from(query.blobQueries()).toArray(BlobQuery.class))
              .with(FluentIterable.from(query.linkQueries()).toArray(LinkQuery.class))
              .build());
        }
      }
    });

    return Optional.ofNullable(new RemoteEntitiesBuilder()
        .entities(remoteEntities)
        .offset(query.offset())
        .max(query.max())
        .count(count.get())
        .build());
  }

  @Override public Boolean removeEntity(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException {
    return removeEntities(new RemoteEntityQuery[] {query});
  }

  @Override public Boolean removeEntities(@NotNull RemoteEntityQuery[] queries)
      throws NotBoundException, RemoteException {
    final boolean[] success = {false};

    Map<String, List<RemoteEntityQuery>> dirOrderedQueries = sort(queries);
    Iterator<String> it = dirOrderedQueries.keySet().iterator();
    while (it.hasNext()) {
      String dir = it.next();
      List<RemoteEntityQuery> queryList = dirOrderedQueries.get(dir);
      manager.transactPersistentEntityStore(dir, false, txn -> {
        queryList.forEach(query -> {
          String entityType = query.entityType();
          String nameSpace = query.nameSpace();
          EntityIterable result = null;
          if (nameSpace != null && !nameSpace.isEmpty()) {
            result =
                txn.findWithProp(entityType, Constants.NAMESPACE_PROPERTY)
                    .intersect(txn.find(entityType, Constants.NAMESPACE_PROPERTY, nameSpace));
          } else {
            result =
                txn.getAll(entityType)
                    .minus(txn.findWithProp(entityType, Constants.NAMESPACE_PROPERTY));
          }
          final boolean[] hasError = {false};
          for (Entity entity : result) {

            entity.getLinkNames().forEach(linkName -> {
              Entity linked = entity.getLink(linkName);
              entity.deleteLink(linkName, linked);
            });

            // TODO: This is a performance issue
            final List<String> allLinkNames = ((PersistentEntityStoreImpl) txn.getStore())
                .getAllLinkNames(
                    (PersistentStoreTransaction) txn.getStore().getCurrentTransaction());
            for (final String entityType1 : txn.getEntityTypes()) {
              for (final String linkName : allLinkNames) {
                for (final Entity referrer : txn.findLinks(entityType1, entity, linkName)) {
                  referrer.deleteLink(linkName, entity);
                }
              }
            }

            entity.getBlobNames().forEach(blobName -> {
              entity.deleteBlob(blobName);
            });

            if (!entity.delete()) {
              hasError[0] = true;
            }
          }
          success[0] = !hasError[0];
        });
      });
    }

    return success[0];
  }

  @Override public Boolean saveProperty(@NotNull RemoteEntityProperty property)
      throws NotBoundException, RemoteException {
    String dir = property.environment();
    String entityType = property.entityType();
    String nameSpace = property.nameSpace();
    AtomicReference<Boolean> updated = new AtomicReference<>(false);
    manager.transactPersistentEntityStore(dir, false, txn -> {
      final AtomicReference<EntityIterable> reference = new AtomicReference<>();
      if (nameSpace != null) {
        reference.set(
            txn.getAll(entityType)
                .intersect(
                    txn.find(entityType, Constants.NAMESPACE_PROPERTY, nameSpace)));
      } else {
        reference.set(txn.getAll(entityType));
      }
      property.propertyActions().forEach(entityPropertyAction -> {
        if (entityPropertyAction instanceof PropertyRenameAction) {
          reference.set(reference.get().intersect(txn.getAll(entityType)));
          EntityIterable entities = reference.get();
          String propertyName = ((PropertyRenameAction) entityPropertyAction).propertyName();
          String newPropertyName = ((PropertyRenameAction) entityPropertyAction).newPropertyName();
          Boolean overwrite = ((PropertyRenameAction) entityPropertyAction).overwrite();
          entities.forEach(entity -> {
            if (entity.getProperty(newPropertyName) != null && !overwrite) {
              throw new IllegalArgumentException(
                  "Conflicting property " + newPropertyName + " exists");
            }
            Comparable propertyValue = entity.getProperty(propertyName);
            entity.deleteProperty(propertyName);
            entity.setProperty(propertyName, propertyValue);
          });
        }
      });
      updated.set(true);
    });
    return updated.get();
  }

  @Override public Boolean removeProperty(@NotNull RemoteEntityProperty property)
      throws NotBoundException, RemoteException {
    String dir = property.environment();
    String entityType = property.entityType();
    String nameSpace = property.nameSpace();
    AtomicReference<Boolean> removed = new AtomicReference<>(false);
    manager.transactPersistentEntityStore(dir, false, txn -> {
      final AtomicReference<EntityIterable> reference = new AtomicReference<>();
      if (nameSpace != null) {
        reference.set(
            txn.getAll(entityType)
                .intersect(
                    txn.find(entityType, Constants.NAMESPACE_PROPERTY, nameSpace)));
      } else {
        reference.set(txn.getAll(entityType));
      }
      property.propertyActions().forEach(entityPropertyAction -> {
        if (entityPropertyAction instanceof PropertyRemoveAction) {
          reference.set(reference.get().intersect(txn.getAll(entityType)));
          String propertyName = ((PropertyRemoveAction) entityPropertyAction).propertyName();
          reference.set(reference.get().intersect(txn.findWithProp(entityType, propertyName)));
          reference.get().forEach(entity -> {
            entity.deleteProperty(propertyName);
          });
        } else {
          throw new IllegalArgumentException("Invalid property action");
        }
      });
      removed.set(true);
    });
    return removed.get();
  }

  @Override
  public Boolean removeEntityType(@NotNull RemoteEntityQuery query)
      throws RemoteException, NotBoundException {
    final boolean[] success = {false};

    String dir = query.environment();
    String entityType = query.entityType();
    String nameSpace = query.nameSpace();

    manager.transactPersistentEntityStore(dir, false, txn -> {
      final AtomicReference<EntityIterable> reference = new AtomicReference<>();
      if (nameSpace != null) {
        reference.set(
            txn.getAll(entityType)
                .intersect(
                    txn.find(entityType, Constants.NAMESPACE_PROPERTY, nameSpace)));
      } else {
        reference.set(txn.getAll(entityType));
      }
      EntityIterable result = reference.get();
      for (Entity entity : result) {
        entity.getLinkNames().forEach(linkName -> {
          Entity linked = entity.getLink(linkName);
          entity.deleteLink(linkName, linked);
        });
        // TODO: This is a performance issue
        final List<String> allLinkNames = ((PersistentEntityStoreImpl) txn.getStore())
            .getAllLinkNames((PersistentStoreTransaction) txn.getStore().getCurrentTransaction());
        for (final String entityType1 : txn.getEntityTypes()) {
          for (final String linkName : allLinkNames) {
            for (final Entity referrer : txn.findLinks(entityType1, entity, linkName)) {
              referrer.deleteLink(linkName, entity);
            }
          }
        }
      }
    });
    return success[0];
  }

  /**
   * Sort an array of {@linkplain RemoteEntityUpdate} by Environment path.
   *
   * @param entityUpdates
   * @return
   */
  private static Map<String, List<RemoteEntityUpdate>> sort(RemoteEntityUpdate[] entityUpdates) {
    Map<String, List<RemoteEntityUpdate>> envOrderedUpdates = new HashMap<>();
    Arrays.asList(entityUpdates).forEach(entityUpdate -> {
      String dir = entityUpdate.entity().environment();
      List<RemoteEntityUpdate> remoteEntityUpdates = envOrderedUpdates.get(dir);
      if (remoteEntityUpdates == null) {
        remoteEntityUpdates = new ArrayList<>();
      }
      remoteEntityUpdates.add(entityUpdate);
    });
    return envOrderedUpdates;
  }

  /**
   * Sort an array of {@linkplain RemoteEntity} by Application ID.
   *
   * @param entities
   * @return
   */
  private static Map<String, List<RemoteEntity>> sort(RemoteEntity[] entities) {
    Map<String, List<RemoteEntity>> dirOrderedEntities = new HashMap<>();
    Arrays.asList(entities).forEach(remoteEntity -> {
      String dir = remoteEntity.environment();
      List<RemoteEntity> remoteEntityUpdates = dirOrderedEntities.get(dir);
      if (remoteEntityUpdates == null) {
        remoteEntityUpdates = new ArrayList<>();
      }
      remoteEntityUpdates.add(remoteEntity);
      dirOrderedEntities.put(dir, remoteEntityUpdates);
    });
    return dirOrderedEntities;
  }

  private static Map<String, List<RemoteEntityQuery>> sort(RemoteEntityQuery[] entities) {
    Map<String, List<RemoteEntityQuery>> appOrderedQueries = new HashMap<>();
    Arrays.asList(entities).forEach(remoteEntity -> {
      String dir = remoteEntity.environment();
      List<RemoteEntityQuery> entityQueries = appOrderedQueries.get(dir);
      if (entityQueries == null) {
        entityQueries = new ArrayList<>();
      }
      entityQueries.add(remoteEntity);
    });
    return appOrderedQueries;
  }

  private EntityIterable processFilter(StoreTransaction txn,
      AtomicReference<EntityIterable> reference,
      List<RemoteTransactionFilter> filters, String entityType) {
    filters.forEach(transactionFilter -> {
      String propertyName = transactionFilter.propertyName();
      Comparable propertyValue = transactionFilter.propertyValue();

      if (RemoteTransactionFilter.EQUALITY_OP.EQUAL.equals(transactionFilter.equalityOp())) {
        reference.set(
            reference.get()
                .intersect(txn.find(entityType, propertyName, propertyValue)));
      } else if (RemoteTransactionFilter.EQUALITY_OP.NOT_EQUAL.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.getAll(entityType)
                .minus(txn.find(entityType, propertyName, propertyValue))));
      } else if (RemoteTransactionFilter.EQUALITY_OP.STARTS_WITH.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.findStartingWith(entityType, propertyName,
                String.valueOf(propertyValue))));
      } else if (RemoteTransactionFilter.EQUALITY_OP.NOT_STARTS_WITH.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.getAll(entityType)
                .minus(txn.findStartingWith(entityType, propertyName,
                    String.valueOf(propertyValue)))));
      } else if (RemoteTransactionFilter.EQUALITY_OP.IN_RANGE.equals(
          transactionFilter.equalityOp())) {
        throw new IllegalArgumentException(
            "Not yet implemented: "
                + RemoteTransactionFilter.EQUALITY_OP.IN_RANGE.toString());
      } else if (RemoteTransactionFilter.EQUALITY_OP.CONTAINS.equals(
          transactionFilter.equalityOp())) {
        throw new IllegalArgumentException(
            "Not yet implemented: "
                + RemoteTransactionFilter.EQUALITY_OP.CONTAINS.toString());
      }
    });
    return reference.get();
  }

  private Comparable asObject(EmbeddedEntityIterable entityIterable) {
    Comparable comparable = entityIterable.asObject();
    if (comparable instanceof ComparableHashMap) {
      ComparableHashMap comparableHashMap = (ComparableHashMap) comparable;
      ComparableHashMap replacements = new ComparableHashMap();
      comparableHashMap.forEach((key, value) -> {
        if (value instanceof EmbeddedEntityIterable) {
          replacements.put(key, asObject((EmbeddedEntityIterable) value));
        }
      });
      comparableHashMap.putAll(replacements);
      return comparableHashMap;
    }
    return comparable;
  }
}
