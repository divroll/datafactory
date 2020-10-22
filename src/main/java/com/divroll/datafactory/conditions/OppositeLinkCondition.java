package com.divroll.datafactory.conditions;

import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface OppositeLinkCondition extends EntityCondition {
  String linkName();

  String oppositeLinkName();

  String oppositeEntityId();

  @Nullable
  @Value.Default
  default Boolean isSet() {
    return false;
  }
}
