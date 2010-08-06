/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.data;

import java.util.Date;

/**
 * @author Ilya Boyandin
 */
public enum GraphMLDataTypes {
  INT(int.class, null, "int", "integer"),
  LONG(long.class, null, "long"),
  FLOAT(float.class, Float.NaN, "float"),
  DOUBLE(double.class, Double.NaN, "double", "real"),
  BOOLEAN(boolean.class, null, "boolean"),
  STRING(String.class, "", "string"),
  DATE(Date.class, null, "date");

  final Class<?> klass;
  private final String[] names;
  private Object defaultValue;

  private GraphMLDataTypes(Class<?> klass, Object defaultValue, String ... names) {
    this.klass = klass;
    this.defaultValue = defaultValue;
    this.names = names;
  }

  public static GraphMLDataTypes parse(String typeName) {
    for (GraphMLDataTypes type : values()) {
      for (String name : type.names) {
        if (typeName.equals(name)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("Type " + typeName + " is not supported");
  }

  public Object getDefaultValue() {
    return defaultValue;
  }
}