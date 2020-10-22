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
package com.divroll.datafactory.properties;

import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class EmbeddedArrayIterable implements Serializable, ByteIterable {

  private byte[] bytes;

  public EmbeddedArrayIterable(List<Comparable> objects) {
    bytes = serialize(objects);
  }

  public static byte[] serialize(Object obj) {
    return Try.of(() -> {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream os = new ObjectOutputStream(out);
      os.writeObject(obj);
      return out.toByteArray();
    }).getOrNull();
  }

  public static Comparable deserialize(byte[] data) {
    return Try.of(() -> {
      ByteArrayInputStream in = new ByteArrayInputStream(data);
      ObjectInputStream is = new ObjectInputStream(in);
      return (Comparable) is.readObject();
    }).getOrNull();
  }

  @Override
  public ByteIterator iterator() {
    return new ArrayByteIterable(bytes).iterator();
  }

  @Override
  public byte[] getBytesUnsafe() {
    return bytes;
  }

  @Override
  public int getLength() {
    return bytes.length;
  }

  @NotNull
  @Override
  public ByteIterable subIterable(int offset, int length) {
    return null;
  }

  @Override
  public int compareTo(@NotNull ByteIterable o) {
    return 0;
  }

  public List<Comparable> asObject() {
    Comparable comparable = deserialize(bytes);
    return comparable instanceof List ? (List<Comparable>) comparable : null;
  }
}
