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

package jflowmap.views.flowmap;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import jflowmap.data.SeqStat;
import jflowmap.geom.GeomUtils;
import jflowmap.util.ColorUtils;
import jflowmap.views.ColorCodes;
import prefuse.util.ColorLib;

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

  public Paint createPaint(double normalizedValue,
      double srcX, double srcY, double targetX, double targetY) {
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
  public Paint createPaint(double normalizedValue,
      double srcX, double srcY, double targetX, double targetY,
      double edgeLength, boolean isSelfLoop)
  {
    VisualFlowMapModel model = visualFlowMap.getModel();
    int alpha = model.getEdgeAlpha();

    boolean diverging = visualFlowMap.getValueStat().isDiverging();

    if (diverging) {
      return createSimpleDivergingPaint(normalizedValue, alpha);
    }

    if (isSelfLoop) {
      return createSelfLoopPaint(normalizedValue, alpha);
    }

    if (model.getShowDirectionMarkers()  ||  model.getFillEdgesWithGradient()) {
      return createCompositePaint(
          normalizedValue, srcX, srcY, targetX, targetY, edgeLength, model, alpha);
    } else {
      return createSimplePaint(normalizedValue, alpha);
    }
  }

  private Paint createSimplePaint(double normalizedValue, int alpha) {
    return ColorUtils.colorBetween(
        visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT),
        visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT),
        normalizedValue, alpha);
  }

  private Paint createSimpleDivergingPaint(double normalizedValue, int alpha) {
    return ColorLib.getColor(ColorUtils.colorFromMap(
        new int[] {
            visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MIN).getRGB(),
            visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_ZERO).getRGB(),
            visualFlowMap.getColor(ColorCodes.EDGE_NO_GRADIENT_DIVERGING_MAX).getRGB()
        },
        normalizedValue, -1.0, 1.0, alpha, true));
  }

  private Paint createSelfLoopPaint(double normalizedValue, int alpha) {
    if (visualFlowMap.getModel().getFillEdgesWithGradient()) {
      return ColorUtils.colorBetween(
          visualFlowMap.getColor(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT),
          visualFlowMap.getColor(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT),
          normalizedValue, alpha);
    } else {
      return createSimplePaint(normalizedValue, alpha);
    }
  }

  private Paint createCompositePaint(double normalizedValue, double srcX, double srcY,
      double targetX, double targetY, double edgeLength, VisualFlowMapModel model, int alpha) {

    // TODO: support for diverging color scheme
    Color startEdgeColor, endEdgeColor;
    if (model.getFillEdgesWithGradient()) {
      startEdgeColor = ColorUtils.colorBetween(
          visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT),
          visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT),
          normalizedValue, alpha);
      endEdgeColor = ColorUtils.colorBetween(
          visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT),
          visualFlowMap.getColor(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT),
          normalizedValue, alpha);
    } else {
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
        SeqStat lstats = visualFlowMap.getStats().getEdgeLengthStats();
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

    LinearGradientPaint paint = new LinearGradientPaint(
        (float)srcX, (float)srcY, (float)targetX, (float)targetY,
        fractions, colors
    );
    return paint;
  }

}
