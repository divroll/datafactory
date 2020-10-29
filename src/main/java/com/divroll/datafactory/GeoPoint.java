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

import com.google.common.base.Preconditions;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class GeoPoint implements Comparable<GeoPoint>, Serializable {

  private Double longitude;
  private Double latitude;

  public GeoPoint(Double longitude, Double latitude) {
    Preconditions.checkArgument(
        latitude >= -90 && latitude <= 90, "Latitude must be in the range of [-90, 90] degrees");
    Preconditions.checkArgument(
        longitude >= -180 && longitude <= 180,
        "Longitude must be in the range of [-180, 180] degrees");
    setLongitude(longitude);
    setLatitude(latitude);
  }

  @Override public int compareTo(@NotNull GeoPoint geoPoint) {
    return 0;
  }

  @Override public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    GeoPoint geoPoint = (GeoPoint) obj;
    return Double.compare(geoPoint.latitude, latitude) == 0
        && Double.compare(geoPoint.longitude, longitude) == 0;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }
}
