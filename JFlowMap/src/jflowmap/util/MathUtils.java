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

/**
 * 
 */
package jflowmap.util;

/**
 * @author ilya
 *
 */
public final class MathUtils {

	private MathUtils() {
	}
	
	public static double log(double x, double base) {
		if (base == Math.E) {
			return Math.log(x);
		}
		if (base == 10) {
			return Math.log10(x);
		}
		return Math.log(x) / Math.log(base);
	}

	public enum Rounding {
		FLOOR {
			@Override
			public long round(double value) {
				return (long) Math.floor(value);
			}
		},
		CEIL {
			@Override
			public long round(double value) {
				return (long) Math.ceil(value);
			}
		},
		ROUND {
			@Override
			public long round(double value) {
				return Math.round(value);
			}
		};
		
		public abstract long round(double value);
	}

}
