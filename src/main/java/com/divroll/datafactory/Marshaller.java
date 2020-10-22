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

import com.divroll.datafactory.builders.DataFactoryBlobBuilder;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.BlobQuery;
import com.divroll.datafactory.builders.queries.LinkQuery;
import com.divroll.datafactory.builders.DataFactoryBlob;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.properties.EmbeddedArrayIterable;
import com.divroll.datafactory.properties.EmbeddedEntityIterable;
import com.google.common.collect.Iterables;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Marshaller {

  private Entity entity;
  private List<LinkQuery> linkQueries;
  private List<BlobQuery> blobQueries;

  public Marshaller with(@NotNull Entity entity) {
    if (this.entity != null) {
      throw new IllegalArgumentException("Entity is already set");
    }
    this.entity = entity;
    return this;
  }

  public Marshaller with(@NotNull LinkQuery[] linkQueries) {
    if (this.linkQueries != null) {
      throw new IllegalArgumentException("LinkQuery is already set");
    }
    this.linkQueries = Arrays.asList(linkQueries);
    return this;
  }

  public Marshaller with(@NotNull BlobQuery[] blobQueries) {
    if (this.blobQueries != null) {
      throw new IllegalArgumentException("BlobQuery is already set");
    }
    this.blobQueries = Arrays.asList(blobQueries);
    return this;
  }

  /**
   * Builds a {@linkplain Entity} into a {@linkplain DataFactoryEntity} for remote transmission. This
   * method should be called within a database {@linkplain StoreTransaction}.
   *
   * @return {@code entity}
   */
  public DataFactoryEntity build() {
    final DataFactoryEntityBuilder builder = new DataFactoryEntityBuilder();

    entity.getPropertyNames().forEach(propertyName -> {
      Comparable propertyValue = entity.getProperty(propertyName);
      if (propertyValue != null) {
        if (propertyValue instanceof EmbeddedEntityIterable) {
          builder.putPropertyMap(propertyName,
              ((EmbeddedEntityIterable) propertyValue).asObject());
        } else if (propertyValue instanceof EmbeddedArrayIterable) {
          builder.putPropertyMap(propertyName,
              (Comparable) ((EmbeddedArrayIterable) propertyValue).asObject());
        } else {
          builder.putPropertyMap(propertyName, propertyValue);
        }
      }
    });

    List<DataFactoryBlob> blobs = new ArrayList<>();
    List<DataFactoryEntity> links = new ArrayList<>();

    if (linkQueries == null) {
      linkQueries = new ArrayList<>();
    }
    linkQueries.forEach(linkQuery -> {
      EntityIterable linkedEntities = entity.getLinks(linkQuery.linkName());
      if (linkQuery.targetEntityId() != null) {
        linkedEntities.forEach(linkedEntity -> {
          if (linkedEntity.getId().toString().equals(linkQuery.targetEntityId())) {
            links.add(new Marshaller()
                .with(linkedEntity)
                .build());
          }
        });
      } else {
        linkedEntities.forEach(linkedEntity -> {
          links.add(new Marshaller()
              .with(linkedEntity)
              .build());
        });
      }
    });

    if (blobQueries == null) {
      blobQueries = new ArrayList<>();
    }
    blobQueries.forEach(blobQuery -> {
      entity.getBlobNames().forEach(blobName -> {
        if (blobQuery.blobName().equals(blobName) && blobQuery.include()) {
          InputStream blobStream = entity.getBlob(blobName);
          blobs.add(new DataFactoryBlobBuilder()
              .blobName(blobName)
              .blobStream(new SimpleRemoteInputStream(blobStream))
              .build());
        }
      });
    });

    return builder
        .environment(entity.getStore().getLocation())
        .nameSpace(String.valueOf(entity.getProperty(Constants.NAMESPACE_PROPERTY)))
        .entityType(entity.getType())
        .entityId(entity.getId().toString())
        .blobNames(Iterables.toArray(entity.getBlobNames(), String.class))
        .linkNames(Iterables.toArray(entity.getLinkNames(), String.class))
        .blobs(blobs)
        .links(links)
        .build();
  }

}
