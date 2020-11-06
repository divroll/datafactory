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
import com.divroll.datafactory.GeoHash;
import com.divroll.datafactory.GeoPoint;
import com.divroll.datafactory.TestEnvironment;
import com.divroll.datafactory.builders.DataFactoryEntities;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import com.divroll.datafactory.repositories.EntityStore;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class PropertyNearbyConditionTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(PropertyNearbyConditionTest.class);

  @Test
  public void testNearbyCondition() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    DataFactoryEntity firstLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
        .putPropertyMap("geoLocation", new GeoPoint(120.976171, 14.580919))
        .build();
    firstLocation = entityStore.saveEntity(firstLocation).get();
    assertNotNull(firstLocation.entityId());
    DataFactoryEntity secondLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", new GeoPoint(121.016723, 14.511879))
        .build();
    secondLocation = entityStore.saveEntity(secondLocation).get();
    assertNotNull(secondLocation.entityId());
    DataFactoryEntity thirdLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", new GeoPoint(120.976619, 14.581578))
        .build();
    thirdLocation = entityStore.saveEntity(thirdLocation).get();
    assertNotNull(thirdLocation);
    long start = System.currentTimeMillis();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyNearbyConditionBuilder()
            .propertyName("geoLocation")
            .longitude(120.976187)
            .latitude(14.581310)
            .distance(100.0)
            .build())
        .build();
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    long time = System.currentTimeMillis() - start;
    LOG.info("Time to complete (ms): " + time);
    assertNotNull(entities);
    assertEquals(2, entities.entities().size());
  }

  @Test
  public void testNearbyConditionShouldBeZero() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    DataFactoryEntity dataFactoryEntity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", new GeoPoint(120.984293, 14.535238))
        .build();
    dataFactoryEntity = entityStore.saveEntity(dataFactoryEntity).get();
    assertNotNull(dataFactoryEntity.entityId());
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyNearbyConditionBuilder()
            .propertyName("geoLocation")
            .longitude(120.976187)
            .latitude(14.581310)
            .distance(200.0)
            .build())
        .build();
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    assertNotNull(entities);
    assertEquals(0, entities.entities().size());
  }

  @Test
  public void testNearbyConditionUseGeoHash() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    DataFactoryEntity firstLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
        .putPropertyMap("geoLocation", GeoHash.create(120.976171, 14.580919))
        .build();
    firstLocation = entityStore.saveEntity(firstLocation).get();
    assertNotNull(firstLocation.entityId());
    DataFactoryEntity secondLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", GeoHash.create(121.016723, 14.511879))
        .build();
    secondLocation = entityStore.saveEntity(secondLocation).get();
    assertNotNull(secondLocation.entityId());
    DataFactoryEntity thirdLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", GeoHash.create(120.976619, 14.581578))
        .build();
    thirdLocation = entityStore.saveEntity(thirdLocation).get();
    assertNotNull(thirdLocation);
    long start = System.currentTimeMillis();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyNearbyConditionBuilder()
            .propertyName("geoLocation")
            .longitude(120.976187)
            .latitude(14.581310)
            .distance(100.0)
            .useGeoHash(true)
            .build())
        .build();
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    long time = System.currentTimeMillis() - start;
    LOG.info("Time to complete (ms): " + time);
    assertNotNull(entities);
    assertEquals(2, entities.entities().size());
  }

  @Test
  public void testNearbyConditionUseGeoHashShouldBeZero() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    DataFactoryEntity dataFactoryEntity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", new GeoHash(120.984293, 14.535238).toString())
        .build();
    dataFactoryEntity = entityStore.saveEntity(dataFactoryEntity).get();
    assertNotNull(dataFactoryEntity.entityId());
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyNearbyConditionBuilder()
            .propertyName("geoLocation")
            .longitude(120.976187)
            .latitude(14.581310)
            .distance(200.0)
            .useGeoHash(true)
            .build())
        .build();
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    assertNotNull(entities);
    assertEquals(0, entities.entities().size());
  }

}
