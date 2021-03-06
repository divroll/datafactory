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
package com.divroll.datafactory.database.impl;

import com.divroll.datafactory.GeoPoint;
import com.divroll.datafactory.LocalTimeRange;
import com.divroll.datafactory.bindings.EmbeddedEntityBinding;
import com.divroll.datafactory.bindings.GeoPointBinding;
import com.divroll.datafactory.bindings.LocalTimeBinding;
import com.divroll.datafactory.bindings.LocalTimeRangeBinding;
import com.divroll.datafactory.database.DatabaseManager;
import com.divroll.datafactory.exceptions.ThrowingConsumer;
import com.divroll.datafactory.properties.EmbeddedArrayIterable;
import com.divroll.datafactory.properties.EmbeddedEntityIterable;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStoreConfig;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.entitystore.StoreTransactionalExecutable;
import jetbrains.exodus.env.ContextualEnvironment;
import jetbrains.exodus.env.ContextualStore;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.EnvironmentConfig;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.env.TransactionalExecutable;
import jetbrains.exodus.lucene.ExodusDirectory;
import jetbrains.exodus.lucene.ExodusDirectoryConfig;
import jetbrains.exodus.vfs.VfsConfig;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public final class DatabaseManagerImpl implements DatabaseManager {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseManagerImpl.class);
  private static final int DEFAULT_LOCK_TIMEOUT = 30000;
  private static final String DEFAULT_ENTITYSTORE_NAME = "persistentEntityStore";

  Map<String, ContextualEnvironment> contextualEnvironmentMap;
  Map<String, ExodusDirectory> exodusDirectoryMap;
  Map<String, Environment> environmentMap;
  Map<String, PersistentEntityStore> entityStoreMap;

  private static DatabaseManagerImpl instance;

  private DatabaseManagerImpl() {
    if (instance != null) {
      throw new RuntimeException("Only one instance of DatabaseManager is allowed");
    }
    entityStoreMap = new HashMap<>();
    environmentMap = new HashMap<>();
    contextualEnvironmentMap = new HashMap<>();
    exodusDirectoryMap = new HashMap<>();
  }

  public static DatabaseManagerImpl getInstance() {
    if (instance == null) {
      instance = new DatabaseManagerImpl();
    }
    return instance;
  }

  @Override
  public Environment getEnvironment(String dir, boolean isContextual) {
    Environment environment = environmentMap.get(dir);
    if (environment == null) {
      EnvironmentConfig config = new EnvironmentConfig();
      config.setLogCacheShared(false);
      config.setEnvCloseForcedly(true);
      config.setManagementEnabled(false);
      config.setLogLockTimeout(DEFAULT_LOCK_TIMEOUT);
      environment = deleteLockingProcessAndGetEnvironment(dir, config, isContextual);
      environmentMap.put(dir, environment);
    }
    return environment;
  }

  @Override
  public PersistentEntityStore getPersistentEntityStore(String dir,
      boolean isReadOnly) {
    return getPersistentEntityStore(dir, DEFAULT_ENTITYSTORE_NAME, isReadOnly);
  }

  @Override public PersistentEntityStore getPersistentEntityStore(String dir, String storeName,
      boolean isReadOnly) {
    PersistentEntityStore entityStore = entityStoreMap.get(dir);
    if (entityStore == null) {
      Environment environment = getEnvironment(dir, false);
      final PersistentEntityStoreConfig config = new PersistentEntityStoreConfig()
          .setRefactoringHeavyLinks(true)
          .setDebugSearchForIncomingLinksOnDelete(true)
          .setManagementEnabled(false);
      entityStore =
          PersistentEntityStores.newInstance(config, environment,
              storeName != null ? storeName : DEFAULT_ENTITYSTORE_NAME);
      entityStore.executeInTransaction(
          txn -> {
            ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(
                txn, EmbeddedEntityIterable.class, EmbeddedEntityBinding.BINDING);
            ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(
                txn, EmbeddedArrayIterable.class, EmbeddedEntityBinding.BINDING);
            ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(
                txn, LocalTime.class, LocalTimeBinding.BINDING);
            ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(
                txn, GeoPoint.class, GeoPointBinding.BINDING);
            ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(
                txn, LocalTimeRange.class, LocalTimeRangeBinding.BINDING);
            entityStoreMap.put(dir,
                ((PersistentEntityStore) txn.getStore()));
          });
    }
    return entityStore;
  }

  @Override public ExodusDirectory getExodusDirectory(String dir) {
    ContextualEnvironment env = (ContextualEnvironment) getEnvironment(dir, true);
    ExodusDirectory exodusDirectory = exodusDirectoryMap.get(dir);
    if(exodusDirectory == null) {
      exodusDirectory = new ExodusDirectory(env, VfsConfig.DEFAULT, StoreConfig.WITHOUT_DUPLICATES_WITH_PREFIXING, new ExodusDirectoryConfig());
      exodusDirectoryMap.put(dir, exodusDirectory);
    }
    return exodusDirectoryMap.get(dir);
  }

  @Override
  public void transactPersistentEntityStore(String dir, boolean isReadOnly,
      StoreTransactionalExecutable txn) {
    final PersistentEntityStore entityStore =
        getPersistentEntityStore(dir, isReadOnly);
    entityStore.executeInTransaction(txn);
  }

  @Override public void transactionContextualStore(String dir, String store, StoreTransactionalExecutable tx) {
    final ContextualEnvironment environment = (ContextualEnvironment) getEnvironment(dir, true);
    ContextualStore contextualStore = environment.openStore(store, StoreConfig.WITHOUT_DUPLICATES);
  }

  @Override public void closeEnvironment(String dir) {
    PersistentEntityStore entityStore = entityStoreMap.get(dir);
    entityStore.getEnvironment().close();
    entityStore.close();
  }

  @Override public void closeEnvironments() {
    entityStoreMap.forEach((dir, entityStore) -> {
      entityStore.getEnvironment().close();
      entityStore.close();
    });
  }

  private Environment deleteLockingProcessAndGetEnvironment(String dir,
      EnvironmentConfig config, boolean isContextual) {
    Environment env = null;
    try {
      env = isContextual ? Environments.newContextualInstance(dir,config) : Environments.newInstance(dir, config);
    } catch (Exception e) {
      if (e.getMessage().contains("Can't acquire environment lock")) {
        try {
          String content = new Scanner(new File(dir)).useDelimiter("\\Z").next();
          String processId = parseProcessId(content);
          boolean isWindows = System.getProperty("os.name")
              .toLowerCase().startsWith("windows");
          String cmd = isWindows ? "taskkill /F /PID " + processId : "kill -9 " + processId;
          Runtime.getRuntime().exec(cmd);
          // Try again
          env = isContextual ? Environments.newContextualInstance(dir,config) : Environments.newInstance(dir, config);
        } catch (FileNotFoundException ex) {
          LOG.error("Lock file not found");
        } catch (IOException ex) {
          LOG.error("Lock file access error");
        }
      }
    }
    return env;
  }

  /**
   * Registers a custom property type
   *
   * @param dir
   * @param clazz   class of property values extending {@linkplain Comparable}
   * @param binding {@code ComparableBinding}
   * @param <T>     class property type
   * @param <B>     binding type
   */
  public <T extends Comparable, B extends ComparableBinding> void registerCustomPropertyType(
      String dir, final Class<T> clazz, final B binding) {
    getPersistentEntityStore(dir, false).executeInTransaction(txn -> {
      ((PersistentEntityStore) txn.getStore()).registerCustomPropertyType(txn, clazz, binding);
    });
  }

  public static String parseProcessId(String text) {
    Pattern pattern = Pattern.compile("^Private property of Exodus:\\s(\\d+)");
    Matcher matcher = pattern.matcher(text);
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
  }

  public static <T, E extends Exception> Consumer<T> consumerWrapper(
      ThrowingConsumer<T> consumer, Class<E> clazz) {
    return i -> {
      try {
        consumer.accept(i);
      } catch (Exception ex) {
        ex.printStackTrace();
        try {
          E exCast = clazz.cast(ex);
          System.err.println(
              "Exception occurred : " + exCast.getMessage());
        } catch (ClassCastException ccEx) {
          //throw ex;
        }
      }
    };
  }
}
