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

import com.google.common.io.ByteStreams;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class BindingUtils {
  private BindingUtils() {
  }

  public static Comparable readObject(@NotNull ByteArrayInputStream stream) {
    return Try.of(() -> {
      byte[] serialized = ByteStreams.toByteArray(stream);
      return deserialize(serialized, Comparable.class);
    }).getOrNull();
  }

  public static byte[] writeObject(@NotNull Comparable object) {
    byte[] serialized = serialize(object);
    return serialized;
  }

  public static <T> T deserialize(@NotNull byte[] data, Class<T> clazz) {
    try {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream is = new ObjectInputStream(in);
      Object readObject = is.readObject();
      return clazz.isInstance(readObject)
          ? (T) readObject : null;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static byte[] serialize(@NotNull Object obj) {
    return Try.of(() -> {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bos);
      out.writeObject(obj);
      return bos.toByteArray();
    }).getOrNull();
  }
}
