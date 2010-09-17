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
import java.util.Iterator;

import jflowmap.data.FlowMapGraphEdgeAggregator.ValueAggregator;

/**
 * @author Ilya Boyandin
 */
public enum GraphMLDataTypes implements ValueAggregator {
  INT(int.class, null, "int", "integer") {
    @Override
    public Object aggregate(Iterable<Object> values) {
      int sum = 0;
      for (Object obj : values) {
        sum += ((Integer)obj).intValue();
      }
      return sum;
    }
  },
  LONG(long.class, null, "long") {
    @Override
    public Object aggregate(Iterable<Object> values) {
      long sum = 0;
      for (Object obj : values) {
        sum += ((Long)obj).longValue();
      }
      return sum;
    }
  },
  FLOAT(float.class, Float.NaN, "float") {
    @Override
    public Object aggregate(Iterable<Object> values) {
      float sum = 0;
      for (Object obj : values) {
        sum += ((Float)obj).floatValue();
      }
      return sum;
    }
  },
  DOUBLE(double.class, Double.NaN, "double", "real") {
    @Override
    public Object aggregate(Iterable<Object> values) {
      double sum = 0;
      for (Object obj : values) {
        sum += ((Double)obj).doubleValue();
      }
      return sum;
    }
  },
  BOOLEAN(boolean.class, null, "boolean"),
  STRING(String.class, "", "string") {
    @Override
    public Object aggregate(Iterable<Object> values) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<Object> it = values.iterator(); it.hasNext(); ) {
        Object obj = it.next();
        sb.append(obj != null ? obj.toString() : "null");
        if (it.hasNext()) { sb.append(","); }
      }
      return sb.toString();
    }
  },
  DATE(Date.class, null, "date");

  final Class<?> klass;
  private final String[] names;
  private Object defaultValue;

  private GraphMLDataTypes(Class<?> klass, Object defaultValue, String ... names) {
    this.klass = klass;
    this.defaultValue = defaultValue;
    this.names = names;
  }

  public static GraphMLDataTypes getByType(Class<?> type) {
    for (GraphMLDataTypes t : values()) {
      if (t.klass.equals(type)) {
        return t;
      }
    }
    return null;
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

  @Override
  public Object aggregate(Iterable<Object> values) {
    return null;
  }
}