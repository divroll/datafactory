/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class GeoHashTest {
  @Test
  public void testCalculateGeoHashPrecisionIndex0() {
    int precision = GeoHash.calculateGeoHashPrecision(0.037);
    Assert.assertEquals(0, precision);
    precision = GeoHash.calculateGeoHashPrecision(0.018);
  }

  @Test
  public void testCalculateGeoHashPrecisionIndex1() {
    int precision = GeoHash.calculateGeoHashPrecision(5004000.00);
    Assert.assertEquals(11, precision);
    precision = GeoHash.calculateGeoHashPrecision(5004000.00);
    Assert.assertEquals(11, precision);
  }

  @Test
  public void testCalculateGeoHashPrecision() {
    int precision = GeoHash.calculateGeoHashPrecision(1000.00);
    Assert.assertEquals(6, precision);
    precision = GeoHash.calculateGeoHashPrecision(300.00);
    Assert.assertEquals(5, precision);
  }
}
