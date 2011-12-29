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

/**
 *
 */
package jflowmap.util;

import java.util.Comparator;

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

  /**
   * @param alpha
   *          between 0 and 1
   */
  public static double between(double a, double b, double alpha) {
    return (a + (b - a) * alpha);
  }

  /**
   * When using Double.compare NaN is the greatest value
   */
  public static int compareDoubles_smallestIsNaN(double d1, double d2) {
    if (Double.isNaN(d1)) {
      return (Double.isNaN(d2) ? 0 : -1);
    }
    if (Double.isNaN(d2)) {
      return 1;
    }
    return Double.compare(d1, d2);
  }

  public static Comparator<Double> COMPARE_DOUBLES_SMALLEST_IS_NAN = new Comparator<Double>() {
    @Override
    public int compare(Double a, Double b) {
      return MathUtils.compareDoubles_smallestIsNaN(a, b);
    }
  };

  public static double nonNaNMin(double a, double b) {
    if (Double.isNaN(a)) {
      return b;
    } else if (Double.isNaN(b)) {
      return a;
    } else {
      return Math.min(a, b);
    }
  }

  public static double nonNaNMax(double a, double b) {
    if (Double.isNaN(a)) {
      return b;
    } else if (Double.isNaN(b)) {
      return a;
    } else {
      return Math.max(a, b);
    }
  }

  public static double relativeDiff(double a, double b) {
    double rdiff;
    if (b == 0) {
      if (a == 0) {
        rdiff = 0;
      } else {
        rdiff = Math.signum(a);
      }
    } else {
      rdiff = (a - b) / b;
    }
    return rdiff;
  }

}
