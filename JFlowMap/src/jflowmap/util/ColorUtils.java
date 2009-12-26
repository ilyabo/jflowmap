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

}
