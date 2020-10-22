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
package com.divroll.datafactory;

import com.divroll.datafactory.actions.BlobRemoveAction;
import com.divroll.datafactory.actions.BlobRenameAction;
import com.divroll.datafactory.actions.BlobRenameRegexAction;
import com.divroll.datafactory.actions.LinkAction;
import com.divroll.datafactory.actions.LinkNewEntityAction;
import com.divroll.datafactory.actions.LinkRemoveAction;
import com.divroll.datafactory.actions.OppositeLinkAction;
import com.divroll.datafactory.actions.OppositeLinkRemoveAction;
import com.divroll.datafactory.actions.PropertyCopyAction;
import com.divroll.datafactory.actions.PropertyIndexAction;
import com.divroll.datafactory.actions.PropertyRemoveAction;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.TransactionFilter;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityId;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Unmarshaller {

  DataFactoryEntity entity;
  StoreTransaction txn;

  public Unmarshaller with(@NotNull DataFactoryEntity entity, StoreTransaction txn) {
    this.entity = entity;
    this.txn = txn;
    return this;
  }

  public Entity build() {
    if (entity == null) {
      new IllegalArgumentException("Must set RemoteEntity to unmarshall into Entity");
    }
    Entity newEntity = txn.newEntity(entity.entityType());
    AtomicReference reference = new AtomicReference<>(txn.getAll(entity.entityType()));
    buildContexedEntity(entity, reference, txn);
    processActions(entity, reference, newEntity, txn);
    throw new IllegalArgumentException("Not yet implemented");
  }

  public static Entity buildContexedEntity(DataFactoryEntity entity,
      AtomicReference<EntityIterable> referenceToScope, StoreTransaction txn) {
    if (entity.nameSpace() != null) {
      referenceToScope.set(
          txn.getAll(entity.entityType())
              .intersect(
                  txn.find(entity.entityType(), Constants.NAMESPACE_PROPERTY,
                      entity.nameSpace())));
    } else {
      referenceToScope.set(txn.getAll(entity.entityType()));
    }

    final Entity entityInContext = entity.entityId() != null ?
        txn.getEntity(txn.toEntityId(entity.entityId())) : txn.newEntity(entity.entityType());
    if (referenceToScope.get().indexOf(entityInContext) == -1 && entity.nameSpace() != null) {
      throw new IllegalArgumentException(
          "Entity " + entity.entityId() + " not found in namespace " + entity.nameSpace());
    }
    return entityInContext;
  }

  public static Entity processActions(DataFactoryEntity dataFactoryEntity,
      AtomicReference<EntityIterable> reference, Entity entityInContext, StoreTransaction txn) {
    dataFactoryEntity.entityActions().forEach(
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
                entityInContext.getLinks(linkName).forEach(otherEntity -> {
                  //otherEntity.deleteLink(linkName, entityInContext);
                  entityInContext.deleteLink(linkName, otherEntity);
                });
                entityInContext.setLink(linkName, targetEntity);
              } else {
                entityInContext.addLink(linkName, targetEntity);
              }
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

            Entity sourceEntity = entityInContext;
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
                .forEach(linkedEntity -> {
                  linkedEntity.deleteLink(oppositeLinkName, entityInContext);
                  entityInContext.deleteLink(linkName, linkedEntity);
                });
          } else if (action instanceof LinkNewEntityAction) {
            LinkNewEntityAction newLinkAction = (LinkNewEntityAction) action;
            String linkName = newLinkAction.linkName();
            Boolean isSet = newLinkAction.isSet();
            DataFactoryEntity newDataFactoryEntity = newLinkAction.newEntity();
            Entity targetEntity = new Unmarshaller().with(newDataFactoryEntity, txn).build();
            if (isSet) {
              entityInContext.getLinks(linkName).forEach(otherEntity -> {
                //otherEntity.deleteLink(linkName, entityInContext);
                entityInContext.deleteLink(linkName, otherEntity);
              });
              entityInContext.setLink(linkName, targetEntity);
            } else {
              entityInContext.addLink(linkName, targetEntity);
            }
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
                  reference.get().intersect(txn.getAll(entityInContext.getType())).getFirst();
              if (first != null) {
                entityInContext.setProperty(copyProperty, first.getProperty(copyProperty));
              }
            } else {
              Entity last =
                  reference.get().intersect(txn.getAll(entityInContext.getType())).getLast();
              if (last != null) {
                entityInContext.setProperty(copyProperty, last.getProperty(copyProperty));
              }
            }
          } else if (action instanceof PropertyIndexAction) {
            PropertyIndexAction propertyIndexAction = (PropertyIndexAction) action;
            String propertyName = propertyIndexAction.propertyName();
            Comparable propertyValue = dataFactoryEntity.propertyMap().get(propertyName);
            if (!txn.find(entityInContext.getType(), propertyName, propertyValue).isEmpty()) {
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
    return entityInContext;
  }

  public static EntityIterable filterContext(AtomicReference<EntityIterable> reference,
      List<TransactionFilter> filters, String entityType, StoreTransaction txn) {
    filters.forEach(transactionFilter -> {
      String propertyName = transactionFilter.propertyName();
      Comparable propertyValue = transactionFilter.propertyValue();

      if (TransactionFilter.EQUALITY_OP.EQUAL.equals(transactionFilter.equalityOp())) {
        reference.set(
            reference.get()
                .intersect(txn.find(entityType, propertyName, propertyValue)));
      } else if (TransactionFilter.EQUALITY_OP.NOT_EQUAL.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.getAll(entityType)
                .minus(txn.find(entityType, propertyName, propertyValue))));
      } else if (TransactionFilter.EQUALITY_OP.STARTS_WITH.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.findStartingWith(entityType, propertyName,
                String.valueOf(propertyValue))));
      } else if (TransactionFilter.EQUALITY_OP.NOT_STARTS_WITH.equals(
          transactionFilter.equalityOp())) {
        reference.set(reference.get()
            .intersect(txn.getAll(entityType)
                .minus(txn.findStartingWith(entityType, propertyName,
                    String.valueOf(propertyValue)))));
      } else if (TransactionFilter.EQUALITY_OP.IN_RANGE.equals(
          transactionFilter.equalityOp())) {
        throw new IllegalArgumentException(
            "Not yet implemented: "
                + TransactionFilter.EQUALITY_OP.IN_RANGE.toString());
      } else if (TransactionFilter.EQUALITY_OP.CONTAINS.equals(
          transactionFilter.equalityOp())) {
        throw new IllegalArgumentException(
            "Not yet implemented: "
                + TransactionFilter.EQUALITY_OP.CONTAINS.toString());
      }
    });
    return reference.get();
  }
}
