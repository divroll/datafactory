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
package com.divroll.gravity.registry.impl;

import com.divroll.gravity.registry.Registrar;
import com.divroll.gravity.repositories.EntityStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class RegistrarImpl implements Registrar {

  private static final Logger LOG = LoggerFactory.getLogger(RegistrarImpl.class);

  EntityStore entityStore;

  static Registry registry;

  public RegistrarImpl(EntityStore entityStore) throws RemoteException {
    this.entityStore = entityStore;
  }

  @Override
  public void register() throws RemoteException {
    System.setProperty("java.rmi.server.hostname", "localhost");
    if (registry == null) {
      registry = LocateRegistry.createRegistry(1099);
    }
    registry.rebind(EntityStore.class.getName(), entityStore);
    Arrays.asList(registry.list()).forEach(className -> {
      System.out.println(className + " binding success");
    });
  }

  @Override
  public void unregister() throws RemoteException {
    if(registry != null) {
      Arrays.asList(registry.list()).forEach(className -> {
        try {
          registry.unbind(className);
        } catch (Exception e) {
          LOG.error(e.getMessage());
        }
      });
    }
  }
}
