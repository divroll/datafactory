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
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class PropertyNearbyConditionPerfTest {

  private static final Logger LOG = LoggerFactory.getLogger(PropertyNearbyConditionPerfTest.class);

  @Test
  public void testPropertyNearbyCondition() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    String environment = TestEnvironment.getEnvironment();
    long start = System.currentTimeMillis();

    for (int i = 0; i < 999; i++) {
      entityStore.saveEntity(new DataFactoryEntityBuilder()
          .environment(environment)
          .entityType("Room")
          .putPropertyMap("geoLocation", new GeoPoint(120.954228, 14.301893))
          .build()).get();
    }

    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("geoLocation", new GeoPoint(120.976187, 14.581310))
        .build()).get();

    DataFactoryEntities dataFactoryEntities =
        entityStore.getEntities(new EntityQueryBuilder()
            .environment(environment)
            .entityType("Room")
            .max(10000)
            .build()).get();

    Assert.assertEquals(1000L, dataFactoryEntities.count().longValue());

    long time = System.currentTimeMillis() - start;
    LOG.info("Time to save complete (ms): " + time);
    start = System.currentTimeMillis();
    List<DataFactoryEntity> matched = new ArrayList<>();
    List<Long> times = new ArrayList<>();

    boolean hasMore = true;
    int offset = 0;
    int max = 100;
    int loopCount = 0;
    while (true) {
      EntityQuery entityQuery = new EntityQueryBuilder()
          .environment(environment)
          .entityType("Room")
          .addConditions(new PropertyNearbyConditionBuilder()
              .propertyName("geoLocation")
              .longitude(120.976187)
              .latitude(14.581310)
              .distance(100.0)
              .build())
          .offset(offset)
          .max(max)
          .build();
      DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
      matched.addAll(entities.entities());
      time = System.currentTimeMillis() - start;
      times.add(time);
      loopCount++;
      offset = loopCount * max;
      hasMore = ((offset * max) < entities.count());
      if (!hasMore) {
        break;
      }
    }
    times.forEach(aLong -> {
      LOG.info("Time to query complete (ms): " + aLong);
    });
    assertEquals(1L, loopCount);
    assertEquals(1L, matched.size());
  }


  @Test
  public void testNearbyConditionWithGeoHash() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();

    DataFactoryEntity firstLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 123, 456 Street, 789 Avenue")
        .putPropertyMap("geoLocation", new GeoHash(120.976171, 14.580919).toString())
        .build();
    firstLocation = entityStore.saveEntity(firstLocation).get();
    assertNotNull(firstLocation.entityId());
    for(int i=0;i<10000;i++){
      DataFactoryEntity secondLocation = new DataFactoryEntityBuilder()
          .environment(environment)
          .entityType("Room")
          .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
          .putPropertyMap("geoLocation", new GeoHash(121.016723, 14.511879).toString())
          .build();
      secondLocation = entityStore.saveEntity(secondLocation).get();
      //assertNotNull(secondLocation.entityId());
    }
    DataFactoryEntity thirdLocation = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("address", "Room 456, 789 Street, 012 Avenue")
        .putPropertyMap("geoLocation", new GeoHash(120.976619, 14.581578).toString())
        .build();
    thirdLocation = entityStore.saveEntity(thirdLocation).get();
    assertNotNull(thirdLocation);
    long start = System.currentTimeMillis();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyNearbyConditionBuilder()
            .propertyName("geoLocation")
            .longitude(120.976619)
            .latitude(14.581578)
            .distance(100.00)
            .useGeoHash(true)
            .build())
        .build();
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    long time = System.currentTimeMillis() - start;
    LOG.info("Time to complete query (ms): " + time);
    assertNotNull(entities);
    assertEquals(2, entities.entities().size());
  }


}
