/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.util;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author Ilya Boyandin
 */
public class PropUtils {

  private PropUtils() {
  }

  public static String require(Properties props, String propName) {
    String val = props.getProperty(propName);
    if (val == null) {
      throw new IllegalArgumentException("Property '"+propName+"' must be specified");
    }
    return val.trim();
  }

  public static String getString(Properties props, String propName) {
    return getStringOrElse(props, propName, null);
  }

  public static String getStringOrElse(Properties props, String propName, String defaultValue) {
    String val = props.getProperty(propName);
    if (isEmpty(val)) {
      return defaultValue;
    }
    return val.trim();
  }

  public static int getIntOrElse(Properties props, String propName, int defaultValue) {
    String val = props.getProperty(propName);
    if (isEmpty(val)) {
      return defaultValue;
    }
    return Integer.parseInt(val.trim());
  }

  public static double getDoubleOrElse(Properties props, String propName, double defaultValue) {
    String val = props.getProperty(propName);
    if (isEmpty(val)) {
      return defaultValue;
    }
    return Double.parseDouble(val.trim());
  }

  public static boolean getBoolOrElse(Properties props, String propName, boolean defaultValue) {
    String val = props.getProperty(propName);
    if (isEmpty(val)) {
      return defaultValue;
    }
    return Boolean.parseBoolean(val.trim());
  }

  public static boolean isEmpty(String value) {
    return value == null  ||  value.trim().length() == 0;
  }

  public static Pair<String, String> requireOneOf(Properties props, String[] propNames) {
    for (String prop : propNames) {
      String val = props.getProperty(prop);
      if (!isEmpty(val)) {
        return Pair.of(prop, val.trim());
      }
    }
    if (propNames.length > 0) {
      throw new IllegalArgumentException("At least one of the properties '" +
          Arrays.toString(propNames) +"' must be specified");
    } else {
      return null;
    }
  }

}
