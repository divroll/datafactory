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

import com.divroll.datafactory.database.DatabaseManager;
import com.divroll.datafactory.database.impl.DatabaseManagerImpl;
import com.divroll.datafactory.repositories.EntityStore;
import com.divroll.datafactory.repositories.impl.EntityStoreImpl;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import jetbrains.exodus.bindings.ComparableBinding;
import lombok.SneakyThrows;

/**
 * DataFactory registers and exposes the repositories for accessing the underlying Xodus database.
 * Directly through {@linkplain DatabaseManager} or remotely using the {@linkplain EntityStore}
 * provided by both {@linkplain DataFactory} and {@linkplain DataFactoryClient}
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DataFactory {

  private static final Logger LOG = LoggerFactory.getLogger(DataFactory.class);

  private EntityStore entityStore;

  private static DataFactory instance;
  private static Registry registry;

  private DataFactory() {
    if (instance != null) {
      throw new RuntimeException("Only one instance of DataFactory is allowed");
    }
  }

  /**
   * Get the singleton instance of {@linkplain DataFactory} and create regisry for Java RMI in the
   * process.
   *
   * @return singleton instance of {@linkplain DataFactory}
   */
  @SneakyThrows
  public static DataFactory getInstance() {
    if (instance == null) {
      instance = new DataFactory();
      System.setProperty("java.rmi.server.hostname", "localhost");
      String port = System.getProperty("java.rmi.server.port", "1099");
      if (port != null) {
        registry = LocateRegistry.createRegistry(Integer.valueOf(port));
      } else {
        registry = LocateRegistry.createRegistry(1099);
      }
      String process = ManagementFactory.getRuntimeMXBean().getName();
      LOG.debug("DataFactory initialized with process id: " + process);
    }
    return instance;
  }

  /**
   * Unbinds all registered classes from RMI registry and close all Xodus environments.
   */
  @SneakyThrows
  public void release() {
    if (registry != null) {
      String[] classNames = registry.list();
      for (int i = 0; i < classNames.length; i++) {
        registry.unbind(classNames[i]);
      }
    }
    DatabaseManagerImpl.getInstance().closeEnvironments();
  }

  public <T extends Comparable, B extends ComparableBinding> DataFactory addCustomPropertyType(
      String dir, final Class<T> clazz, final B binding) {
    DatabaseManagerImpl.getInstance().registerCustomPropertyType(dir, clazz, binding);
    return this;
  }

  public EntityStore getEntityStore() throws RemoteException, NotBoundException {
    if (entityStore == null) {
      entityStore = new EntityStoreImpl(DatabaseManagerImpl.getInstance());
    }
    if (!Arrays.asList(registry.list()).contains(EntityStore.class.getName())) {
      registry.rebind(EntityStore.class.getName(), entityStore);
    }
    return entityStore;
  }
}
