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
package com.divroll.datafactory.repositories;

import com.divroll.datafactory.builders.DataFactoryEntities;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityTypes;
import com.divroll.datafactory.builders.DataFactoryProperty;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.queries.EntityTypeQuery;
import com.divroll.datafactory.exceptions.DataFactoryException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public interface EntityStore extends Remote {

  Optional<DataFactoryEntity> saveEntity(@NotNull DataFactoryEntity entity)
      throws DataFactoryException, DataFactoryException, NotBoundException, RemoteException;

  Optional<DataFactoryEntities> saveEntities(@NotNull DataFactoryEntity[] entities)
      throws DataFactoryException, NotBoundException, RemoteException;

  Optional<DataFactoryEntity> getEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException;

  Optional<DataFactoryEntities> getEntities(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException;

  /**
   * Remove entities matching the query
   *
   * @param query
   * @return
   * @throws NotBoundException
   * @throws RemoteException
   */
  Boolean removeEntity(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException;

  /**
   * Remove entities matching the list of queries
   *
   * @param queries
   * @return
   * @throws NotBoundException
   * @throws RemoteException
   */
  Boolean removeEntities(@NotNull EntityQuery[] queries)
      throws DataFactoryException, NotBoundException, RemoteException;

  Boolean saveProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException;

  Boolean removeProperty(@NotNull DataFactoryProperty property)
      throws DataFactoryException, NotBoundException, RemoteException;

  Boolean removeEntityType(@NotNull EntityQuery query)
      throws DataFactoryException, NotBoundException, RemoteException;

  Optional<DataFactoryEntityTypes> getEntityTypes(EntityTypeQuery query)
      throws DataFactoryException, NotBoundException, RemoteException;
}
