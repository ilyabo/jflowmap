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

package jflowmap.views;

import java.awt.Color;
import java.util.Map;

import prefuse.util.ColorLib;
import prefuse.util.ColorMap;

import com.google.common.collect.ImmutableMap;

/**
 * @author Ilya Boyandin
 */
public class EnumColorScheme implements IFlowMapColorScheme {

  private final String name;
  private final Map<ColorCodes, Getter> colors;

  private EnumColorScheme(String name, Map<ColorCodes, Getter> colors) {
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
    return colors.get(code).get();
  }

  public Color getForValue(ColorCodes code, double value) {
    return colors.get(code).getFor(value);
  }

  private static IFlowMapColorScheme of(String name, Map<ColorCodes, Getter> colors) {
    return new EnumColorScheme(name, colors);
  }

  public static class Builder {
    private final String name;
    private final ImmutableMap.Builder<ColorCodes, Getter> mapBuilder;

    public Builder(String name) {
      this.name = name;
      this.mapBuilder = ImmutableMap.builder();
    }

    public Builder put(ColorCodes code, Color paint) {
      mapBuilder.put(code, new ColorGetter(paint));
      return this;
    }

    public Builder put(ColorCodes code, ColorMap map) {
      mapBuilder.put(code, new ColorMapGetter(map));
      return this;
    }

    public IFlowMapColorScheme build() {
      return EnumColorScheme.of(name, mapBuilder.build());
    }
  }

  private interface Getter {
    Color get();
    Color getFor(double value);
  }

  private static class ColorGetter implements Getter {
    private final Color color;
    public ColorGetter(Color color) {
      super();
      this.color = color;
    }

    @Override
    public Color get() {
      return color;
    }

    @Override
    public Color getFor(double value) {
      throw new UnsupportedOperationException();
    }
  }


  private static class ColorMapGetter implements Getter {
    private final ColorMap colorMap;

    public ColorMapGetter(ColorMap colorMap) {
      this.colorMap = colorMap;
    }

    @Override
    public Color get() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Color getFor(double value) {
      return ColorLib.getColor(colorMap.getColor(value));
    }

  }

}
