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

package jflowmap.visuals;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

import java.awt.*;

import jflowmap.models.map.Area;
import jflowmap.models.map.Polygon;

/**
 */
public class VisualArea extends PNode {

    private static final Color mapPaintColor = new Color(45, 45, 45);
    private static final Color mapStrokeColor = new Color(55, 55, 55);
//    private static final Color mapPaintColor = new Color(235, 235, 235);
//    private static final Color mapStrokeColor = new Color(225, 225, 225);
    private static final PFixedWidthStroke mapStroke = new PFixedWidthStroke(1);

    public VisualArea(Area area) {
        for (Polygon poly : area.getPolygons()) {
            PPath path = PPath.createPolyline(poly.getPoints());
            path.setPaint(mapPaintColor);
            path.setStrokePaint(mapStrokeColor);
            path.setStroke(mapStroke);
            addChild(path);
        }
    }
}
