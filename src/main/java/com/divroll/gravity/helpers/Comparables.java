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
package com.divroll.gravity.helpers;

import com.divroll.gravity.properties.EmbeddedArrayIterable;
import com.divroll.gravity.properties.EmbeddedEntityIterable;
import java.util.List;
import java.util.Map;
import util.ComparableHashMap;
import util.ComparableLinkedList;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class Comparables {
  public static <V extends Comparable> ComparableLinkedList<Comparable> cast(
      List<V> comparableList) {
    if (comparableList == null) {
      return null;
    }
    if (comparableList.isEmpty()) {
      return new ComparableLinkedList<Comparable>();
    }
    ComparableLinkedList<Comparable> casted = new ComparableLinkedList<Comparable>();
    comparableList.forEach(
        comparable -> {
          if (comparable instanceof EmbeddedEntityIterable) {
            casted.add(((EmbeddedEntityIterable) comparable).asObject());
          } else if (comparable instanceof EmbeddedArrayIterable) {
            casted.add(cast(((EmbeddedArrayIterable) comparable).asObject()));
          } else {
            casted.add(comparable);
          }
        });
    return casted;
  }

  public static <K extends Comparable, V extends Comparable> ComparableHashMap<K, V> cast(
      Map<K, V> map) {
    if (map == null) {
      return null;
    }
    if (map.isEmpty()) {
      return new ComparableHashMap<>();
    }
    ComparableHashMap<K, V> casted = new ComparableHashMap<>();
    map.forEach(
        (key, value) -> {
          casted.put(key, value);
        });
    return casted;
  }
}
