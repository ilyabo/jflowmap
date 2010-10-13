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

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import jflowmap.data.MinMax;
import jflowmap.geom.ArrowQuadPath;
import jflowmap.views.ColorCodes;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class VisualLegend extends PPath {

  private static final long serialVersionUID = -8907313603307434727L;

  private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 13);
  private static final int NUM_LEGEND_WEIGHT_VALUES = 5;

  private final VisualFlowMap visualFlowMap;
  private final double lineWidth = 50, gapBetweenLines = 3, gapBeforeText = 7, paddingX = 2, paddingY = 2;
  private final double spaceForArrow = 10;
  private final double startY = 10;
  private final double startX = 10;

  public VisualLegend(VisualFlowMap visualFlowMap) {
    super(new RoundRectangle2D.Double(4, 4, 140, 120, 10, 20));
    setPaint(visualFlowMap.getColor(ColorCodes.LEDGEND_BOX_PAINT));
    setStroke(null);
    setStrokePaint(null);
    this.visualFlowMap = visualFlowMap;
    setPickable(false);
    update();
  }

  public void updateScale() {
    setScale(visualFlowMap.getModel().getVisualLegendScale());
  }

  public void update() {
    removeAllChildren();

    double posX = startX + paddingX;
    double posY = startY + paddingY;

    addChild(createArrow(posX, posY));
    posY += spaceForArrow;

    MinMax stats = visualFlowMap.getFlowMapGraph().getStats().getEdgeWeightStats();
    List<Double> legendValues = LegendValuesGenerator.generateLegendValues(
        stats.getMin(), stats.getMax(), NUM_LEGEND_WEIGHT_VALUES);

    PPath line = null;
    PText text = null;
    double rightMost = 0;
    for (Double weight : legendValues) {
      final float fontSize = LABEL_FONT.getSize2D();

      line = createLine(weight, posX, posY, lineWidth, fontSize);
      addChild(line);

      double height = line.getHeight();
      double textY;
      if (height < fontSize) {
        height = fontSize;
        textY = line.getY() + getStrokeWidth(line.getStroke())/2;
      } else {
        textY = line.getY() + height/2;
      }

      text = createText(weight, posX + lineWidth + gapBeforeText, textY);
      addChild(text);

      double r = text.getX() + text.getWidth();
      if (r > rightMost) {
        rightMost = r;
      }

      posY += height + gapBetweenLines;
    }
    if (line != null) {
      setWidth(rightMost - getY() + paddingY * 2);
      setHeight((line.getY() + line.getHeight() + gapBetweenLines + paddingX * 2));
    }
  }

  private PPath createArrow(double posX, double posY) {
    PPath arrow = new PPath(new ArrowQuadPath(
        posX + 2, posY + spaceForArrow/2, posX + lineWidth - 4, posY + spaceForArrow/2,
        posX + lineWidth/2, posY + spaceForArrow/2, 4));
    arrow.setStrokePaint(visualFlowMap.getColor(ColorCodes.LEDGEND_ARROW));
    return arrow;
  }

  private PPath createLine(double weight, double x, double y, double width, double minVSpace) {
    VisualFlowMapModel fmm = visualFlowMap.getModel();
    Paint paint = visualFlowMap.getVisualEdgePaintFactory().createPaint(
        fmm.normalizeEdgeWeightForColorScale(weight), x, y, x + width, y);

    Stroke stroke = visualFlowMap.getVisualEdgeStrokeFactory().createStroke(
        fmm.normalizeEdgeWeightForWidthScale(weight));

    double sw = getStrokeWidth(stroke);
    double height = sw;
    if (height < minVSpace) {
      height = minVSpace;
    }

    double yp = y + height/2;
    PPath ppath = new PPath(new Line2D.Double(x, yp, x + width, yp));
    ppath.setStroke(stroke);
    ppath.setStrokePaint(paint);
    return ppath;
  }

  private PText createText(double weight, double leftX, double middleY) {
    PText ptext = new PText(FlowMapView.NUMBER_FORMAT.format(weight));
    ptext.setX(leftX);
    ptext.setY(middleY - LABEL_FONT.getSize2D()/2 - 1);
    ptext.setFont(LABEL_FONT);
    ptext.setTextPaint(visualFlowMap.getColor(ColorCodes.LEDGEND_TEXT));
//    ptext.setPaint(Color.black);
//    ptext.setTextPaint(Color.white);
//    ptext.setJustification(JLabel.CENTER_ALIGNMENT);
    return ptext;
  }

  private double getStrokeWidth(Stroke stroke) {
    double strokeWidth;
    if (stroke instanceof PFixedWidthStroke) {
      strokeWidth = ((PFixedWidthStroke)stroke).getLineWidth();
    } else if (stroke instanceof BasicStroke) {
      strokeWidth = ((BasicStroke)stroke).getLineWidth();
    } else {
      strokeWidth = visualFlowMap.getModel().getMaxEdgeWidth();
    }
    return strokeWidth;
  }

  @Override
  public void paint(PPaintContext pc) {
    final int oldQuality = pc.getRenderQuality();
    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    }

    super.paint(pc);

    if (oldQuality != PPaintContext.HIGH_QUALITY_RENDERING) {
      pc.setRenderQuality(oldQuality);
    }
  }

}
