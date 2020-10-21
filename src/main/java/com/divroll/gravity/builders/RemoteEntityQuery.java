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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface RemoteEntityQuery extends Serializable {

  @Nullable
  @Value.Default
  default String environment() {
    return System.getProperty("xodusRoot");
  }

  String nameSpace();

  String entityType();

  @Nullable
  String entityId();

  /**
   * Indicates the {@code linkNames} to use in the query
   *
   * @return names of the links of {@linkplain RemoteEntity} to query
   */
  @Nullable
  @Value.Default
  default List<LinkQuery> linkQueries() {
    return new ArrayList<>();
  }

  /**
   * Indicates the {@code blobNames} to use in the query
   *
   * @return names of the blobs of {@linkplain RemoteEntity} to query
   */
  @Nullable
  @Value.Default
  default List<BlobQuery> blobQueries() {
    return new ArrayList<>();
  }

  /**
   * Indicates the query should return only the first entity found
   *
   * @return
   */
  @Nullable
  @Value.Default
  default Boolean first() {
    return false;
  }

  /**
   * Indicates the query should return only the last entity found
   *
   * @return
   */
  @Nullable
  @Value.Default
  default Boolean last() {
    return false;
  }

  @Nullable
  @Value.Default
  default List<RemoteTransactionFilter> transactionFilters() {
    return new ArrayList<>();
  }

  Integer offset();

  Integer max();

  @Nullable
  String sortAscending();

  @Nullable
  String sortDescending();
}
