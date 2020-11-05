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
package com.divroll.datafactory.repositories.impl;

import com.divroll.datafactory.Constants;
import com.divroll.datafactory.Marshaller;
import com.divroll.datafactory.Unmarshaller;
import com.divroll.datafactory.actions.PropertyRemoveAction;
import com.divroll.datafactory.actions.PropertyRenameAction;
import com.divroll.datafactory.builders.DataFactoryEntities;
import com.divroll.datafactory.builders.DataFactoryEntitiesBuilder;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityType;
import com.divroll.datafactory.builders.DataFactoryEntityTypeBuilder;
import com.divroll.datafactory.builders.DataFactoryEntityTypes;
import com.divroll.datafactory.builders.DataFactoryEntityTypesBuilder;
import com.divroll.datafactory.builders.DataFactoryProperty;
import com.divroll.datafactory.builders.queries.BlobQuery;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.queries.EntityTypeQuery;
import com.divroll.datafactory.builders.queries.LinkQuery;
import com.divroll.datafactory.conditions.UnsatisfiedCondition;
import com.divroll.datafactory.database.DatabaseManager;
import com.divroll.datafactory.exceptions.DataFactoryException;
import com.divroll.datafactory.lucene.LuceneIndexer;
import com.divroll.datafactory.properties.EmbeddedArrayIterable;
import com.divroll.datafactory.properties.EmbeddedEntityIterable;
import com.divroll.datafactory.repositories.EntityStore;
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
import org.jetbrains.annotations.NotNull;
import util.ComparableHashMap;

