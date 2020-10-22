package com.divroll.datafactory.conditions;

import java.time.LocalTime;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface PropertyLocalTimeRangeCondition {
  String propertyName();
  LocalTime upper();
  LocalTime lower();
}
