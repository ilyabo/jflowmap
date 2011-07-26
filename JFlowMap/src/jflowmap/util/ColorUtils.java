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

import java.awt.Color;

import prefuse.util.ColorLib;

/**
 * @author Ilya Boyandin
 */
public class ColorUtils {

  public static final Color[] createCategoryColors(int numberOfColors, float alpha) {
    Color[] colors = new Color[numberOfColors];
    int[] palette = ColorLib.getCategoryPalette(numberOfColors, .7f, .4f, 1.f, alpha);
    for (int i = 0; i < numberOfColors; i++) {
      colors[i] = new Color(palette[i], true);
    }
    return colors;
  }

  public static final Color[] createCategoryColors(int numberOfColors) {
    return createCategoryColors(numberOfColors, 1.f);
  }

  /**
   * Returns a color "between" min and max, corresponding to the given weight.
   * If the weight is 0.0 then the method returns min color,
   * if it's 1.0 then the max color, otherwise something in between.
   * @param weight Between 0.0 and 1.0
   */
  public static Color colorBetween(Color min, Color max, double weight, int alpha) {
    if (weight < 0.0  ||  weight > 1.0) {
      throw new IllegalArgumentException("Weight must be between 0.0 and 1.0. Actual: " + weight);
    }
    int r1 = min.getRed(), r2 = max.getRed();
    int g1 = min.getGreen(), g2 = max.getGreen();
    int b1 = min.getBlue(), b2 = max.getBlue();
    return new Color(
        (int)Math.round(r1 + (r2 - r1) * weight),
        (int)Math.round(g1 + (g2 - g1) * weight),
        (int)Math.round(b1 + (b2 - b1) * weight),
        alpha
    );
  }

  public static int intColorBetween(int color1, int color2, double weight, int alpha) {
    if (weight < 0.0  ||  weight > 1.0) {
      throw new IllegalArgumentException("Weight must be between 0.0 and 1.0. Actual: " + weight);
    }
    int r1 = ColorLib.red(color1), r2 = ColorLib.red(color2);
    int g1 = ColorLib.green(color1), g2 = ColorLib.green(color2);
    int b1 = ColorLib.blue(color1), b2 = ColorLib.blue(color2);
    return ColorLib.rgba(
        (int)Math.round(r1 + (r2 - r1) * weight),
        (int)Math.round(g1 + (g2 - g1) * weight),
        (int)Math.round(b1 + (b2 - b1) * weight),
        alpha);
  }

  public static int colorFromMap(int[] colors, double weight, double minW, double maxW, int alpha, boolean interpolate) {
    if (weight < minW  ||  weight > maxW) {
      throw new IllegalArgumentException("Weight must be between " + minW + " and " + maxW + ". Actual: " + weight);
    }
    double nw = (weight - minW) / (maxW - minW);
    int max = colors.length - 1;
    if (interpolate) {
      int low = (int)Math.floor(max * nw);
      int high = (int)Math.ceil(max * nw);
      if (low == high) {
        return colors[low];
      } else {
        double seg = 1.0 / max;
        double segw = (nw - seg * low) / seg;
        return intColorBetween(colors[low], colors[high], segw, alpha);
      }
    } else {
      return colors[(int)Math.round(max * nw)];
    }
  }

  public static Color farthestColor(Color from1, Color from2, Color to) {
    if (dist(from1, to) > dist(from2, to)) {
      return from1;
    } else {
      return from2;
    }
  }

  public static int dist(Color c1, Color c2) {
    return
      Math.abs(c1.getRed() - c2.getRed()) +
      Math.abs(c1.getGreen() - c2.getGreen()) +
      Math.abs(c1.getBlue() - c2.getBlue());
  }

  public static Color setAlpha(Color color, int alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  public static String toHexString(Color color) {
    String str = Integer.toHexString(color.getRGB()  &  0x00ffffff);
    while (str.length() < 6) { str = "0" + str; }
    return str;
  }

}
