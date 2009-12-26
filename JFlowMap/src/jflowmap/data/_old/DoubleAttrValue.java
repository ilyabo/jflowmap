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

package jflowmap.data._old;

/**
 * @author Ilya Boyandin
 */
public class DoubleAttrValue extends AbstractAttrValue<Double> {
	
	public DoubleAttrValue(double value) {
		super(value);
	}

	public DoubleAttrValue(Double value) {
		super(value);
	}

	public Class<Double> getType() {
		return Double.class;
	}

	public static double asDouble(IAttrValue<?> value) {
        if (value == null) {
        	return Double.NaN;
        }
		if (value.getType() != Double.class) {
			throw new IllegalArgumentException("Value '" + value.getValue() + "' is not of type double, but " + value.getType());
		}
        if (value.getValue() == null) {
        	return Double.NaN;
        }
        return ((DoubleAttrValue)value).getValue();
	}
}
