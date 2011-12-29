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

import java.util.Arrays;
import java.util.Iterator;

import jflowmap.data.FlowMapGraphEdgeAggregator.AggEntity;
import jflowmap.data.FlowMapGraphEdgeAggregator.ValueAggregator;
import prefuse.data.Tuple;

/**
 * @author Ilya Boyandin
 */
public enum AttrDataTypes implements ValueAggregator {
  /*
  INT(int.class, null, "int", "integer") {
    @Override
    public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
      int sum = 0;
      for (Object obj : values) {
        sum += ((Integer)obj).intValue();
      }
      return sum;
    }
  },
  LONG(long.class, null, "long") {
    @Override
    public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
      long sum = 0;
      for (Object obj : values) {
        sum += ((Long)obj).longValue();
      }
      return sum;
    }
  },
  FLOAT(float.class, Float.NaN, "float") {
    @Override
    public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
      float sum = 0;
      for (Object obj : values) {
        sum += ((Float)obj).floatValue();
      }
      return sum;
    }
  },
  */
  DOUBLE(double.class, Double.NaN, "double", "real") {
    @Override
    public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
      double sum = 0;
      int cnt = 0;
      for (Object obj : values) {
        double v = ((Double)obj).doubleValue();
        if (!Double.isNaN(v)) {
          sum += v;
          cnt++;
        }
      }
      if (cnt == 0)
        return Double.NaN;
      else
        return sum;
    }
  },
  BOOLEAN(boolean.class, null, "boolean"),
  STRING(String.class, "", "string") {
    @Override
    public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<Object> it = values.iterator(); it.hasNext(); ) {
        Object obj = it.next();
        sb.append(obj != null ? obj.toString() : "null");
        if (it.hasNext()) { sb.append(","); }
      }
      return sb.toString();
    }
  }/*,
  DATE(Date.class, null, "date")*/;

  final Class<?> klass;
  private final String[] names;
  private Object defaultValue;

  private AttrDataTypes(Class<?> klass, Object defaultValue, String ... names) {
    this.klass = klass;
    this.defaultValue = defaultValue;
    this.names = names;
  }

  public static AttrDataTypes getByType(Class<?> type) {
    for (AttrDataTypes t : values()) {
      if (t.klass.equals(type)) {
        return t;
      }
    }
    return null;
  }

  public static AttrDataTypes parse(String typeName) {
    for (AttrDataTypes type : values()) {
      for (String name : type.names) {
        if (typeName.equals(name)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("Type " + typeName + " is not supported. " +
    		"List of supported types:\n" +  Arrays.toString(values()));
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  @Override
  public Object aggregate(Iterable<Object> values, Iterable<Tuple> tuples, AggEntity entity) {
    return null;
  }

  @SuppressWarnings("unchecked")
  public int compare(Object v1, Object v2) {
    if (!klass.isInstance(v1)  ||  !klass.isInstance(v2)) {
      throw new IllegalArgumentException();
    }

    if (v1 == null) {
      if (v2 == null) {
        return 0;
      }
      return -1;
    } else if (v2 == null) {
      return 1;
    }

    if (!(v1 instanceof Comparable)  ||  !(v2 instanceof Comparable)) {
      throw new UnsupportedOperationException();
    }

    return ((Comparable<Object>)v1).compareTo(v2);
  }
}
