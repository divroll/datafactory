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

import com.divroll.datafactory.helpers.MathHelper;
import com.google.common.base.Preconditions;


/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class GeoHash {

  private String geoHash;

  private GeoHash() {
  }
  public GeoHash(Double longitude, Double latitude) {
    Preconditions.checkArgument(
        latitude >= -90 && latitude <= 90, "Latitude must be in the range of [-90, 90] degrees");
    Preconditions.checkArgument(
        longitude >= -180 && longitude <= 180,
        "Longitude must be in the range of [-180, 180] degrees");
    this.geoHash = ch.hsr.geohash.GeoHash.withCharacterPrecision(latitude, longitude, 12).toBase32();
  }
  public static String create(Double longitude, Double latitude) {
    return new GeoHash(longitude, latitude).toString();
  }

  @Override public String toString() {
    return geoHash;
  }

  /**
   * Calculate the required precision for a give distance query
   *
   * @param reference
   * @return
   */
  public static int calculateGeoHashPrecision(Double reference) {
    double[][] ranges = {
        {0.037, 0.018},
        {0.149, 0.149},
        {1.19, 0.6},
        {4.47, 4.78},
        {38.2, 19.1},
        {152.8, 152.8},
        {1200, 610},
        {4900, 4900},
        {39000, 19500},
        {156000, 156000},
        {1251000, 625000},
        {5004000, 5004000},
    };
    int maxValueIndexInRaw = 0;
    int minValueIndexInRaw = 1;
    for (int i = 0; i < ranges.length; i++) {
      if (reference >= ranges[i][minValueIndexInRaw]
          && reference <= ranges[i][maxValueIndexInRaw]) {
        return i;
      }
    }
    // Nothing matched finding nearest match
    double arr[] = new double[24];
    int arrIndex = 0;
    for (int row = 0; row < ranges.length; row++) {
      for (int col = 0; col < ranges[row].length; col++) {
        arr[arrIndex] = ranges[row][col];
        arrIndex++;
      }
    }
    double nearest = MathHelper.findClosest(arr, reference);
    for (int row = 0; row < ranges.length; row++) {
      for (int col = 0; col < ranges[row].length; col++) {
        if (ranges[row][col] == nearest) {
          return row;
        }
      }
    }
    return -1;
  }

}
