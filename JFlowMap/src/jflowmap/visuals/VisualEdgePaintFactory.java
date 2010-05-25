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

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import jflowmap.data.MinMax;
import jflowmap.geom.GeomUtils;
import jflowmap.util.ColorUtils;

/**
 * @author Ilya Boyandin
 */
public class VisualEdgePaintFactory {

//  private static final int MIN_COLOR_INTENSITY = 25;
  private static final float[] DEFAULT_GRADIENT_FRACTIONS = new float[] { 0.0f, 1.0f };
  private static final float MIN_FRACTION_DIFF = 1e-5f;

  private final VisualFlowMap visualFlowMap;

  public VisualEdgePaintFactory(VisualFlowMap visualFlowMap) {
    this.visualFlowMap = visualFlowMap;
  }

  public Paint createPaint(double normalizedValue, double srcX, double srcY, double targetX, double targetY) {
    return createPaint(
        normalizedValue, srcX, srcY, targetX, targetY,
        GeomUtils.distance(srcX, srcY, targetX, targetY),
        GeomUtils.isSelfLoopEdge(srcX, srcY, targetX, targetY)
    );
  }

  /**
   * The parameters are redundant, but if the values are cached then passing
   * them instead of recalculating each time can improve performance
   */
  public Paint createPaint(
      double normalizedValue,
      double srcX, double srcY, double targetX, double targetY,
      double edgeLength, boolean isSelfLoop)
  {
    VisualFlowMapModel model = visualFlowMap.getModel();
    int alpha = model.getEdgeAlpha();
    if (isSelfLoop) {
      return ColorUtils.colorBetween(
          visualFlowMap.getColor(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT),
          visualFlowMap.getColor(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT),
          normalizedValue, alpha);
    } else {
      if (!model.getShowDirectionMarkers()  &&  !model.getFillEdgesWithGradient()) {
        return ColorUtils.colorBetween(
            visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT),
            visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT),
            normalizedValue, alpha);
      } else {
        Color startEdgeColor, endEdgeColor;
        if (model.getFillEdgesWithGradient()) {
//          startEdgeColor = new Color(value, 0, 0, alpha);
//          endEdgeColor = new Color(0, value, 0, alpha);
          startEdgeColor = ColorUtils.colorBetween(
              visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT),
              visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT),
              normalizedValue, alpha);
          endEdgeColor = ColorUtils.colorBetween(
              visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT),
              visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT),
              normalizedValue, alpha);
        } else {
          // TODO: use a special paint (not gradient) for this case
//          startEdgeColor = new Color(value, value, value, alpha);
          startEdgeColor = ColorUtils.colorBetween(
              visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT),
              visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT),
              normalizedValue, alpha);
          endEdgeColor = startEdgeColor;
        }

        float[] fractions = null;
        Color[] colors = null;
        if (model.getShowDirectionMarkers()) {
          float markerSize;
          if (model.getUseProportionalDirectionMarkers()) {
            markerSize = (float)model.getDirectionMarkerSize();
          } else {
            MinMax lstats = visualFlowMap.getStats().getEdgeLengthStats();
            markerSize = (float)Math.min(
                .5 - MIN_FRACTION_DIFF,  // the markers must not be longer than half of an edge
                ((lstats.getMin() + model.getDirectionMarkerSize() * (lstats.getMax() - lstats.getMin()))
                / 2)
                / edgeLength  // the markers must be of equal length for every edge
                        // (excepting the short ones)
            );
          }
          if (markerSize - MIN_FRACTION_DIFF < 0) {
            markerSize = MIN_FRACTION_DIFF;
          }
          if (markerSize > 0.5f - MIN_FRACTION_DIFF) {
            markerSize = 0.5f - MIN_FRACTION_DIFF;
          }
          int markerAlpha = model.getDirectionMarkerAlpha();
//          Color startMarkerColor = new Color(value, 0, 0, markerAlpha);
//          Color endMarkerColor = new Color(0, value, 0, markerAlpha);
          Color startMarkerColor = ColorUtils.colorBetween(
              visualFlowMap.getColor(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT),
              visualFlowMap.getColor(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT),
              normalizedValue, markerAlpha);
          Color endMarkerColor = ColorUtils.colorBetween(
              visualFlowMap.getColor(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT),
              visualFlowMap.getColor(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT),
              normalizedValue, markerAlpha);
          fractions = new float[] {
              markerSize - MIN_FRACTION_DIFF,     // start marker
              markerSize, 1.0f - markerSize,      // line
              1.0f - markerSize + MIN_FRACTION_DIFF   // end marker
          };
          colors = new Color[] {
              startMarkerColor,
              startEdgeColor,
              endEdgeColor,
              endMarkerColor,
          };
        } else {
          fractions = DEFAULT_GRADIENT_FRACTIONS;
          colors = new Color[] { startEdgeColor, endEdgeColor };
        }
        return new LinearGradientPaint(
            (float)srcX, (float)srcY, (float)targetX, (float)targetY,
            fractions, colors
        );
      }

    }
  }

}
