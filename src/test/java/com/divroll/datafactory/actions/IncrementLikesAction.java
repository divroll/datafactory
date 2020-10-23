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
package com.divroll.datafactory.actions;

import jetbrains.exodus.entitystore.Entity;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class IncrementLikesAction implements CustomAction {

  Integer incrementValue;

  public IncrementLikesAction(Integer incrementValue) {
    this.incrementValue = incrementValue;
  }

  @Override public void execute(Entity entityInContext) {
    Integer likes = entityInContext.getProperty("likes") != null
        ? (Integer) entityInContext.getProperty("likes") : 0;
    likes = likes + incrementValue;
    entityInContext.setProperty("likes", likes);
  }
}
