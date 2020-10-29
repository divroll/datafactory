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
package com.divroll.datafactory.conditions;

import com.divroll.datafactory.DataFactory;
import com.divroll.datafactory.TestEnvironment;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import com.divroll.datafactory.repositories.EntityStore;
import java.util.NoSuchElementException;
import jetbrains.exodus.bindings.ComparableSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class PropertyContainsConditionTest {

  @Test
  public void testContainsCondition() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    ComparableSet<String> keywords = new ComparableSet<>();
    keywords.addItem("room");
    keywords.addItem("street");
    keywords.addItem("avenue");
    DataFactoryEntity dataFactoryEntity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
        .putPropertyMap("keywords", keywords)
        .build();
    dataFactoryEntity = entityStore.saveEntity(dataFactoryEntity).get();
    assertNotNull(dataFactoryEntity.entityId());
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyContainsConditionBuilder<String>()
            .propertyName("keywords")
            .innerPropertyValue("room")
            .build())
        .build();
    DataFactoryEntity entityWithKeywords = entityStore.getEntity(entityQuery).get();
    assertNotNull(entityWithKeywords);
    assertEquals(dataFactoryEntity.entityId(), entityWithKeywords.entityId());
  }

  @Test(expected = NoSuchElementException.class)
  public void testContainsConditionShouldFail() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    ComparableSet<String> keywords = new ComparableSet<>();
    keywords.addItem("room");
    keywords.addItem("street");
    keywords.addItem("avenue");
    DataFactoryEntity dataFactoryEntity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
        .putPropertyMap("keywords", keywords)
        .build();
    dataFactoryEntity = entityStore.saveEntity(dataFactoryEntity).get();
    assertNotNull(dataFactoryEntity.entityId());
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyContainsConditionBuilder<String>()
            .propertyName("keywords")
            .innerPropertyValue("unknown")
            .build())
        .build();
    DataFactoryEntity entityWithKeywords = entityStore.getEntity(entityQuery).get();
    assertNull(entityWithKeywords);
  }
}
