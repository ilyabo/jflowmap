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

import java.awt.Shape;
import java.awt.geom.Line2D;

import prefuse.data.Edge;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class LineVisualEdge extends VisualEdge {

  private static final long serialVersionUID = 1L;

  public LineVisualEdge(VisualFlowMap visualFlowMap, Edge edge,
      VisualNode sourceNode, VisualNode targetNode) {
    super(visualFlowMap, edge, sourceNode, targetNode);

    targetNode.addIncomingEdge(this);
    sourceNode.addOutgoingEdge(this);

    init();
  }

  @Override
  protected PPath createEdgePPath() {
    final double x1 = getSourceX();
    final double y1 = getSourceY();
    final double x2 = getTargetX();
    final double y2 = getTargetY();

    Shape shape;
    if (isSelfLoop()) {
      shape = createSelfLoopShape();

    } else {
      shape = new Line2D.Double(x1, y1, x2, y2);
    }

    PPath ppath = new PPath(shape);
    return ppath;
  }
}
