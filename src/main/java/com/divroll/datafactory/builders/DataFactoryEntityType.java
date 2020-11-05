package com.divroll.datafactory.builders;

import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface DataFactoryEntityType {
  String entityTypeName();
}
