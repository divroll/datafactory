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
package com.divroll.datafactory.repositories;

import com.divroll.datafactory.DataFactory;
import com.divroll.datafactory.TestEnvironment;
import com.divroll.datafactory.actions.ImmutableBlobRemoveAction;
import com.divroll.datafactory.actions.ImmutableBlobRenameAction;
import com.divroll.datafactory.actions.ImmutableBlobRenameRegexAction;
import com.divroll.datafactory.actions.ImmutableLinkAction;
import com.divroll.datafactory.builders.DataFactoryBlobBuilder;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.BlobQueryBuilder;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import com.divroll.datafactory.conditions.PropertyStartsWithConditionBuilder;
import com.divroll.datafactory.exceptions.UnsatisfiedConditionException;
import com.google.common.io.ByteSource;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EntityStoreTest {

  @Test
  public void testSaveEntity() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
  }

  @Test
  public void testSaveEntityWithBlobRemove() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    // Create with blob
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .addBlobs(new DataFactoryBlobBuilder()
            .blobName("message")
            .blobStream(
                new SimpleRemoteInputStream(ByteSource.wrap("Hello Word".getBytes()).openStream()))
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    // Get with blob
    entityStore.getEntity(new EntityQueryBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("message")
            .include(true)
            .build())
        .build()).ifPresent(saved -> {
      assertNotNull(saved);
      assertEquals(1, saved.blobNames().length);
      assertEquals("message", saved.blobNames()[0]);
      assertEquals(1, saved.blobs().size());
      saved.blobs().forEach(dataFactoryBlob -> {
        assertNotNull(dataFactoryBlob.blobStream());
        try {
          String blobString =
              IOUtils.toString(RemoteInputStreamClient.wrap(dataFactoryBlob.blobStream()), Charset
                  .defaultCharset());
          assertEquals("Hello Word", blobString);
        } catch (IOException e) {
          e.printStackTrace();
          fail();
        }
      });
    });
    // Delete blob
    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .entityId(dataFactoryEntity.entityId())
        .addActions(ImmutableBlobRemoveAction.builder()
            .blobNames(Arrays.asList("message"))
            .build())
        .build());
    // Get with blob, expecting blob is removed
    entityStore.getEntity(new EntityQueryBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("message")
            .include(true)
            .build())
        .build()).ifPresent(saved -> {
      assertNotNull(saved);
      assertEquals(0, Arrays.asList(saved.blobNames()).size());
      assertEquals(0, saved.blobs().size());
    });
  }

  @Test
  public void testSaveEntityWithBlobRename() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    // Create with blob
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .addBlobs(new DataFactoryBlobBuilder()
            .blobName("message")
            .blobStream(
                new SimpleRemoteInputStream(ByteSource.wrap("Hello Word".getBytes()).openStream()))
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    // Get with blob
    entityStore.getEntity(new EntityQueryBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("message")
            .include(true)
            .build())
        .build()).ifPresent(saved -> {
      assertNotNull(saved);
      assertEquals(1, saved.blobNames().length);
      assertEquals("message", saved.blobNames()[0]);
      assertEquals(1, saved.blobs().size());
      saved.blobs().forEach(dataFactoryBlob -> {
        assertNotNull(dataFactoryBlob.blobStream());
        try {
          String blobString =
              IOUtils.toString(RemoteInputStreamClient.wrap(dataFactoryBlob.blobStream()), Charset
                  .defaultCharset());
          assertEquals("Hello Word", blobString);
        } catch (IOException e) {
          e.printStackTrace();
          fail();
        }
      });
    });
    // Rename blob
    DataFactoryEntity entity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .entityId(dataFactoryEntity.entityId())
        .addActions(ImmutableBlobRenameAction.builder()
            .blobName("message")
            .newBlobName("theMessage")
            .build())
        .build();
    entity = entityStore.saveEntity(entity).get();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityId(entity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("message")
            .build(), new BlobQueryBuilder()
            .blobName("theMessage")
            .build())
        .build();
    entityStore.getEntity(entityQuery).ifPresent(updatedEntity -> {
      assertFalse(Arrays.asList(updatedEntity.blobNames()).contains("message"));
      assertEquals(1, updatedEntity.blobs().size());
      updatedEntity.blobs().forEach(dataFactoryBlob -> {
        try {
          String blobString =
              IOUtils.toString(RemoteInputStreamClient.wrap(dataFactoryBlob.blobStream()), Charset
                  .defaultCharset());
          assertEquals("Hello Word", blobString);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    });
  }

  @Test
  public void testSaveEntityWithBlobRenameRegex() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    // Create with blob
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .addBlobs(new DataFactoryBlobBuilder()
            .blobName("message")
            .blobStream(
                new SimpleRemoteInputStream(ByteSource.wrap("Should not be replaced".getBytes()).openStream()))
            .build())
        .addBlobs(new DataFactoryBlobBuilder()
            .blobName("123")
            .blobStream(
                new SimpleRemoteInputStream(ByteSource.wrap("Should be replaced".getBytes()).openStream()))
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    // Rename blob with regex
    DataFactoryEntity entity = new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .entityId(dataFactoryEntity.entityId())
        .addActions(ImmutableBlobRenameRegexAction.builder()
            .regexPattern("\\d+")
            .replacement("OneTwoThree")
            .build())
        .build();
    entity = entityStore.saveEntity(entity).get();
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityId(entity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("OneTwoThree")
            .build())
        .build();
    entityStore.getEntity(entityQuery).ifPresent(updatedEntity -> {
      assertEquals(2, updatedEntity.blobNames().length);
      assertEquals(1, updatedEntity.blobs().size());
      updatedEntity.blobs().forEach(dataFactoryBlob -> {
        try {
          String blobString =
              IOUtils.toString(RemoteInputStreamClient.wrap(dataFactoryBlob.blobStream()), Charset
                  .defaultCharset());
          assertEquals("Should be replaced", blobString);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    });
    entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityId(entity.entityId())
        .addBlobQueries(new BlobQueryBuilder()
            .blobName("message")
            .build())
        .build();
    entityStore.getEntity(entityQuery).ifPresent(updatedEntity -> {
      assertEquals(2, updatedEntity.blobNames().length);
      assertEquals(1, updatedEntity.blobs().size());
      updatedEntity.blobs().forEach(dataFactoryBlob -> {
        try {
          String blobString =
              IOUtils.toString(RemoteInputStreamClient.wrap(dataFactoryBlob.blobStream()), Charset
                  .defaultCharset());
          assertEquals("Should not be replaced", blobString);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    });
  }

  @Test
  public void testSaveEntityWithLink() throws Exception {

  }

  @Test
  public void testSaveEntityWithLinkNewEntity() throws Exception {

  }

  @Test
  public void testSaveEntityWithLinkRemove() throws Exception {

  }

  @Test
  public void testSaveEntityWithOppositeLink() throws Exception {

  }

  @Test
  public void testSaveEntityWithOppositeLinkRemove() throws Exception {

  }

  @Test
  public void testSaveEntityWithPropertyCopy() throws Exception {

  }

  @Test
  public void testSaveEntityWithPropertyIndex() throws Exception {

  }

  @Test
  public void testSaveEntityWithPropertyRemove() throws Exception {

  }

  @Test
  public void testSaveEntityWithPropertySave() throws Exception {

  }

  @Test
  public void testSaveEntityWithLinkCondition() throws Exception {

  }

  @Test
  public void testSaveEntityWithOppositeLinkCondition() throws Exception {

  }

  @Test
  public void testSaveEntityWithPropertyEqualCondition() throws Exception {

  }

  @Test
  public void testSaveEntityWithLocalTimeRangeCondition() throws Exception {

  }

  @Test
  public void testSaveEntityWithMinMaxCondition() throws Exception {

  }

  @Test
  public void testSaveEntityWithNearbyCondition() throws Exception {

  }

  @Test(expected = UnsatisfiedConditionException.class)
  public void testSaveEntityWithStartsWithShouldFail() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "fooBar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());

    dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .putPropertyMap("hasFooBaz", true)
        .addConditions(new PropertyStartsWithConditionBuilder()
            .propertyName("foo")
            .startsWith("fooBaz")
            .build())
        .build()).get();
  }

  @Test
  public void testSaveEntityWithStartsWith() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "fooBar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    // Update entity if property "starts with", else fail
    String entityId = dataFactoryEntity.entityId();
    dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .putPropertyMap("hasFooBar", true)
        .addConditions(new PropertyStartsWithConditionBuilder()
            .propertyName("foo")
            .startsWith("fooBar")
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    assertEquals(entityId, dataFactoryEntity.entityId());
    // Get entity
    EntityQuery entityQuery = new EntityQueryBuilder()
        .environment(environment)
        .entityId(entityId)
        .build();
    entityStore.getEntity(entityQuery).ifPresent(updatedEntity -> {
      assertEquals("fooBar", updatedEntity.propertyMap().get("foo"));
      assertEquals(true, updatedEntity.propertyMap().get("hasFooBar"));
    });
  }

  @Test
  public void testGetEntity() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
    EntityQuery query = new EntityQueryBuilder()
        .environment(environment)
        .entityId(dataFactoryEntity.entityId())
        .build();
    DataFactoryEntity savedEntity = entityStore.getEntity(query).get();
    assertNotNull(savedEntity);
    assertEquals(dataFactoryEntity.entityId(), savedEntity.entityId());
    assertEquals(dataFactoryEntity.propertyMap().get("foo"), savedEntity.propertyMap().get("foo"));
  }

  @Test(expected = EntityRemovedInDatabaseException.class)
  public void test() throws Exception {
    String environment = TestEnvironment.getEnvironment();
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .addActions(ImmutableLinkAction.builder()
            .linkName("baz")
            .otherEntityId("0-1000")
            .isSet(true)
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);
  }
}
