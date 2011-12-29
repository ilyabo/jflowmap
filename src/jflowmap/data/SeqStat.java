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

import jflowmap.util.MathUtils;
import jflowmap.util.Normalizer;

/**
 * Stats for a sequence of numbers.
 *
 * @author Ilya Boyandin
 */
public class SeqStat {

  private final double min;
  private final double max;
  private final double sum;
  private final int count;
  private final Normalizer normalizer;

  private SeqStat(double minValue, double maxValue, double sum, int count) {
    if (minValue > maxValue) {
      throw new IllegalArgumentException("minValue > maxValue");
    }

    this.min = minValue;
    this.max = maxValue;
    this.sum = sum;
    this.count = count;
    this.normalizer = new Normalizer(min, max);

    double avg = getAvg();

    if (avg > maxValue) {
      throw new IllegalArgumentException("avg > maxValue");
    }
    if (avg < minValue) {
      throw new IllegalArgumentException("avg < minValue");
    }
  }

  public static SeqStat createFor(Iterable<Double> values) {
    return createFor(values.iterator());
  }

  public static SeqStat createFor(Iterator<Double> it) {
    if (!it.hasNext()) {
      return new SeqStat(Double.NaN, Double.NaN, Double.NaN, 0);
    }
    double max = Double.NaN;
    double min = Double.NaN;
    double sum = 0;
    double sumPos = 0;
    double sumNeg = 0;
    int count = 0;

    while (it.hasNext()) {
      double v = it.next();
      if (Double.isNaN(max) || v > max) {
        max = v;
      }
      if (Double.isNaN(min) || v < min) {
        min = v;
      }
      if (!Double.isNaN(v)) {
        if (v > 0) {
          sumPos += v;
        } else if (v < 0) {
          sumNeg += v;
        }
        sum += v;
        count++;
      }
    }
    return new SeqStat(min, max, sum, count);
  }

  public SeqStat mergeWith(Iterable<Double> it) {
    return mergeWith(createFor(it));
  }

  public SeqStat mergeWith(SeqStat minMax) {
    return new SeqStat(
        MathUtils.nonNaNMin(min, minMax.min), MathUtils.nonNaNMax(max, minMax.max),
        sum + minMax.sum,
        count + minMax.count);
  }

  public SeqStat mergeWith(double value) {
    if (Double.isNaN(value)  ||  (min <= value  &&  value <= max)) {
      return this;
    } else {
      return new SeqStat(
          MathUtils.nonNaNMin(min, value), MathUtils.nonNaNMax(max, value),
          sum + value,
          count + 1
      );
    }
  }

  public boolean isDiverging() {
    return (min < 0  &&  max > 0);
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  /**
   * Sum of the non-NaN values
   */
  public double getSum() {
    return sum;
  }

  /**
   * Average of the non-NaN values
   */
  public double getAvg() {
    return sum / count;
  }

  public Normalizer normalizer() {
    return normalizer;
  }

  @Override
  public String toString() {
    return "MinMax [min=" + min + ", max=" + max + ", sum=" + sum + ", count=" + count + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + count;
    long temp;
    temp = Double.doubleToLongBits(max);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(min);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(sum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SeqStat other = (SeqStat) obj;
    if (count != other.count)
      return false;
    if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
      return false;
    if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
      return false;
    if (Double.doubleToLongBits(sum) != Double.doubleToLongBits(other.sum))
      return false;
    return true;
  }

}
