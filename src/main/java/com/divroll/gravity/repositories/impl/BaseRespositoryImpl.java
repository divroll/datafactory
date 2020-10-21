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
package com.divroll.gravity.repositories.impl;

import com.divroll.gravity.properties.EmbeddedArrayIterable;
import com.divroll.gravity.properties.EmbeddedEntityIterable;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import util.ComparableLinkedList;
import util.CustomHashMap;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public abstract class BaseRespositoryImpl<T> extends UnicastRemoteObject {

  private static final Logger LOG = LoggerFactory.getLogger(BaseRespositoryImpl.class);

  public BaseRespositoryImpl() throws RemoteException {
  }

  public static <T> void removeDuplicates(ComparableLinkedList<T> list) {
    int size = list.size();
    int out = 0;
    {
      final Set<T> encountered = new HashSet<T>();
      for (int in = 0; in < size; in++) {
        final T t = list.get(in);
        final boolean first = encountered.add(t);
        if (first) {
          list.set(out++, t);
        }
      }
    }
    while (out < size) {
      list.remove(--size);
    }
  }

  public static <T> void removeDuplicates(ArrayList<T> list) {
    int size = list.size();
    int out = 0;
    {
      final Set<T> encountered = new HashSet<T>();
      for (int in = 0; in < size; in++) {
        final T t = list.get(in);
        final boolean first = encountered.add(t);
        if (first) {
          list.set(out++, t);
        }
      }
    }
    while (out < size) {
      list.remove(--size);
    }
  }

  protected static CustomHashMap<String, Comparable> entityToMap(
      jetbrains.exodus.entitystore.Entity entity) {
    CustomHashMap<String, Comparable> comparableMap = new CustomHashMap<>();
    for (String property : entity.getPropertyNames()) {
      Comparable value = entity.getProperty(property);
      if (value != null) {
        if (value instanceof EmbeddedEntityIterable) {
          comparableMap.put(property, ((EmbeddedEntityIterable) value).asObject());
        } else if (value instanceof EmbeddedArrayIterable) {
          comparableMap.put(
              property, (Comparable) ((EmbeddedArrayIterable) value).asObject());
        } else {
          comparableMap.put(property, value);
        }
      }
    }
    return comparableMap;
  }

  protected String getISODate() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df =
        new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    return df.format(new Date());
  }

  //protected String getXodusRoot(String entityId) throws NotBoundException, RemoteException {
  //  if(entityId == null) {
  //    throw new IllegalArgumentException("Entity ID must not be null");
  //  }
  //  return partition.getPartitionByEntityId(entityId);
  //}

  protected String getXodusRoot() {
    return System.getProperty("xodusRoot");
  }

}
