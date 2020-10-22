package com.divroll.datafactory.repositories;

import com.divroll.datafactory.DataFactory;
import com.divroll.datafactory.actions.ImmutableLinkAction;
import com.divroll.datafactory.builders.DataFactoryEntity;
import com.divroll.datafactory.builders.queries.EntityQuery;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import jetbrains.exodus.entitystore.EntityRemovedInDatabaseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityStoreTest {
  @Test
  public void testSaveEntity() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment("/var/test/")
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    assertNotNull(dataFactoryEntity.entityId());
  }
  @Test
  public void testGetEntity() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment("/var/test/")
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    assertNotNull(dataFactoryEntity);
    System.out.println(dataFactoryEntity.entityId());
    EntityQuery query = new EntityQueryBuilder()
        .environment("/var/test/")
        .entityId(dataFactoryEntity.entityId())
        .build();
    DataFactoryEntity savedEntity = entityStore.getEntity(query).get();
    assertNotNull(savedEntity);
    assertEquals(dataFactoryEntity.entityId(), savedEntity.entityId());
    assertEquals(dataFactoryEntity.propertyMap().get("foo"), savedEntity.propertyMap().get("foo"));
  }
  @Test(expected = EntityRemovedInDatabaseException.class)
  public void test() throws Exception {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    assertNotNull(entityStore);
    DataFactoryEntity dataFactoryEntity = entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment("/var/test/")
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .addEntityActions(ImmutableLinkAction.builder()
            .linkName("baz")
            .otherEntityId("0-1000")
            .isSet(true)
            .build())
        .build()).get();
    assertNotNull(dataFactoryEntity);

  }
}
