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
package com.divroll.datafactory.bindings;

import com.divroll.datafactory.LocalTimeRange;
import java.io.ByteArrayInputStream;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class LocalTimeRangeBinding extends ComparableBinding {

  public static final LocalTimeRangeBinding BINDING = new LocalTimeRangeBinding();

  @Override public Comparable readObject(@NotNull ByteArrayInputStream stream) {
    return BindingUtils.readObject(stream);
  }

  @Override public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {
    output.write(BindingUtils.writeObject(object));
  }

  /**
   * De-serializes {@linkplain ByteIterable} entry to a {@linkplain LocalTimeRange} value.
   *
   * @param entry {@linkplain ByteIterable} instance
   * @return de-serialized value
   */
  public static LocalTimeRange entryToLocalTimeRange(@NotNull final ByteIterable entry) {
    return (LocalTimeRange) BINDING.entryToObject(entry);
  }

  /**
   * Serializes {@linkplain LocalTimeRange} value to the {@linkplain ArrayByteIterable} entry.
   *
   * @param object value to serialize
   * @return {@linkplain ArrayByteIterable} entry
   */
  public static ArrayByteIterable localTimeRangeToEntry(final LocalTimeRange object) {
    return BINDING.objectToEntry(object);
  }
}
