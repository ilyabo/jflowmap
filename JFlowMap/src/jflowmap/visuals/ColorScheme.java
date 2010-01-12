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

package jflowmap.visuals;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * @author Ilya Boyandin
 */
public class ColorScheme {

    private final String name;
    private final Map<ColorCodes, Color> colors;

    private ColorScheme(String name, Map<ColorCodes, Color> colors) {
        this.name = name;
        for (ColorCodes code : ColorCodes.values()) {
            if (!colors.containsKey(code)) {
                throw new IllegalArgumentException("Color " + code + " is missing");
            }
        }
        this.colors = ImmutableMap.copyOf(colors);
    }

    public String getName() {
        return name;
    }

    public Color get(ColorCodes code) {
        return colors.get(code);
    }

    public static ColorScheme of(String name, Map<ColorCodes, Color> colors) {
        return new ColorScheme(name, colors);
    }

}
