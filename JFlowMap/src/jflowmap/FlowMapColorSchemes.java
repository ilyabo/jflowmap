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

package jflowmap;

import java.awt.Color;

import jflowmap.views.ColorCodes;
import jflowmap.views.EnumColorScheme;
import jflowmap.views.IFlowMapColorScheme;

/**
 * @author Ilya Boyandin
 */
public enum FlowMapColorSchemes {
  DARK(new EnumColorScheme.Builder("Dark")
    // using colors from Till Nagel's Sankey Arcs for Origin-Dest
    .put(ColorCodes.BACKGROUND, Color.black/*new Color(0x20, 0x20, 0x20)*/)
    .put(ColorCodes.AREA_PAINT, new Color(45, 45, 45))
    .put(ColorCodes.AREA_STROKE, new Color(55, 55, 55))
    .put(ColorCodes.NODE_PAINT, new Color(255, 255, 255, 90))
    .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
    .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
    .put(ColorCodes.NODE_STROKE_PAINT, new Color(255, 255, 255, 200))
    .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))
    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(255, 150, 0, 200))
    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 182, 80, 200))
    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 202, 201, 200))
    .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(255, 255, 0))

    .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(255, 255, 255))

    .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 182, 80))
    .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 202, 201))
    .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 182, 80))
    .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 202, 201))

    .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(140, 140, 140, 200))
    .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
    .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))
    .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))

    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(5, 113, 176))
//    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(0, 0, 0))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(202, 0, 32))


    .build()),

    LIGHT(new EnumColorScheme.Builder("Light")
      .put(ColorCodes.BACKGROUND, new Color(255, 255, 255))
      .put(ColorCodes.AREA_PAINT, new Color(255, 255, 255))
      .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

      .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
      .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
      .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))

      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))

      .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(200, 156, 0))

      .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

      .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(26, 150, 65))

      // TODO: use ColorMap with these colors:  215, 25, 28; 253, 174, 97; 255, 255, 191; 166, 217, 106; 26, 150, 65;
      .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(26, 150, 65))

      .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
      .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
      .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
      .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))

    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(103, 169, 207))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(239, 138, 98))

      .build()),


      BLACK_ON_WHITE(new EnumColorScheme.Builder("Black on white")
      .put(ColorCodes.BACKGROUND, new Color(255, 255, 255))
      .put(ColorCodes.AREA_PAINT, new Color(235, 235, 235))
      .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

      .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
      .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
      .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))

      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))

      .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(0, 0, 0))

      .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

      .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(0, 0, 0))
      .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 0, 0))

      // TODO: use ColorMap with these colors:  215, 25, 28; 253, 174, 97; 255, 255, 191; 166, 217, 106; 26, 150, 65;
      .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(26, 150, 65))

      .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
      .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
      .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
      .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))

    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(0, 131, 207)) // 103, 169, 207))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(239, 68, 0))  //239, 138, 98))

      .build()),

