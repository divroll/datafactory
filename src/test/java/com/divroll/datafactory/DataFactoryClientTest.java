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

import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.repositories.EntityStore;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataFactoryClientTest {
  @Test
  public void testGetInstance() throws Exception {
    DataFactory dataFactory = DataFactory.getInstance();
    DataFactoryClient client = DataFactoryClient.getInstance("localhost", "1099");
    EntityStore entityStore = client.getEntityStore();
    Assert.assertNotNull(entityStore);
  }

  @Test
  public void testSimpleSave() throws Exception {
    DataFactory dataFactory = DataFactory.getInstance();
    await().atMost(5, TimeUnit.SECONDS);
    DataFactoryClient client = DataFactoryClient.getInstance("localhost", "1099");
    EntityStore entityStore = client.getEntityStore();
    Assert.assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(TestEnvironment.getEnvironment())
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    Assert.assertNotNull(dataFactoryEntity);
    Assert.assertNotNull(dataFactoryEntity.entityId());
    Assert.assertEquals("bar", dataFactoryEntity.propertyMap().get("foo"));
  }

}
