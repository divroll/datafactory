package com.divroll.datafactory.conditions;

import com.divroll.datafactory.DataFactory;
import com.divroll.datafactory.LocalTimeRange;
import com.divroll.datafactory.TestEnvironment;
import com.divroll.datafactory.builders.DataFactoryEntities;
import com.divroll.datafactory.builders.DataFactoryEntityBuilder;
import com.divroll.datafactory.builders.queries.EntityQueryBuilder;
import com.divroll.datafactory.repositories.EntityStore;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;

public class PropertyLocalTimeRangeConditionTest {
  @Test
  public void testInRangeCondition() throws RemoteException, NotBoundException {
    EntityStore entityStore = DataFactory.getInstance().getEntityStore();
    String environment = TestEnvironment.getEnvironment();
    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("SUNDAY",
            new LocalTimeRange(LocalTime.parse("07:00"), LocalTime.parse("18:00")))
        .build()).get();

    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("SUNDAY",
            new LocalTimeRange(LocalTime.parse("08:00"), LocalTime.parse("22:00")))
        .build()).get();

    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("SUNDAY",
            new LocalTimeRange(LocalTime.parse("01:00"), LocalTime.parse("06:00")))
        .build()).get();

    entityStore.saveEntity(new DataFactoryEntityBuilder()
        .environment(environment)
        .entityType("Room")
        .putPropertyMap("MONDAY",
            new LocalTimeRange(LocalTime.parse("07:00"), LocalTime.parse("23:59")))
        .build()).get();

    DataFactoryEntities dataFactoryEntities = entityStore.getEntities(new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyLocalTimeRangeConditionBuilder()
            .propertyName("SUNDAY")
            .lower(LocalTime.parse("10:00"))
            .upper(LocalTime.parse("11:00"))
            .build())
        .build()).get();
    Assert.assertNotNull(dataFactoryEntities);
    Assert.assertNotNull(dataFactoryEntities.entities());
    Assert.assertEquals(2L, dataFactoryEntities.count().longValue());

    DataFactoryEntities dataFactoryEntities2 = entityStore.getEntities(new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyLocalTimeRangeConditionBuilder()
            .propertyName("MONDAY")
            .lower(LocalTime.parse("19:00"))
            .upper(LocalTime.parse("20:00"))
            .build())
        .build()).get();
    Assert.assertNotNull(dataFactoryEntities2);
    Assert.assertNotNull(dataFactoryEntities2.entities());
    Assert.assertEquals(1L, dataFactoryEntities2.count().longValue());

    DataFactoryEntities dataFactoryEntities3 = entityStore.getEntities(new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyLocalTimeRangeConditionBuilder()
            .propertyName("MONDAY")
            .lower(LocalTime.parse("01:00"))
            .upper(LocalTime.parse("06:00"))
            .build())
        .build()).get();
    Assert.assertNotNull(dataFactoryEntities3);
    Assert.assertNotNull(dataFactoryEntities3.entities());
    Assert.assertEquals(0L, dataFactoryEntities3.count().longValue());

    DataFactoryEntities dataFactoryEntities4 = entityStore.getEntities(new EntityQueryBuilder()
        .environment(environment)
        .entityType("Room")
        .addConditions(new PropertyLocalTimeRangeConditionBuilder()
            .propertyName("MONDAY")
            .lower(LocalTime.parse("07:00"))
            .upper(LocalTime.parse("23:59"))
            .build())
        .build()).get();
    Assert.assertNotNull(dataFactoryEntities4);
    Assert.assertNotNull(dataFactoryEntities4.entities());
    Assert.assertEquals(1L, dataFactoryEntities4.count().longValue());
  }
}
