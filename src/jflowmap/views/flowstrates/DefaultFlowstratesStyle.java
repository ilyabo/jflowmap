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

package jflowmap.views.flowstrates;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import jflowmap.FlowEndpoint;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class DefaultFlowstratesStyle implements FlowstratesStyle {


  @Override
  public Color getBackgroundColor() {
    return Color.white;
  }

  private static final Color FLOW_CIRCLE_COLOR = new Color(215, 25, 28);
  @Override
  public Color getFlowCircleColor() {
    return FLOW_CIRCLE_COLOR;
  }

  private static final Color MAP_TO_MATRIX_LINE_COLOR = new Color(70, 208, 243, 50);
//  private static final Color MAP_TO_MATRIX_LINE_COLOR = new Color(200, 200, 200);
  @Override
  public Color getFlowLineColor() {
    return MAP_TO_MATRIX_LINE_COLOR;
  }

  private static final Color MAP_TO_MATRIX_LINE_HIGHLIGHTED_COLOR = new Color(70, 208, 243, 150);
  @Override
  public Color getFlowLineHighlightedColor() {
    return MAP_TO_MATRIX_LINE_HIGHLIGHTED_COLOR;
  }
  @Override
  public Color getMissingValueColor() {
    return Color.lightGray;
  }

  /*

  private static final int[] sequentialValueColors = new int[] {
      // ColorBrewer  OrRd (sequential)
      ColorLib.rgb(255, 247, 236), ColorLib.rgb(254, 232, 200),
      ColorLib.rgb(253, 212, 158), ColorLib.rgb(253, 187, 132),
      ColorLib.rgb(252, 141, 89), ColorLib.rgb(239, 101, 72),
      ColorLib.rgb(215, 48, 31), ColorLib.rgb(179, 0, 0),
      ColorLib.rgb(127, 0, 0)
  };

  private static final int[] divergingValueColors = new int[] {
      // ColorBrewer RdYlGn 9 (diverging)
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

      // ColorBrewer Spectral 3
//      ColorLib.rgb(252, 141, 89),
//      ColorLib.rgb(255, 255, 191),
//      ColorLib.rgb(153, 213, 148)


      // ColorBrewer RdYlBu 6                              // good for the heatmap
//      ColorLib.rgb(215, 48, 39),
//      ColorLib.rgb(252, 141, 89),
//      ColorLib.rgb(254, 224, 144),
//      ColorLib.rgb(224, 243, 248),
//      ColorLib.rgb(145, 191, 219),
//      ColorLib.rgb(69, 117, 180)

      // ColorBrewer RdBu 5
      ColorLib.rgb(5, 113, 176),
      ColorLib.rgb(146, 197, 222),
      ColorLib.rgb(247, 247, 247),
      ColorLib.rgb(244, 165, 130),
      ColorLib.rgb(202, 0, 32)

      // ColorBrewer BrBg 9
//      ColorLib.rgb(140, 81, 10), ColorLib.rgb(191, 129, 45), ColorLib.rgb(223, 129, 125),
//      ColorLib.rgb(246, 232, 195), ColorLib.rgb(245, 245, 245), ColorLib.rgb(199, 234, 229),
//      ColorLib.rgb(128, 205, 193), ColorLib.rgb(53, 151, 143), ColorLib.rgb(1, 102, 94)

      // ColorBrewer BrBg 5
//      ColorLib.rgb(166, 97, 26), ColorLib.rgb(223, 194, 125), ColorLib.rgb(245, 245, 245),
//      ColorLib.rgb(128, 205, 193), ColorLib.rgb(1, 133, 113)

      // ColorBrewer RdBu 3                                // for the geomaps
//      ColorLib.rgb(239, 138, 98), ColorLib.rgb(247, 247, 247), ColorLib.rgb(103, 169, 207)
  };

  @Override
  public int[] getSequentialValueColors() {
    return sequentialValueColors;
  }

  @Override
  public int[] getDivergingValueColors() {
    return divergingValueColors;
  }
  */

  private static final BasicStroke TIMELINE_CELL_STROKE = null;
  @Override
  public Stroke getTimelineCellStroke() {
    return TIMELINE_CELL_STROKE;
  }

  private static final Stroke SELECTED_TIMELINE_CELL_STROKE = new PFixedWidthStroke(2);
  @Override
  public Stroke getHeatmapSelectedCellStroke() {
    return SELECTED_TIMELINE_CELL_STROKE;
  }

  private static final Color TIMELINE_CELL_STROKE_COLOR = null;
  @Override
  public Color getTimelineCellStrokeColor() {
    return TIMELINE_CELL_STROKE_COLOR;
  }

  private static final Color SELECTED_TIMELINE_CELL_STROKE_COLOR = Color.cyan;
  @Override
  public Color getHeatmapSelectedCellStrokeColor() {
    return SELECTED_TIMELINE_CELL_STROKE_COLOR;
  }

  private static final Color MAP_AREA_CENTROID_PAINT = new Color(0, 0, 200, 75);
  @Override
  public Color getMapAreaCentroidPaint() {
    return MAP_AREA_CENTROID_PAINT;
  }

  private static final Color MAP_AREA_SELECTED_CENTROID_PAINT = new Color(200, 0, 0);

  @Override
  public Color getMapAreaSelectedCentroidPaint() {
    return MAP_AREA_SELECTED_CENTROID_PAINT;
  }

  private static final Color MAP_AREA_HIGHLIGHTED_PAINT = new Color(255, 255, 255);
  @Override
  public Color getMapAreaHighlightedPaint() {
    return MAP_AREA_HIGHLIGHTED_PAINT;
  }

  private static final Color MAP_AREA_HIGHLIGHTED_STROKE_PAINT = new Color(255, 208, 144);
  @Override
  public Color getMapAreaHighlightedStrokePaint() {
    return MAP_AREA_HIGHLIGHTED_STROKE_PAINT;
  }

  private static final Color MAP_AREA_CENTROID_LABEL_TEXT_PAINT = new Color(0, 0, 200);

  @Override
  public Color getMapAreaCentroidLabelTextPaint() {
    return MAP_AREA_CENTROID_LABEL_TEXT_PAINT;
  }

  private static final PFixedWidthStroke MAP_AREA_HIGHLIGHTED_STROKE = new PFixedWidthStroke(3);
  @Override
  public Stroke getMapAreaHighlightedStroke() {
    return MAP_AREA_HIGHLIGHTED_STROKE;
  }

  private static final PFixedWidthStroke MAP_AREA_STROKE = new PFixedWidthStroke(1);
  @Override
  public Stroke getMapAreaStroke() {
    return MAP_AREA_STROKE;
  }

  private static final Color MAP_AREA_CENTROID_LABEL_PAINT = new Color(255, 255, 255, 100);
  @Override
  public Color getMapAreaCentroidLabelPaint() {
    return MAP_AREA_CENTROID_LABEL_PAINT;
  }

  private static final Color MAP_AREA_SELECTED_CENTROID_LABEL_PAINT = new Color(0, 213, 213, 150);
  @Override
  public Color getMapAreaSelectedCentroidLabelPaint() {
    return MAP_AREA_SELECTED_CENTROID_LABEL_PAINT;
  }

  private static final Color MAP_AREA_SELECTED_CENTROID_LABEL_TEXT_PAINT = new Color(0, 0, 0, 200);
  @Override
  public Color getMapAreaSelectedCentroidLabelTextPaint() {
    return MAP_AREA_SELECTED_CENTROID_LABEL_TEXT_PAINT;
  }

  private static final Color MAP_AREA_HIGHLIGHTED_CENTROID_PAINT = new Color(200, 200, 0);

  @Override
  public Color getMapAreaHighlightedCentroidColor() {
    return MAP_AREA_HIGHLIGHTED_CENTROID_PAINT;
  }

  private static final Color MAP_AREA_HIGHLIGHTED_CENTROID_LABEL_PAINT = new Color(255, 245, 181, 200);
  @Override
  public Color getMapAreaHighlightedCentroidLabelColor() {
    return MAP_AREA_HIGHLIGHTED_CENTROID_LABEL_PAINT;
  }

  private static final Color MAP_AREA_HIGHLIGHTED_CENTROID_LABEL_TEXT_PAINT = new Color(0, 0, 0, 255);
  @Override
  public Color getMapAreaHighlightedCentroidLabelTextColor() {
    return MAP_AREA_HIGHLIGHTED_CENTROID_LABEL_TEXT_PAINT;
  }

  private static final Color LASSO_SOURCES_STROKE_PAINT = new Color(200, 0, 0, 150);
  private static final Color LASSO_TARGETS_STROKE_PAINT = new Color(0, 0, 200, 150);

  @Override
  public Color getLassoStrokePaint(FlowEndpoint s) {
    switch (s) {
    case ORIGIN:
      return LASSO_SOURCES_STROKE_PAINT;
    case DEST:
      return LASSO_TARGETS_STROKE_PAINT;
    default:
      throw new AssertionError();
    }
  }



  @Override
  public Color getMapAreaHasNoFlowsColor() {
    return null;
  }

}
