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
package com.divroll.datafactory;

import java.io.Serializable;
import java.time.LocalTime;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class LocalTimeRange implements Comparable<LocalTimeRange>, Serializable {

  private LocalTime lower;
  private LocalTime upper;

  public LocalTimeRange(LocalTime lower, LocalTime upper) {
    setLower(lower);
    setUpper(upper);
  }
  
  @Override public int compareTo(@NotNull LocalTimeRange range) {
    if ((range.lower.isBefore(lower) && range.upper.isBefore(lower)) && (range.lower.isBefore(upper)
        && range.upper.isBefore(upper))) {
      return -1;
    } else if ((range.lower.isAfter(lower) && range.upper.isAfter(lower)) && (range.lower.isAfter(
        upper)
        && range.upper.isAfter(upper))) {
      return 1;
    }
    return 0;
  }

  public LocalTime getLower() {
    return lower;
  }

  public void setLower(LocalTime lower) {
    this.lower = lower;
  }

  public LocalTime getUpper() {
    return upper;
  }

  public void setUpper(LocalTime upper) {
    this.upper = upper;
  }
}
