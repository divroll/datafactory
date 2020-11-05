package com.divroll.datafactory.builders;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface DataFactoryEntityTypes {
  @Value.Default
  default List<DataFactoryEntityType> entityTypes() {
    return new ArrayList<>();
  }

  @Nullable
  @Value.Default
  default Long entityCount() {
    return -1L;
  }
}
