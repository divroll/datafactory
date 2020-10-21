package com.divroll.gravity.repositories;

import com.divroll.gravity.Gravity;
import com.divroll.gravity.builders.RemoteEntity;
import com.divroll.gravity.builders.RemoteEntityBuilder;
import com.divroll.gravity.builders.RemoteEntityQuery;
import com.divroll.gravity.builders.RemoteEntityQueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityStoreTest {
  @Test
  public void testEntityRepository() throws Exception {
    Gravity database = new Gravity();
    database.startDatabase();
    EntityStore entityStore = database.getEntityStore();
    assertNotNull(entityStore);
    RemoteEntity remoteEntity = entityStore.saveEntity(new RemoteEntityBuilder()
        .environment("/var/test/")
        .entityType("Foo")
        .putPropertyMap("foo", "bar")
        .build()).get();
    assertNotNull(remoteEntity);
    System.out.println(remoteEntity.entityId());
    RemoteEntityQuery query = new RemoteEntityQueryBuilder()
        .environment("/var/test/")
        .entityId(remoteEntity.entityId())
        .build();
    RemoteEntity savedEntity = entityStore.getEntity(query).get();
    assertNotNull(savedEntity);
    assertEquals(remoteEntity.entityId(), savedEntity.entityId());
    assertEquals(remoteEntity.propertyMap().get("foo"), savedEntity.propertyMap().get("foo"));
  }
}
