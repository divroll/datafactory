package com.divroll.datafactory.conditions;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface PropertyNearbyCondition {
  String propertyName();
  Double longitude();
  Double latitude();
}
