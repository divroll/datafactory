package com.divroll.datafactory.conditions;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface PropertyStartsWithCondition extends EntityCondition {
  String propertyName();
  String startsWith();
}
