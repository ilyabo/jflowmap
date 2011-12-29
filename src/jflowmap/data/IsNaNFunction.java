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

package jflowmap.data;

import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.FunctionExpression;
import prefuse.data.expression.Predicate;

/**
 * @author Ilya Boyandin
 */
public class IsNaNFunction extends FunctionExpression implements Predicate {

  public IsNaNFunction() {
    super(1);
  }

  public Class<?> getType(Schema s) {
    return boolean.class;
  }

  @Override
  public Object get(Tuple t) {
    return getBoolean(t) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public String getName() {
    return "ISNAN";
  }

  @Override
  public boolean getBoolean(Tuple t) {
    if (paramCount() == 1) {
      return Double.isNaN(param(0).getDouble(t));
    } else {
        missingParams(); return false;
    }
  }

}
