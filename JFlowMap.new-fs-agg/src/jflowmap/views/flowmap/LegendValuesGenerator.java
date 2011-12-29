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

package jflowmap.views.flowmap;

import java.util.List;

import at.fhj.utils.graphics.AxisMarks;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class LegendValuesGenerator {

  // TODO: tests for LegendValuesGenerator
  private LegendValuesGenerator() {
  }

  public static List<Double> generate(double min, double max, int numValues) {
    if (max < min) {
      throw new IllegalArgumentException();
    }

    List<Double> legendValues = Lists.newArrayList();
    if (min >= 0  &&  max >= 0) {
      legendValues.addAll(genPositive(min, max, numValues));
    } else if (min < 0  &&  max > 0) {
      List<Double> positive = genPositive(0, Math.max(max, -min), numValues/2);
      legendValues.addAll(positive);
      addAsNegative(legendValues, positive);
    } else if (min < 0  &&  max < 0) {
      addAsNegative(legendValues, genPositive(-max, -min, numValues/2));
    }

    return legendValues;
  }

  private static void addAsNegative(List<Double> legendValues, List<Double> positive) {
    for (int i = positive.size() - 1; i >= 0; i--) {
      Double v = positive.get(i);
      if (v != 0) {
        legendValues.add(-v);
      }
    }
  }

  private static List<Double> genPositive(double min, double max, int numValues) {
    List<Double> legendValues = Lists.newArrayList();
    double ord = AxisMarks.ordAlpha(max);
    double w = Math.floor(max / ord) * ord;
    for (int i = 0; i < numValues / 2; i++) {
      if (w < min) {
        break;
      }
      legendValues.add(w);
      if (i == 0) {
        double nw = AxisMarks.ordAlpha(w);
        if (w != 0  &&  (w - nw) / w < 0.2) {
          w /= 2;
        } else {
          w = nw;
        }
      } else {
        w /= 2;
      }
    }

    if (w > min) {
      w = AxisMarks.ordAlpha(w);
      for (int i = 0; i < numValues / 2; i++) {
        if (w < min) {
          break;
        }
        legendValues.add(w);
        w /= 10;
      }
    }

    if (legendValues.size() == 0  || Iterables.getLast(legendValues) > min) {
      legendValues.add(min);
    }
    return legendValues;
  }

}
