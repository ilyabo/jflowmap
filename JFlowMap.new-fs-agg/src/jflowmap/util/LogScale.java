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

/**
 * TODO: finish LogScale
 * 
 * Allows to map a range of linear values to a log scale.
 * 
 * @author Ilya Boyandin
 */
final class LogScale {

	private final double base;
	private final double minLinearValue;
	private final double maxLinearValue;
	private final double argMultiplier;
	private final double norm;

	private LogScale(double base, double minLinearValue, double maxLinearValue, int steepness) {
		assert(steepness >= 0);
		this.base = base;
		this.minLinearValue = minLinearValue;
		this.maxLinearValue = maxLinearValue;
		this.argMultiplier = Math.pow(2, steepness + 1);
		this.norm = Math.log1p(this.argMultiplier);
	}
	
	/**
	 * @param x Between 0.0 and 0.1
	 * @return
	 */
	public double linearToLog(double x) {
		if (x < 0.0  ||  x > 1.0) {
			throw new IllegalArgumentException(
					"Argument must be between 0.0 and 1.0. Actual value: " + x);
		}
		if (Double.isNaN(x)) {
			return Double.NaN;
		}
		return 
			minLinearValue + 
			(Math.log1p(x * argMultiplier) / norm) * (maxLinearValue - minLinearValue);
	}

	/**
	 * @param a
	 * @return Value between 0.0 and 1.0
	 */
	public double logToLinear(double a) {
//		return Math.pow(a, b);
		return 0;
	}
	
	/*public*/ static final class Builder {
		private double base;
		private double min;
		private double max;
		private int steepness = 7;

		public Builder(double minLinearValue, double maxLinearValue) {
			this.min = minLinearValue;
			this.max = maxLinearValue;
		}
		
		/**
		 * @param steepness Must be greater than zero
		 */
		public Builder steepness(int steepness) {
			if (steepness < 0) throw new IllegalArgumentException();
			this.steepness = steepness;
			return this;
		}

		/**
		 * @param base Must be greater than 1.0
		 */
		public Builder base(double base) {
			if (base <= 1.0) throw new IllegalArgumentException();
			this.base = base;
			return this;
		}
		
		public LogScale build() {
			return new LogScale(base, min, max, steepness);
		}
	}
	
}
