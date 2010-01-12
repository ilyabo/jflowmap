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

package jflowmap.util;

import java.awt.Color;

import prefuse.util.ColorLib;

/**
 * @author Ilya Boyandin
 */
public class ColorUtils {

    public static final Color[] createCategoryColors(int numberOfColors) {
        Color[] colors = new Color[numberOfColors];
        int[] palette = ColorLib.getCategoryPalette(numberOfColors, .7f, .4f, 1.f, 1.f);
        for (int i = 0; i < numberOfColors; i++) {
            colors[i] = new Color(palette[i]);
        }
        return colors;
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

}
