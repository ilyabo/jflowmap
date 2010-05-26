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

package jflowmap.visuals.flowmap;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;

import jflowmap.geom.BSplinePath;
import jflowmap.geom.Point;
import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class BSplineVisualEdge extends VisualEdge {

  private static final Color DOT_COLOR = new Color(255,0,0,100);

  private static final long serialVersionUID = 1L;

//  private List<Point> subdivisionPoints;

  public BSplineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
      VisualNode sourceNode, VisualNode targetNode, List<Point> points,
      boolean showSplinePoints) {
    super(visualFlowMap, edge, sourceNode, targetNode);

    int numPoints = points.size();
    assert numPoints >= 2;

//    subdivisionPoints = ImmutableList.copyOf(Arrays.asList(points));
    Point start = points.get(0);
    Point end = points.get(numPoints - 1);
    assert(start.x() == sourceNode.getValueX());
    assert(start.y() == sourceNode.getValueY());
    assert(end.x() == targetNode.getValueX());
    assert(end.y() == targetNode.getValueY());

    Shape shape;
    if (isSelfLoop()) {
      shape = createSelfLoopShape();
    } else {
      Path2D path;
      if (numPoints < 4) {
        path = new Path2D.Double();
        path.moveTo(start.x(), start.y());
        for (int i = 1; i < numPoints; i++) {
          Point point = points.get(i);
          path.lineTo(point.x(), point.y());
        }
      } else {
        path = new BSplinePath(points);
      }

      // add spline points
      if (showSplinePoints) {
        final double d = visualFlowMap.getStats().getEdgeLengthStats().getMax() / 100;
        int cnt = 0;
        for (Point p : points) {
          PPath ell = new PPath(new Ellipse2D.Double(p.x()-d/2, p.y()-d/2, d, d));
          ell.setStrokePaint(DOT_COLOR);
          ell.setPaint(DOT_COLOR);
          ell.moveToFront();
          addChild(ell);
          cnt++;
        }
      }

      shape = path;
    }

    PPath ppath = new PPath(shape);
    setEdgePPath(ppath);
    addChild(ppath);

//    update();
  }

}
