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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.ByteIterator;
import jetbrains.exodus.util.ByteIterableUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ByteValueIterable implements Serializable, ByteIterable {

  private final long offset;
  private final int length;
  private ByteValue byteValue;
  private byte[] bytes;

  public ByteValueIterable(ByteValue byteValue) {
    byte[] bytes = toByteArray(byteValue);
    this.bytes = bytes;
    this.byteValue = byteValue;
    this.offset = 0L;
    this.length = bytes.length;
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
    return length;
  }

  @NotNull
  @Override
  public ByteIterable subIterable(int offset, int length) {
    throw new UnsupportedOperationException("subIterable");
  }

  @Override
  public int compareTo(@NotNull ByteIterable o) {
    return ByteIterableUtil.compare(this, o);
  }

  protected byte[] toByteArray(Object object) {
    try {
      try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
           ObjectOutput out = new ObjectOutputStream(bos)) {
        out.writeObject(object);
        return bos.toByteArray();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
