package com.divroll.datafactory.bindings;

import com.divroll.datafactory.GeoPoint;
import java.io.ByteArrayInputStream;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import org.jetbrains.annotations.NotNull;

public class GeoPointBinding extends ComparableBinding {

  public static final GeoPointBinding BINDING = new GeoPointBinding();

  @Override public Comparable readObject(@NotNull ByteArrayInputStream stream) {
    return null;
  }

  @Override public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {

  }

  /**
   * De-serializes {@linkplain ByteIterable} entry to a {@linkplain GeoPoint} value.
   *
   * @param entry {@linkplain ByteIterable} instance
   * @return de-serialized value
   */
  public static GeoPoint entryToGeoPoint(@NotNull final ByteIterable entry) {
    return (GeoPoint) BINDING.entryToObject(entry);
  }

  /**
   * Serializes {@linkplain GeoPoint} value to the {@linkplain ArrayByteIterable} entry.
   *
   * @param object value to serialize
   * @return {@linkplain ArrayByteIterable} entry
   */
  public static ArrayByteIterable geoPointToEntry(final GeoPoint object) {
    return BINDING.objectToEntry(object);
  }

}
