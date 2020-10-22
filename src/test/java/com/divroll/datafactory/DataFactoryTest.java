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
package com.divroll.datafactory;

import com.divroll.datafactory.bindings.EmbeddedEntityBinding;
import com.divroll.datafactory.properties.EmbeddedArrayIterable;
import com.divroll.datafactory.properties.EmbeddedEntityIterable;
import com.divroll.datafactory.repositories.EntityStore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DataFactoryTest {
  @Test
  public void testGetInstance() throws Exception {
    DataFactory.getInstance();
  }

  @Test
  public void testRelease() throws Exception {
    //DataFactory..getInstance().release();
  }

  @Test
  public void testRegisterCustomPropertyTypes() throws Exception {
    DataFactory.getInstance()
        .addCustomPropertyType("/var/test/", EmbeddedArrayIterable.class,
            EmbeddedEntityBinding.BINDING)
        .addCustomPropertyType("/var/test/", EmbeddedEntityIterable.class,
            EmbeddedEntityBinding.BINDING);
  }

  @Test
  public void testGetEntityStore() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
  }
}
