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

import java.util.Iterator;

/**
 * @author Ilya Boyandin
 */
public class MinMax {
  private final double min;
  private final double max;
	private final double avg;
//  private final double minLog;
//  private final double maxLog;
  private final double distLog;
  private final int count;

  private MinMax(double minValue, double avg, double maxValue, int count) {
    if (minValue > maxValue) {
      throw new IllegalArgumentException("minValue > maxValue");
    }
    if (avg > maxValue) {
      throw new IllegalArgumentException("avg > maxValue");
    }
    if (avg < minValue) {
      throw new IllegalArgumentException("avg < minValue");
    }
    this.avg = avg;
    this.min = minValue;
    this.max = maxValue;
//    this.minLog = Math.log(min);
//    this.maxLog = Math.log(max);
    this.distLog = Math.log(1.0 + (max - min));
    this.count = count;
  }


  public MinMax mergeWith(MinMax minMax) {
    return new MinMax(
        Math.min(min, minMax.min),
        (avg * count + minMax.count * minMax.count) / (count + minMax.count),
        Math.max(max, minMax.max),
        count + minMax.count
    );
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  public double getAvg() {
    return avg;
  }

//  public double getMaxLog() {
//    return maxLog;
//  }
//
//  public double getMinLog() {
//    return minLog;
//  }

  public static MinMax createFor(Iterable<Double> values) {
    return createFor(values.iterator());
  }

  public static MinMax createFor(Iterator<Double> it) {
    if (!it.hasNext()) {
      return new MinMax(Double.NaN, Double.NaN, Double.NaN, 0);
    }
    double max = Double.MIN_VALUE;
    double min = Double.MAX_VALUE;
    double sum = 0;
    int count = 0;

    while (it.hasNext()) {
      double v = it.next();
      if (v > max) {
        max = v;
      }
      if (v < min) {
        min = v;
      }
      sum += v;
      count++;
    }
    return new MinMax(min, sum / count, max, count);
  }

  /**
   * Returns a normalized value between 0 and 1 for the dataset
   * the min and max were calculated for.
   * In case if max == min the method always returns 1.
   */
  public double normalize(double value) {
    checkInterval(value);
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    if (getMax() == getMin()) return 1.0;
    double rv = (value - getMin()) / (getMax() - getMin());
    checkNormalized(value, rv);
    return rv;
  }

  public double denormalize(double normalized) {
    if (Double.isNaN(normalized)) {
      return Double.NaN;
    }
    if (getMax() == getMin()) return getMin();

    double rv = getMin() + (normalized * (getMax() - getMin()));
//    checkInterval(rv);
    return rv;
  }

  /**
   * Returns a value between -1.0 and 1.0. Zero stays zero.
   * If both min and max are positive, works in the same way as {@link #normalize(double)}
   */
  public double normalizeAroundZero(double value) {
    if (getMin() >= 0.0) {
      return normalize(value);
    }
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    checkInterval(value);
    if (getMax() == getMin()) return 0.0;
    double r = Math.max(Math.abs(getMin()), Math.abs(getMax()));
    double rv = value / r;
    checkNormalized(value, rv, -1.0, 1.0);
    return rv;
  }

  private void checkNormalized(double input, double normalized) throws AssertionError {
    checkNormalized(input, normalized, 0.0, 1.0);
  }

  private void checkNormalized(double input, double normalized, double min, double max) throws AssertionError {
    if (!(normalized >= min  &&  normalized <= max)) {
      throw new AssertionError("Normalized value must be between " + min + " and " + max + ". " +
          "Input value: " + input + ", " +
      		"Normalized value: " + normalized + ". " + this);
    }
  }

  private void checkInterval(double value) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          "Value must be between " + min + " and " + max + ". Actual value = " + value);
    }
  }

  /**
   * Returns a normalized log(value) between 0 and 1.
   * In case if max == min the method always returns 1.
   */
  public double normalizeLog(double value) {
    if (Double.isNaN(value)) {
      return Double.NaN;
    }
    checkInterval(value);
    if (getMax() == getMin()) return 1.0;
//    double rv = (Math.log(value) - getMinLog()) / (getMaxLog() - getMinLog());
//    double rv = Math.log(logarithmize(value)) / LOG_SCALE_MAX_LOG;
    double rv = Math.log(1.0 + value - min) / distLog;
    checkNormalized(value, rv);
    return rv;
  }

//  private static final double LOG_SCALE_MAX = 1e5;
//  private static final double LOG_SCALE_MAX_LOG = Math.log(LOG_SCALE_MAX);
//  private double logarithmize(double v) {
//    return 1 + (LOG_SCALE_MAX - 1) * (v - min) / (max - min);
//  }

  /**
   * Constructs a <code>String</code> with all attributes
   * in name = value format.
   *
   * @return a <code>String</code> representation
   * of this object.
   */
  @Override
  public String toString()
  {
    final String TAB = "  ";

    String retValue = "";

    retValue = "MinMax ( "
      + super.toString() + TAB
      + "min = " + this.min + TAB
      + "max = " + this.max + TAB
      + "avg = " + this.avg + TAB
//      + "minLog = " + this.minLog + TAB
//      + "maxLog = " + this.maxLog + TAB
      + " )";

    return retValue;
  }


}