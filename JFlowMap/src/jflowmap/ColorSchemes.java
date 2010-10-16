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

package jflowmap;

import java.util.List;

import prefuse.util.ColorLib;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

/**
 * See http://colorbrewer2.org/
 *
 * @author Ilya Boyandin
 */
public enum ColorSchemes {

  OrRd(
      Type.SEQUENTIAL,
      new int[] {
          ColorLib.rgb(255, 247, 236), ColorLib.rgb(254, 232, 200),
          ColorLib.rgb(253, 212, 158), ColorLib.rgb(253, 187, 132),
          ColorLib.rgb(252, 141, 89), ColorLib.rgb(239, 101, 72),
          ColorLib.rgb(215, 48, 31), ColorLib.rgb(179, 0, 0),
          ColorLib.rgb(127, 0, 0)
      }),
  Reds4(
      Type.SEQUENTIAL,
      new int[] {
          ColorLib.rgb(254, 229, 217), ColorLib.rgb(252, 174, 145), ColorLib.rgb(251, 106, 74),
          ColorLib.rgb(203, 24, 29)
      }),
  Purples4(
      Type.SEQUENTIAL,
      new int[] {
          ColorLib.rgb(242, 240, 247), ColorLib.rgb(203, 201, 226), ColorLib.rgb(158, 154, 200),
          ColorLib.rgb(106, 81, 163)
      }),
  RdPu5(
      Type.SEQUENTIAL,
      new int[] {
          ColorLib.rgb(254, 235, 226), ColorLib.rgb(251, 180, 185), ColorLib.rgb(247, 104, 161),
          ColorLib.rgb(197, 27, 138), ColorLib.rgb(122, 1, 119)
       }),


  RdYlGn9(
      Type.DIVERGING,
      new int[] {
        ColorLib.rgb(0, 104, 55),
        ColorLib.rgb(26, 152, 80),
        ColorLib.rgb(102, 189, 99),
        ColorLib.rgb(166, 217, 106),
        ColorLib.rgb(217, 239, 139),
        ColorLib.rgb(255, 255, 191),
        ColorLib.rgb(254, 224, 139),
        ColorLib.rgb(253, 174, 97),
        ColorLib.rgb(244, 109, 67),
        ColorLib.rgb(215, 48, 39),
        ColorLib.rgb(165, 0, 38)
      }),
  Spectral3(
      Type.DIVERGING,
      new int[] {
          ColorLib.rgb(252, 141, 89),
          ColorLib.rgb(255, 255, 191),
          ColorLib.rgb(153, 213, 148)
      }),
  RdYlBu6(                              // good for the duotimeline heatmap
      Type.DIVERGING,
      new int[] {
        ColorLib.rgb(215, 48, 39),
        ColorLib.rgb(252, 141, 89),
        ColorLib.rgb(254, 224, 144),
        ColorLib.rgb(224, 243, 248),
        ColorLib.rgb(145, 191, 219),
        ColorLib.rgb(69, 117, 180)
      }),
  RdBu5(
      Type.DIVERGING,
      new int[] {
          ColorLib.rgb(5, 113, 176),
          ColorLib.rgb(146, 197, 222),
          ColorLib.rgb(247, 247, 247),
          ColorLib.rgb(244, 165, 130),
          ColorLib.rgb(202, 0, 32)
      }),
  BrBg9(
      Type.DIVERGING,
      new int[] {
        ColorLib.rgb(140, 81, 10), ColorLib.rgb(191, 129, 45), ColorLib.rgb(223, 129, 125),
        ColorLib.rgb(246, 232, 195), ColorLib.rgb(245, 245, 245), ColorLib.rgb(199, 234, 229),
        ColorLib.rgb(128, 205, 193), ColorLib.rgb(53, 151, 143), ColorLib.rgb(1, 102, 94)
      }),
  BrBg5(
      Type.DIVERGING,
      new int[] {
        ColorLib.rgb(166, 97, 26), ColorLib.rgb(223, 194, 125), ColorLib.rgb(245, 245, 245),
        ColorLib.rgb(128, 205, 193), ColorLib.rgb(1, 133, 113)
      }),
  RdBu3(                                // for the geomaps
      Type.DIVERGING,
      new int[] {
          ColorLib.rgb(103, 169, 207), ColorLib.rgb(247, 247, 247), ColorLib.rgb(239, 138, 98)
      });


  public enum Type {
    SEQUENTIAL, DIVERGING, QUALITATIVE;
  }

  private Type type;
  private int[] colors;

  private ColorSchemes(Type type, int[] colors) {
      this.type = type;
      this.colors = colors;
  }

  public int[] getColors() {
    return colors;
  }

  public final static List<ColorSchemes> ofType(final Type type) {
    return ImmutableList.copyOf(
        Iterators.filter(Iterators.forArray(values()),
        new Predicate<ColorSchemes>() {
          public boolean apply(ColorSchemes cs) {
            return cs.type == type;
          }
        }));
  }

}
