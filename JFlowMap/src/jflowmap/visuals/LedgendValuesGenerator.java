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

package jflowmap.visuals;

import java.util.List;

import at.fhj.utils.graphics.AxisMarks;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class LedgendValuesGenerator {

  // TODO: tests for LedgendValuesGenerator
  private LedgendValuesGenerator() {
  }

  public static List<Double> generateLegendValues(double min, double max, int numValues) {
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
