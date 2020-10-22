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

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public final class Constants implements Serializable {
  public static final String JAVA_RMI_HOST_ENVIRONMENT = "java.rmi.server.hostname";
  public static final String JAVA_RMI_PORT_ENVIRONMENT = "java.rmi.server.port";
  public static final String JAVA_RMI_PORT_DEFAULT = "1099";
  public static final String DATAFACTORY_DIRECTORY_ENVIRONMENT = "datafactory.dir";
  public static final String NAMESPACE_PROPERTY = "____NAMESPACE____";

  private Constants() {
  }
}
