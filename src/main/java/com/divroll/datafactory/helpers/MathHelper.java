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
package com.divroll.datafactory.helpers;

import java.time.LocalTime;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class MathHelper {
  public static boolean inRange(LocalTime upper1, LocalTime lower1, LocalTime upper2,
      LocalTime lower2) {
    throw new IllegalArgumentException("Not yet implemented");
  }

  public static double findClosest(double arr[], double target) {
    int n = arr.length;
    if (target <= arr[0]) {
      return arr[0];
    }
    if (target >= arr[n - 1]) {
      return arr[n - 1];
    }
    int i = 0, j = n, mid = 0;
    while (i < j) {
      mid = (i + j) / 2;
      if (arr[mid] == target) {
        return arr[mid];
      }
      if (target < arr[mid]) {
        if (mid > 0 && target > arr[mid - 1]) {
          return getClosest(arr[mid - 1],
              arr[mid], target);
        }
        j = mid;
      } else {
        if (mid < n - 1 && target < arr[mid + 1]) {
          return getClosest(arr[mid],
              arr[mid + 1], target);
        }
        i = mid + 1;
      }
    }
    return arr[mid];
  }

  public static double getClosest(double val1, double val2,
      double target) {
    if (target - val1 >= val2 - target) {
      return val2;
    } else {
      return val1;
    }
  }
}
