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
package com.divroll.gravity;

import com.divroll.gravity.registry.Registrar;
import com.divroll.gravity.registry.impl.RegistrarImpl;
import com.divroll.gravity.repositories.EntityStore;
import com.divroll.gravity.repositories.impl.EntityStoreImpl;
import com.divroll.gravity.database.impl.DatabaseManagerImpl;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import jetbrains.exodus.bindings.ComparableBinding;
import lombok.SneakyThrows;

/**
 * Database registers and exposes the repositories for accessing the underlying Xodus database.
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Gravity {
  private static final Logger LOG = LoggerFactory.getLogger(Gravity.class);

  Registrar registrar;

  EntityStore entityStore;

  @SneakyThrows
  public void startDatabase() throws NotBoundException, RemoteException {
    entityStore = new EntityStoreImpl(DatabaseManagerImpl.getInstance());
    registrar = new RegistrarImpl(entityStore);
    String process = ManagementFactory.getRuntimeMXBean().getName();
    registrar.register();
    LOG.debug("Started database with process id: " + process);
  }

  @SneakyThrows
  public void stopDatabase() throws NotBoundException, RemoteException {
    if(registrar == null) {
      LOG.error("Database has not started");
    } else {
      registrar.unregister();
    }
  }

  public <T extends Comparable, B extends ComparableBinding> void registerCustomPropertyType(
      String dir, final Class<T> clazz, final B binding) {
    DatabaseManagerImpl.getInstance().registerCustomPropertyType(dir, clazz, binding);
  }

  public EntityStore getEntityStore() {
    if(registrar == null) {
      throw new RuntimeException("Database has not started");
    }
    return this.entityStore;
  }
}
