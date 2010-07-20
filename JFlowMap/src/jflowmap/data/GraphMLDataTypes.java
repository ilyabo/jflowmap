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

public enum GraphMLDataTypes {
  INT(int.class, "int", "integer"),
  LONG(long.class, "long"),
  FLOAT(float.class, "float"),
  DOUBLE(double.class, "double", "real"),
  BOOLEAN(boolean.class, "boolean"),
  STRING(String.class, "string"),
  DATE(Date.class, "date");

  final Class<?> klass;
  private final String[] names;

  private GraphMLDataTypes(Class<?> klass, String ... names) {
    this.klass = klass;
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
}