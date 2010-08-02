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

import jflowmap.models.map.Area;
import jflowmap.views.ColorCodes;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 */
public class VisualArea extends PPath {

  private static final long serialVersionUID = 1L;
  private static final PFixedWidthStroke mapStroke = new PFixedWidthStroke(1);
  private final VisualAreaMap visualAreaMap;
  private final Area area;

  public VisualArea(VisualAreaMap visualAreaMap, Area area) {
    super(area.asPath(visualAreaMap.getMapProjection()));
    this.visualAreaMap = visualAreaMap;
    this.area = area;
    updateColors();
  }

  public Area getArea() {
    return area;
  }

  public void updateColors() {
    ColorSchemeAware cs = visualAreaMap.getColorSchemaAware();
    Color paint = cs.getColor(ColorCodes.AREA_PAINT);
    Color strokePaint = cs.getColor(ColorCodes.AREA_STROKE);
    setPaint(paint);
    setStrokePaint(strokePaint);
    setStroke(mapStroke);
  }

}
