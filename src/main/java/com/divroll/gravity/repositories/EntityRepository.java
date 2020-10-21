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
package com.divroll.gravity.repositories;

import com.divroll.gravity.builders.RemoteEntities;
import com.divroll.gravity.builders.RemoteEntity;
import com.divroll.gravity.builders.RemoteEntityProperty;
import com.divroll.gravity.builders.RemoteEntityQuery;
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
public interface EntityRepository extends Remote {

  Optional<RemoteEntity> saveEntity(@NotNull RemoteEntity entity)
      throws NotBoundException, RemoteException;

  Optional<RemoteEntities> saveEntities(@NotNull RemoteEntity[] entities)
      throws NotBoundException, RemoteException;

  Optional<RemoteEntity> getEntity(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException;

  Optional<RemoteEntities> getEntities(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException;

  /**
   * Remove entities matching the query
   *
   * @param query
   * @return
   * @throws NotBoundException
   * @throws RemoteException
   */
  Boolean removeEntity(@NotNull RemoteEntityQuery query) throws NotBoundException, RemoteException;

  /**
   * Remove entities matching the list of queries
   *
   * @param queries
   * @return
   * @throws NotBoundException
   * @throws RemoteException
   */
  Boolean removeEntities(@NotNull RemoteEntityQuery[] queries)
      throws NotBoundException, RemoteException;

  Boolean saveProperty(@NotNull RemoteEntityProperty property)
      throws NotBoundException, RemoteException;

  Boolean removeProperty(@NotNull RemoteEntityProperty property)
      throws NotBoundException, RemoteException;

  Boolean removeEntityType(@NotNull RemoteEntityQuery query)
      throws NotBoundException, RemoteException;
}
