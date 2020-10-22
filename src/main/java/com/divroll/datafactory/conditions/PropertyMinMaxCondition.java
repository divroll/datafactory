package com.divroll.datafactory.conditions;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface PropertyMinMaxCondition extends EntityCondition {
  String propertyName();
  Comparable minValue();
  Comparable maxValue();
}
