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

package jflowmap.ui;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.util.LineShape;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class Lasso extends PBasicInputEventHandler {

  private static final Stroke LINE_STROKE = new PFixedWidthStroke(2.0f);
  private static final double MIN_DIST_BETWEEN_SUBSEQUENT_POINTS = 5.0;  // on screen

  private final PNode targetNode;
  private final PLine line;
  private Point2D lastPos;
  private Area prevSelectionArea;

  /**
   * @param targetNode Node to which the lasso node will be added.
   */
  public Lasso(PNode targetNode, Color lineColor) {
    this.targetNode = targetNode;
    line = new PLine(null, LINE_STROKE);
    line.setStrokePaint(lineColor);
    setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return event.isRightMouseButton();
      }
    });
  }

  @Override
  public void mouseReleased(PInputEvent event) {
    if (inTarget(event)) {
      /*if (line.getPointCount() > 0)*/ {
        Area area = asArea(line.getLineReference());
        if (event.isShiftDown()) {
          area.add(prevSelectionArea);
        } else if (event.isAltDown()) {
          prevSelectionArea.subtract(area);
          area = prevSelectionArea;
        }
        selectionMade(area);
        prevSelectionArea = area;
        clear();
      }
    }
  }

  /**
   * @param shape In targetNode's local coords
   */
  public void selectionMade(Shape shape) {
    // to be overriden
  }

//  @Override
//  public void mouseMoved(PInputEvent event) {
//    if (!event.isLeftMouseButton()) {
//      clear();
//    }
//  }

//  @Override
//  public void mouseExited(PInputEvent event) {
//    if (event.getPickedNode() == targetNode) {
//      clear();
//    }
//  }

  @Override
  public void mouseDragged(PInputEvent event) {
    if (inTarget(event)) {
      Point2D posInCanvas = event.getCanvasPosition();
      int numPoints = line.getPointCount();
      if (numPoints == 0) {
        start();
      }
      if (numPoints == 0   ||  posInCanvas.distance(lastPos) > MIN_DIST_BETWEEN_SUBSEQUENT_POINTS) {
        lastPos = (Point2D) posInCanvas.clone();  // copy to be stored
        Point2D pos = targetNode.globalToLocal((Point2D)posInCanvas.clone());
        if (numPoints == 0) {
          line.addPoint(numPoints, pos.getX(), pos.getY());
          line.addPoint(numPoints + 1, pos.getX(), pos.getY());
        } else {
          line.addPoint(numPoints - 1, pos.getX(), pos.getY());
        }
      }
    }
  }

private boolean inTarget(PInputEvent event) {
  return targetNode.getFullBounds().contains(event.getCanvasPosition());
}

  private void start() {
    targetNode.addChild(line);
  }

  public void clear() {
    targetNode.removeChild(line);
    line.removeAllPoints();
    lastPos = null;
  }

  public static Area asArea(LineShape line) {
    GeneralPath path = new GeneralPath();
    int numPoints = line.getPointCount();
    if (numPoints > 0) {
      path.moveTo(line.getX(0), line.getY(0));
      for (int i = 0; i < numPoints; i++) {
        path.lineTo(line.getX(i), line.getY(i));
      }
    }
    return new Area(path);
  }
}
