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

import jflowmap.models.map.Area;
import jflowmap.models.map.Polygon;
import jflowmap.util.piccolo.PNodes;
import jflowmap.visuals.ColorCodes;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 */
public class VisualArea extends PNode {

  private static final long serialVersionUID = 1L;
  private static final PFixedWidthStroke mapStroke = new PFixedWidthStroke(1);
  private final VisualAreaMap visualAreaMap;

  public VisualArea(VisualAreaMap visualAreaMap, Area area) {
    this.visualAreaMap = visualAreaMap;
    for (Polygon poly : area.getPolygons()) {
      PPath path = PPath.createPolyline(poly.getPoints());
      addChild(path);
    }
    updateColors();
  }

  public void updateColors() {
    VisualFlowMap vfm = visualAreaMap.getVisualFlowMap();
    Color paint = vfm.getColor(ColorCodes.AREA_PAINT);
    Color strokePaint = vfm.getColor(ColorCodes.AREA_STROKE);
    for (PPath path : PNodes.childrenOfType(this, PPath.class)) {
      path.setPaint(paint);
      path.setStrokePaint(strokePaint);
      path.setStroke(mapStroke);
    }
  }
}