/*
  LIGHT_BLUE(new EnumColorScheme.Builder("Light Blue")
      .put(ColorCodes.BACKGROUND, new Color(196, 224, 255))
//    .put(ColorCodes.BACKGROUND, new Color(216, 234, 255))
    .put(ColorCodes.AREA_PAINT, new Color(255, 255, 255))
    .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

    .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
    .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
    .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
    .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
    .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))

    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
    .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))

    .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 255))
    .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(200, 156, 0))

    .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
    .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(20, 20, 200))

    .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
    .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 0, 0))

//    .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
//    .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 255, 0))
    .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
    .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(48, 156, 1))

    .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
    .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 0, 0))
    .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
//    .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 255, 0))
    .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(8, 156, 1))

    .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
    .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
    .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
    .build()),
    */

    LIGHT_BLUE__COLOR_BREWER(new EnumColorScheme.Builder("Light Blue")
      .put(ColorCodes.BACKGROUND, new Color(196, 224, 255))
      .put(ColorCodes.AREA_PAINT, new Color(255, 255, 255))
      .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

      .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
      .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
      .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))

      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))

      .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(200, 156, 0))

      .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

      .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(26, 150, 65))

      // TODO: use ColorMap with these colors:  215, 25, 28; 253, 174, 97; 255, 255, 191; 166, 217, 106; 26, 150, 65;
      .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(26, 150, 65))

      .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
      .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
      .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))
      .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))

      .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(103, 169, 207))
      .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
      .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(239, 138, 98))

      .build()),

      /*
    LIGHT_BLUE__COLOR_BREWER__PHOTOCOPYABLE(new EnumColorScheme.Builder("Light Blue / Color Brewer")
      .put(ColorCodes.BACKGROUND, new Color(196, 224, 255))
      .put(ColorCodes.AREA_PAINT, new Color(255, 255, 255))
      .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

      .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
      .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
      .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
      .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))

      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
      .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))

      .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(200, 156, 0))

      .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

      .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(43, 131, 186))

      // TODO: use ColorMap with these colors:  215, 25, 28; 253, 174, 97; 255, 255, 191; 171, 221, 164; 43, 131, 186;
      .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(215, 25, 28))
      .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
      .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(43, 131, 186))

      .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
      .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
      .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
      .build()),
      */


  INVERTED(new EnumColorScheme.Builder("Inverted")
  .put(ColorCodes.BACKGROUND, new Color(223, 223, 223))
  .put(ColorCodes.AREA_PAINT, new Color(210, 210, 210))
  .put(ColorCodes.AREA_STROKE, new Color(200, 200, 200))
  .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))

  .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(55, 55, 255, 200))
  .put(ColorCodes.NODE_SELECTED_PAINT, new Color(55, 55, 255, 200))
  .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
  .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(0, 0, 255, 255))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(255, 255, 0, 200))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(0, 255, 255, 200))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(255, 0, 255, 200))

  .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(0, 0, 255))

  .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

  .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 0, 255))
  .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 255, 255))

  .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 0, 255))
  .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 255, 255))

   .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
    .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
    .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
      .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))

    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(103, 169, 207))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(239, 138, 98))

  .build()),

GRAY_RED_GREEN(new EnumColorScheme.Builder("Gray red-green")
  .put(ColorCodes.BACKGROUND, new Color(255, 255, 255))
  .put(ColorCodes.AREA_PAINT, new Color(210, 210, 210))
  .put(ColorCodes.AREA_STROKE, new Color(200, 200, 200))
  .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))

  .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(55, 55, 255, 200))
  .put(ColorCodes.NODE_SELECTED_PAINT, new Color(55, 55, 255, 200))
  .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
  .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(0, 0, 255, 255))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(255, 255, 0, 200))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(0, 255, 255, 200))
  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(255, 0, 255, 200))

  .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(255, 255, 0))

  .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))

  .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(188, 35, 39))
  .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(48, 156, 1))

  .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(188, 35, 39))
  .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
  .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(48, 156, 1))

    .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
    .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
    .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
      .put(ColorCodes.FLOW_ATTR_LABEL, new Color(200, 200, 200, 200))

    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN, new Color(103, 169, 207))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO, new Color(247, 247, 247))
    .put(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX, new Color(239, 138, 98))

  .build());

  private IFlowMapColorScheme scheme;

  private FlowMapColorSchemes(IFlowMapColorScheme scheme) {
    this.scheme = scheme;
  }

  public IFlowMapColorScheme getScheme() {
    return scheme;
  }

  public Color get(ColorCodes code) {
    return getScheme().get(code);
  }

  public Color getForValue(ColorCodes code, double value) {
    return getScheme().getForValue(code, value);
  }

  public static final FlowMapColorSchemes findByScheme(IFlowMapColorScheme scheme) {
    for (FlowMapColorSchemes cs : values()) {
      if (cs.getScheme() == scheme) {
        return cs;
      }
    }
    return null;
  }

  public static IFlowMapColorScheme findByName(String colorSchemeName) {
    for (FlowMapColorSchemes cs : values()) {
      if (colorSchemeName.equals(cs.getScheme().getName())) {
        return cs.getScheme();
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return getScheme().getName();
  }
}