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
package com.divroll.datafactory.exceptions;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.divroll.datafactory.exceptions.Throwing.rethrow;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ThrowingTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testRethrow() {
    thrown.expect(IOException.class);
    thrown.expectMessage("i=3");

    Arrays.asList(1, 2, 3).forEach(rethrow(e -> {
      int i = e.intValue();
      if (i == 3) {
        throw new IOException("i=" + i);
      }
    }));
  }

}
