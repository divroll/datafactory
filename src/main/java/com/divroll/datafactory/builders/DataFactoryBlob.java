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
package com.divroll.datafactory.builders;

import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.Serializable;
import javax.annotation.Nullable;
import jetbrains.exodus.entitystore.Entity;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public interface DataFactoryBlob extends Serializable {
  String blobName();

  RemoteInputStream blobStream();

  /**
   * Indicates that this blob can be {@code set} to multiple {@linkplain Entity}. If used for a
   * delete operation this property indicates whether to delete the blob from multiple {@linkplain
   * Entity} matching the query.
   *
   * @return
   */
  @Nullable
  @Value.Default
  default Boolean allowMultiple() {
    return false;
  }

  @Nullable
  Long count();

}
