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
 * @author Ilya Boyandin
 */
public class Normalizer {

  private final double min;
  private final double max;


  public Normalizer(double min, double max) {
    this.min = min;
    this.max = max;
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  /**
   * Returns a normalized value between 0 and 1 for the dataset the min and max were calculated for.
   * In case if max == min the method always returns 1.
   */
  public double normalize(double value) {
    checkInterval(value, min, max);
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    if (max == min)
      return 1.0;
    double rv = (value - min) / (max - min);
    checkNormalized(value, rv);
    return rv;
  }

  public double denormalize(double normalized) {
    if (Double.isNaN(normalized)) {
      return Double.NaN;
    }
    if (max == min)
      return min;

    double rv = min + (normalized * (max - min));
    // checkInterval(rv);
    return rv;
  }

  /**
   * Returns a value between -1.0 and 1.0. Zero stays zero.
   */
  public double normalizeAroundZero(double value) {
    return normalizeAroundZero(value, false);
  }

  public double normalizeAroundZero(double value, boolean omitIntervalCheck) {
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    if (!omitIntervalCheck) {
      checkIntervalAroundZero(value, min, max);
    }
    if (max == min)
      return 0.0;
    double r = Math.max(Math.abs(min), Math.abs(max));
    double rv = value / r;
    if (!omitIntervalCheck) {
      checkNormalized(value, rv, -1.0, 1.0);
    }
    return rv;
  }

  private void checkNormalized(double input, double normalized) throws AssertionError {
    checkNormalized(input, normalized, 0.0, 1.0);
  }

  private void checkNormalized(double input, double normalized, double min, double max) throws AssertionError {
    if (!(normalized >= min && normalized <= max)) {
      throw new AssertionError("Normalized value must be between " + min + " and " + max + ". "
          + "Input value: " + input + ", " + "Normalized value: " + normalized + ". " + this);
    }
  }

  private void checkInterval(double value, double min, double max) {
    if (value < min || value > max) {
      throw new IllegalArgumentException("Value must be between " + min + " and " + max + ". Actual value = "
          + value);
    }
  }

  private void checkIntervalAroundZero(double value, double min, double max) {
    double maxAbs = Math.max(Math.abs(min), Math.abs(max));
    if (Math.abs(value) > maxAbs) {
      throw new IllegalArgumentException("Value must be between " + (-maxAbs) + " and " + maxAbs + ". Actual value = "
          + value);
    }
  }

  /**
   * Returns a normalized log10(value) between 0 and 1. In case if max == min the method always
   * returns 1.
   */
  public double normalizeLog(double value) {
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    if (value == min) {
      return 0.0;
    }
    if (value == max) {
      return 1.0;
    }
    checkInterval(value, min, max);
    if (max == min)
      return 1.0;
    double logOfRadius = Math.log10(1.0 + (max - min));
    double rv = Math.log10(1.0 + value - min) / logOfRadius;
    checkNormalized(value, rv);
    return rv;
  }

  /**
   * Returns signum(value)*log10(abs(value)) normalized between -1 and 1. In case if max == min the
   * method always returns signum(max).
   */
  public double normalizeLogAroundZero(double value) {
    return normalizeLogAroundZero(value, false);
  }

  public double normalizeLogAroundZero(double value, boolean omitIntervalCheck) {
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    if (!omitIntervalCheck) {
      checkIntervalAroundZero(value, min, max);
    }
    if (max == min)
      return Math.signum(max);
    double radius = Math.max(Math.abs(max), Math.abs(min));
    double logOfRadius = Math.log10(1.0 + radius);
    double rv = Math.signum(value) * Math.log10(1.0 + Math.abs(value)) / logOfRadius;

    if (!omitIntervalCheck) { // TODO: remove this
      checkNormalized(value, rv, -1.0, 1.0);
    }
    return rv;
  }



}
