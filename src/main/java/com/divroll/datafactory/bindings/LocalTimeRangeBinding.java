package com.divroll.datafactory.bindings;

import com.divroll.datafactory.LocalTimeRange;
import java.io.ByteArrayInputStream;
import jetbrains.exodus.ArrayByteIterable;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.ComparableBinding;
import jetbrains.exodus.util.LightOutputStream;
import org.jetbrains.annotations.NotNull;

public class LocalTimeRangeBinding extends ComparableBinding {

  public static final LocalTimeRangeBinding BINDING = new LocalTimeRangeBinding();

  @Override public Comparable readObject(@NotNull ByteArrayInputStream stream) {
    return null;
  }

  @Override public void writeObject(@NotNull LightOutputStream output, @NotNull Comparable object) {

  }

  /**
   * De-serializes {@linkplain ByteIterable} entry to a {@linkplain LocalTimeRange} value.
   *
   * @param entry {@linkplain ByteIterable} instance
   * @return de-serialized value
   */
  public static LocalTimeRange entryToGeoPoint(@NotNull final ByteIterable entry) {
    return (LocalTimeRange) BINDING.entryToObject(entry);
  }

  /**
   * Serializes {@linkplain LocalTimeRange} value to the {@linkplain ArrayByteIterable} entry.
   *
   * @param object value to serialize
   * @return {@linkplain ArrayByteIterable} entry
   */
  public static ArrayByteIterable geoPointToEntry(final LocalTimeRange object) {
    return BINDING.objectToEntry(object);
  }

}
