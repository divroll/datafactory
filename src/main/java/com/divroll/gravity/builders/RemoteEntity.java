/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2020, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.divroll.gravity.builders;

import com.divroll.gravity.actions.EntityAction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface RemoteEntity extends Serializable {

  @Value.Default
  default String environment() {
    return System.getProperty("xodusRoot");
  }

  String entityType();

  @Nullable
  String nameSpace();

  @Nullable
  String entityId();

  @Value.Default
  default Map<String, Comparable> propertyMap() {
    return new LinkedHashMap<>();
  }

  @Nullable
  @Value.Default
  default List<RemoteBlob> blobs() {
    return new ArrayList<>();
  }

  @Nullable
  @Value.Default
  default List<RemoteEntity> links() {
    return new ArrayList<>();
  }

  @Nullable
  @Value.Default
  default String[] blobNames() {
    return null;
  }

  @Nullable
  @Value.Default
  default String[] linkNames() {
    return null;
  }

  @Nullable
  @Value.Default
  default List<EntityAction> entityActions() {
    return new ArrayList<>();
  }

  @Nullable
  @Value.Default
  default List<RemoteTransactionFilter> filters() {
    return new ArrayList<>();
  }
}
