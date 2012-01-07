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

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import jflowmap.JFlowMapMain;
import jflowmap.views.map.PGeoMap;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.nodes.PLine;
import edu.umd.cs.piccolox.util.LineShape;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * @author Ilya Boyandin
 */
public class Lasso extends PBasicInputEventHandler {

  private static final int APPLE_CMD_KEY_CODE = 1280;
  private static final Stroke LINE_STROKE = new PFixedWidthStroke(2.0f);
  private static final double MIN_DIST_BETWEEN_SUBSEQUENT_POINTS = .0;  // on screen

  private final PCamera camera;
  private final PLine line;
  private Point2D lastPos;
  private Area prevSelectionArea;
  private boolean wasDragged;

  /**
   * @param camera Node to which the lasso node will be added.
   */
  public Lasso(PCamera camera, Color lineColor) {
    this.camera = camera;
    line = new PLine(null, LINE_STROKE);
    line.setStrokePaint(lineColor);
//    setEventFilter(new PInputEventFilter() {
//      @Override
//      public boolean acceptsEvent(PInputEvent event, int type) {
//        return event.isControlDown();
//      }
//    });
  }

  public boolean isSelecting() {
    return line.getPointCount() > 0;
  }

  @Override
  public void mouseReleased(PInputEvent event) {
    if (inTarget(event)) {
      if (isSelecting()  ||
          (!wasDragged  &&  !isControlOrAppleCmdDown(event)  &&
             (event.getPickedNode() instanceof PGeoMap))) {  // to support "clear selection"
        stop(event);
      }
    }
    wasDragged = false;
  }

  /**
   * @param shape In camera's local coords
   */
  public void selectionMade(Shape shape) {
    // to be overriden
  }

//  @Override
  @Override
  public void mouseMoved(PInputEvent event) {
    if (isSelecting()  &&  !isControlOrAppleCmdDown(event)  &&  !event.isLeftMouseButton()) {
      stop(event);
    }
  }

//  @Override
//  public void mouseExited(PInputEvent event) {
//    if (event.getPickedNode() == camera) {
//      clear();
//    }
//  }

  @Override
  public void mouseDragged(PInputEvent event) {
    if (inTarget(event)) {
      if (isControlOrAppleCmdDown(event)) {
        Point2D posInCanvas = event.getCanvasPosition();
        int numPoints = line.getPointCount();
        if (numPoints == 0) {
          start();
        }
        if (numPoints == 0   ||  posInCanvas.distance(lastPos) > MIN_DIST_BETWEEN_SUBSEQUENT_POINTS) {
          lastPos = (Point2D) posInCanvas.clone();  // copy to be stored
          Point2D pos = camera.globalToLocal((Point2D)posInCanvas.clone());
          if (numPoints == 0) {
            line.addPoint(numPoints, pos.getX(), pos.getY());
            line.addPoint(numPoints + 1, pos.getX(), pos.getY());
          } else {
            line.addPoint(numPoints - 1, pos.getX(), pos.getY());
          }
        }
      } else {
//        stop(event);
      }
      wasDragged = true;
    }
  }

  public static boolean isControlOrAppleCmdDown(PInputEvent event) {
    return
        event.isControlDown()  ||
        (JFlowMapMain.IS_OS_MAC  &&  event.getModifiersEx() == APPLE_CMD_KEY_CODE);
  }

  private boolean inTarget(PInputEvent event) {
    return true;
//    return camera.localToGlobal(camera.getBounds()).contains(event.getCanvasPosition());
  }

  private void start() {
    camera.addChild(line);
  }

  private void stop(PInputEvent event) {
    Area area = asArea(line.getLineReference());
    if (event.isShiftDown()) {
      if (prevSelectionArea != null) {
        area.add(prevSelectionArea);
      }
    } else if (event.isAltDown()) {
      if (prevSelectionArea != null) {
        prevSelectionArea.subtract(area);
      }
      area = prevSelectionArea;
    }
    selectionMade(area);
    prevSelectionArea = area;
    clear();
  }

  public void clear() {
    camera.removeChild(line);
    line.removeAllPoints();
    lastPos = null;
    wasDragged = false;
  }

  public Area asArea(LineShape line) {
    GeneralPath path = new GeneralPath();
    int numPoints = line.getPointCount();
    if (numPoints > 0) {
      Point2D.Double p = new Point2D.Double(line.getX(0), line.getY(0));
      camera.localToView(p);
      path.moveTo(p.x, p.y);
      for (int i = 0; i < numPoints; i++) {
        p.setLocation(line.getX(i), line.getY(i));
        camera.localToView(p);
        path.lineTo(p.x, p.y);
      }
    }
    return new Area(path);
  }
}
