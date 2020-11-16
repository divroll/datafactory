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
public class PropertyEqualConditionPerfTest {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyEqualConditionPerfTest.class);

  @Test
  public void testPropertyEqualCondition() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    String environment = TestEnvironment.getEnvironment();
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
      entityStore.saveEntity(new DataFactoryEntityBuilder()
          .environment(environment)
          .entityType("Foo")
          .putPropertyMap("foo", "bar")
          .build()).get();
    }
    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("baz", "fuu")
        .build()).get();
    long time = System.currentTimeMillis() - start;
    start = System.currentTimeMillis();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityType("Foo")
        .addConditions(new PropertyEqualConditionBuilder()
            .propertyName("baz")
            .propertyValue("fuu")
            .build())
        .build();
    //System.out.println(new Gson().toJson(entityQuery));
    DataFactoryEntities entities = entityStore.getEntities(entityQuery).get();
    time = System.currentTimeMillis() - start;
    LOG.info("Time to get complete (ms): " + time);
    assertEquals(1L, entities.entities().size());
  }
}
