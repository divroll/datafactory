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

import com.divroll.datafactory.repositories.EntityStore;
import com.divroll.datafactory.repositories.impl.EntityStoreClientImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DataFactoryClient {

  private static DataFactoryClient instance;
  private static final String DEFAULT_HOST = "localhost";
  private static final String DEFAULT_PORT = "1099";
  private EntityStore entityStore;

  private DataFactoryClient() {
  }

  public DataFactoryClient(EntityStore entityStore) {
    this.entityStore = entityStore;
  }

  public static DataFactoryClient getInstance() {
    String host = System.getProperty(Constants.JAVA_RMI_HOST_ENVIRONMENT);
    if (host == null) {
      host = System.setProperty(Constants.JAVA_RMI_HOST_ENVIRONMENT, DEFAULT_HOST);
    }
    String port =
        System.getProperty(Constants.JAVA_RMI_PORT_ENVIRONMENT, Constants.JAVA_RMI_PORT_DEFAULT);
    return getInstance(host, port);
  }

  public static DataFactoryClient getInstance(@NotNull String host, @NotNull String port) {
    EntityStore entityStore = new EntityStoreClientImpl(host != null ? host : DEFAULT_HOST,
        Integer.valueOf(port != null ? port : DEFAULT_PORT));
    DataFactoryClient instance = new DataFactoryClient(entityStore);
    return instance;
  }

  public EntityStore getEntityStore() {
    return entityStore;
  }
}