import static com.divroll.datafactory.Unmarshaller.processActions;
import static com.divroll.datafactory.Unmarshaller.processConditions;
import static com.divroll.datafactory.Unmarshaller.processUnsatisfiedConditions;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EntityStoreImpl extends StoreBaseImpl implements EntityStore {

  private static final Logger LOG = LoggerFactory.getLogger(EntityStoreImpl.class);

  private DatabaseManager manager;

  private LuceneIndexer searchIndexer;

  public EntityStoreImpl(DatabaseManager databaseManager, LuceneIndexer searchIndexer)
      throws DataFactoryException, NotBoundException, RemoteException {
    this.manager = databaseManager;
    this.searchIndexer = searchIndexer;
  }

  @Override public Optional<DataFactoryEntity> saveEntity(@NotNull DataFactoryEntity entity)
      throws DataFactoryException, NotBoundException, RemoteException {
    DataFactoryEntities dataFactoryEntities = saveEntities(new DataFactoryEntity[] {entity}).get();
    return dataFactoryEntities.entities().stream().findFirst();
  }

  @Override
  public Optional<DataFactoryEntities> saveEntities(@NotNull DataFactoryEntity[] entities)
      throws DataFactoryException, NotBoundException, RemoteException {
    Map<String, List<DataFactoryEntity>> envIdOrderedEntities = sort(entities);
    Iterator<String> it = envIdOrderedEntities.keySet().iterator();
    AtomicReference<DataFactoryEntities> finalResult = new AtomicReference<>(null);
    while (it.hasNext()) {
      String dir = it.next();
      List<DataFactoryEntity> dataFactoryEntityList = envIdOrderedEntities.get(dir);
      manager.transactPersistentEntityStore(dir, false, txn -> {

        List<DataFactoryEntity> resultEntities = new ArrayList<>();
        dataFactoryEntityList.forEach(entity -> {

          /**
           * Build a {@linkplain Entity} in context of a referenced scoped entities based on the
           * {@code namespace}
           */

          final AtomicReference<EntityIterable> reference = new AtomicReference<>();
          final Entity entityInContext = Unmarshaller.buildContexedEntity(entity, reference, txn);

          /**
           * Filter the {@linkplain EntityIterable} reference scoped
           */
          reference.set(
              Unmarshaller.filterContext(reference, entity.filters(), entity.entityType(), txn));

          /**
           * Process entity conditions, if there are no {@linkplain UnsatisfiedCondition}
           * process the actions, blobs and properties
           */
          processUnsatisfiedConditions(reference, entity.conditions(), entityInContext, txn);

          /**
           * Process entity actions within the context of the {@linkplain Entity}
           */
          processActions(entity, reference, entityInContext, txn);

          entity.blobs().forEach(remoteBlob -> {
            InputStream blobStream =
                Try.of(() -> RemoteInputStreamClient.wrap(remoteBlob.blobStream())).getOrNull();
            entityInContext.setBlob(remoteBlob.blobName(), blobStream);
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

          resultEntities.add(new Marshaller()
              .with(entityInContext)
              .build());
        });
        finalResult.set(new DataFactoryEntitiesBuilder()
            .entities(resultEntities)
            .build());
      });
    }
    return Optional.ofNullable(finalResult.get());
  }

  @Override public Optional<DataFactoryEntity> getEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    Optional<DataFactoryEntities> optional = getEntities(query);
    if (optional.isPresent()) {
      return optional.get().entities().stream().findFirst();
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<DataFactoryEntities> getEntities(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {

    String dir = query.environment();
    AtomicReference<String> entityType = new AtomicReference<>(query.entityType());
    String nameSpace = query.nameSpace();

    List<DataFactoryEntity> remoteEntities = new ArrayList<>();

    AtomicReference<Long> count = new AtomicReference<>(0L);

    manager.transactPersistentEntityStore(dir, true, txn -> {

      AtomicReference<EntityIterable> result = new AtomicReference<>();
      if (nameSpace != null && !nameSpace.isEmpty() && entityType.get() != null) {
        result.set(txn.getAll(entityType.get())
            .intersect(txn.find(entityType.get(), Constants.NAMESPACE_PROPERTY, nameSpace)));
      } else if (entityType.get() != null) {
        result.set(txn.getAll(entityType.get()));
      }

      if (query.entityId() == null && entityType.get() == null) {
        throw new IllegalArgumentException("Either entity ID or entity type must be present");
      } else if (query.entityId() == null) {
        processConditions(searchIndexer, entityType.get(), query.conditions(), result, txn);
      }

      if (query.entityId() != null) {
        // Query by id encompasses name spacing
        EntityId idOfEntity = txn.toEntityId(query.entityId());
        final Entity entity = txn.getEntity(idOfEntity);
        entityType.set(entity.getType());
        remoteEntities.add(new Marshaller().with(entity)
            .with(FluentIterable.from(query.blobQueries()).toArray(BlobQuery.class))
            .with(FluentIterable.from(query.linkQueries()).toArray(LinkQuery.class))
            .build());
        count.set(1L);
      } else {
        result.set(
            Unmarshaller.filterContext(result, query.filters(), entityType.get(), txn));
        count.set(result.get().size());
        result.set(result.get().skip(query.offset()).take(query.max()));

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

    return Optional.ofNullable(new DataFactoryEntitiesBuilder()
        .entities(remoteEntities)
        .offset(query.offset())
        .max(query.max())
        .count(count.get())
        .build());
  }

  @Override public Boolean removeEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    return removeEntities(new EntityQuery[] {query});
  }

  @Override public Boolean removeEntities(@NotNull EntityQuery[] queries)
      throws DataFactoryException, NotBoundException, RemoteException {
    final boolean[] success = {false};

    Map<String, List<EntityQuery>> dirOrderedQueries = sort(queries);
    Iterator<String> it = dirOrderedQueries.keySet().iterator();
    while (it.hasNext()) {
      String dir = it.next();
      List<EntityQuery> queryList = dirOrderedQueries.get(dir);
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

  @Override public Boolean saveProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException {
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

  @Override public Boolean removeProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException {
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
  public Boolean removeEntityType(@NotNull EntityQuery query)
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

  @Override public Optional<DataFactoryEntityTypes> getEntityTypes(EntityTypeQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    AtomicReference<DataFactoryEntityTypes> atomicReference = new AtomicReference<>();
    manager.transactPersistentEntityStore(query.environment(), true, txn -> {
      List<String> entityTypes = txn.getEntityTypes();
      Long count = null;
      if(query.count()) {
        EntityIterable entities = txn.getAll(query.entityType());
        count = entities.size();
      }
      List<DataFactoryEntityType> dataFactoryEntityTypeList = new ArrayList<>();
      if (entityTypes != null) {
        for (String entityType : entityTypes) {
          dataFactoryEntityTypeList.add(new DataFactoryEntityTypeBuilder()
              .entityTypeName(entityType)
              .build());
        }
      }
      DataFactoryEntityTypes dataFactoryEntityTypes = new DataFactoryEntityTypesBuilder()
          .entityTypes(dataFactoryEntityTypeList)
          .entityCount(count)
          .build();
      atomicReference.set(dataFactoryEntityTypes);
    });
    return Optional.ofNullable(atomicReference.get());
  }

  /**
   * Sort an array of {@linkplain EntityUpdate} by Environment path.
   *
   * @param entityUpdates
   * @return
   */
  //private static Map<String, List<EntityUpdate>> sort(EntityUpdate[] entityUpdates) {
  //  Map<String, List<EntityUpdate>> envOrderedUpdates = new HashMap<>();
  //  Arrays.asList(entityUpdates).forEach(entityUpdate -> {
  //    String dir = entityUpdate.entity().environment();
  //    List<EntityUpdate> remoteEntityUpdates = envOrderedUpdates.get(dir);
  //    if (remoteEntityUpdates == null) {
  //      remoteEntityUpdates = new ArrayList<>();
  //    }
  //    remoteEntityUpdates.add(entityUpdate);
  //  });
  //  return envOrderedUpdates;
  //}

  /**
   * Sort an array of {@linkplain DataFactoryEntity} by Application ID.
   *
   * @param entities
   * @return
   */
  private static Map<String, List<DataFactoryEntity>> sort(DataFactoryEntity[] entities) {
    Map<String, List<DataFactoryEntity>> dirOrderedEntities = new HashMap<>();
    Arrays.asList(entities).forEach(remoteEntity -> {
      String dir = remoteEntity.environment();
      List<DataFactoryEntity> dataFactoryEntityUpdates = dirOrderedEntities.get(dir);
      if (dataFactoryEntityUpdates == null) {
        dataFactoryEntityUpdates = new ArrayList<>();
      }
      dataFactoryEntityUpdates.add(remoteEntity);
      dirOrderedEntities.put(dir, dataFactoryEntityUpdates);
    });
    return dirOrderedEntities;
  }

  private static Map<String, List<EntityQuery>> sort(EntityQuery[] entities) {
    Map<String, List<EntityQuery>> appOrderedQueries = new HashMap<>();
    Arrays.asList(entities).forEach(remoteEntity -> {
      String dir = remoteEntity.environment();
      List<EntityQuery> entityQueries = appOrderedQueries.get(dir);
      if (entityQueries == null) {
        entityQueries = new ArrayList<>();
      }
      entityQueries.add(remoteEntity);
    });
    return appOrderedQueries;
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
