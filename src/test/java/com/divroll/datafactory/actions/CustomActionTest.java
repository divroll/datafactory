/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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
package com.divroll.datafactory.actions;

import com.divroll.datafactory.DataFactory;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import com.divroll.datafactory.repositories.EntityStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class CustomActionTest {

  private static final String TEST_ENVIRONMENT = "/var/test/";

  @Test
  public void testCustomAction() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(TEST_ENVIRONMENT)
        .entityType("Room")
        .putPropertyMap("likes", 10)
        .build()).get();
    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(TEST_ENVIRONMENT)
        .entityId(dataFactoryEntity.entityId())
        .addActions(new IncrementLikesAction(2990))
        .build());
    DataFactoryEntity updatedEntity = entityStore.getEntity(new EntityQueryBuilder()
        .environment(TEST_ENVIRONMENT)
        .entityId(dataFactoryEntity.entityId())
        .build()).get();
    assertEquals(3000, updatedEntity.propertyMap().get("likes"));
  }
}
