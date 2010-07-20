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

package jflowmap.views.timeline;

import java.awt.Color;
import java.awt.Paint;

import prefuse.util.ColorLib;

/**
 * @author Ilya Boyandin
 */
public class DefaultDuoTimelineStyle implements DuoTimelineStyle {


  @Override
  public Color getBackgroundColor() {
    return Color.white;
  }

  private static final Color FLOW_CIRCLE_COLOR = new Color(215, 25, 28);
  @Override
  public Color getFlowCircleColor() {
    return FLOW_CIRCLE_COLOR;
  }

  private static final Color MAP_TO_MATRIX_LINE_COLOR = new Color(0f, 0f, 0f, .05f);
  @Override
  public Color getMapToMatrixLineLinesColor() {
    return MAP_TO_MATRIX_LINE_COLOR;
  }

  private static final Color MAP_TO_MATRIX_LINE_HIGHLIGHTED_COLOR = new Color(0f, 0f, .7f, .7f);
  @Override
  public Paint getMapToMatrixLineHighlightedColor() {
    return MAP_TO_MATRIX_LINE_HIGHLIGHTED_COLOR;
  }
  @Override
  public Color getMissingValueColor() {
    return Color.lightGray;
  }


  private static final int[] valueColors = new int[] {
      // ColorBrewer  OrRd (sequential)
      ColorLib.rgb(255, 247, 236), ColorLib.rgb(254, 232, 200),
      ColorLib.rgb(253, 212, 158), ColorLib.rgb(253, 187, 132),
      ColorLib.rgb(252, 141, 89), ColorLib.rgb(239, 101, 72),
      ColorLib.rgb(215, 48, 31), ColorLib.rgb(179, 0, 0),
      ColorLib.rgb(127, 0, 0)

      // ColorBrewer RdYlGn (diverging)
//      ColorLib.rgb(0, 104, 55),
//      ColorLib.rgb(26, 152, 80),
//      ColorLib.rgb(102, 189, 99),
//      ColorLib.rgb(166, 217, 106),
//      ColorLib.rgb(217, 239, 139),
//      ColorLib.rgb(255, 255, 191),
//      ColorLib.rgb(254, 224, 139),
//      ColorLib.rgb(253, 174, 97),
//      ColorLib.rgb(244, 109, 67),
//      ColorLib.rgb(215, 48, 39),
//      ColorLib.rgb(165, 0, 38)
  };

  @Override
  public int[] getValueColors() {
    return valueColors;
  }

}
