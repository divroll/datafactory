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

import com.divroll.datafactory.builders.DataFactoryEntities;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryProperty;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.exceptions.DataFactoryException;
import com.divroll.datafactory.repositories.EntityStore;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EntityStoreClientImpl implements EntityStore {

  private Integer port;
  private String host;

  private EntityStoreClientImpl() {

  }

  public EntityStoreClientImpl(String host, Integer port) {
    this.host = host;
    this.port = port;
  }

  @Override public Optional<DataFactoryEntity> saveEntity(@NotNull DataFactoryEntity entity)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.saveEntity(entity);
  }

  @Override
  public Optional<DataFactoryEntities> saveEntities(@NotNull DataFactoryEntity[] entities)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.saveEntities(entities);
  }

  @Override public Optional<DataFactoryEntity> getEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.getEntity(query);
  }

  @Override public Optional<DataFactoryEntities> getEntities(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.getEntities(query);
  }

  @Override public Boolean removeEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.removeEntity(query);
  }

  @Override public Boolean removeEntities(@NotNull EntityQuery[] queries)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.removeEntities(queries);
  }

  @Override public Boolean saveProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.saveProperty(property);
  }

  @Override public Boolean removeProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.removeProperty(property);
  }

  @Override public Boolean removeEntityType(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    EntityStore entityStore =
        (EntityStore) registry.lookup(EntityStore.class.getName());
    return entityStore.removeEntityType(query);
  }
}
