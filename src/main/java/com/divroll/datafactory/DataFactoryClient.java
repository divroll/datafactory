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

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
import com.divroll.datafactory.repositories.EntityStore;
import com.divroll.datafactory.repositories.impl.EntityStoreClientImpl;

public class DataFactoryClient {

  private static DataFactoryClient instance;
  private static final Integer DEFAULT_PORT = 1099;
  private EntityStore entityStore;

  private DataFactoryClient() {
    if (instance != null) {
      throw new RuntimeException("Only one instance of DataFactoryClient is allowed");
    }
  }

  public DataFactoryClient getInstance() {
    if(instance == null) {
      instance = new DataFactoryClient();
      entityStore = new EntityStoreClientImpl("localhost", DEFAULT_PORT);
    }
    return instance;
  }

  public EntityStore getEntityStore() {
    return entityStore;
  }

}
