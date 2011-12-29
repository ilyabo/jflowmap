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

package jflowmap.views.map;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import jflowmap.geo.MapProjection;
import jflowmap.models.map.MapArea;
import jflowmap.views.ColorCodes;
import jflowmap.views.flowmap.ColorSchemeAware;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 */
public class PGeoMapArea extends PPath {

  private static final long serialVersionUID = 1L;
  private static final PFixedWidthStroke mapStroke = new PFixedWidthStroke(1);
  private final PGeoMap visualAreaMap;
  private final MapArea area;
  private final Rectangle2D boundingBox;
//  private PActivity lastActivity;

  public PGeoMapArea(PGeoMap visualAreaMap, MapArea area, MapProjection proj) {
    super(area.asPath(proj));
    this.visualAreaMap = visualAreaMap;
    this.area = area;
    this.boundingBox = area.asBoundingBox(proj);
    updateColors();
  }

  public PGeoMapArea(PGeoMap visualAreaMap, MapArea area) {
    this(visualAreaMap, area, visualAreaMap.getMapProjection());
  }

  public MapArea getArea() {
    return area;
  }

  public Rectangle2D getBoundingBox() {
    return (Rectangle2D)boundingBox.clone();
  }

  public void updateColors() {
    ColorSchemeAware cs = visualAreaMap.getColorSchemaAware();
    Color paint = cs.getColor(ColorCodes.AREA_PAINT);
    Color strokePaint = cs.getColor(ColorCodes.AREA_STROKE);
    setPaint(paint);
    setStrokePaint(strokePaint);
    setStroke(mapStroke);
  }

  @Override
  public void setPaint(Paint newPaint) {
    super.setPaint(newPaint);
    repaint();
  }

//  @Override
//  public boolean addActivity(PActivity activity) {
//    if (lastActivity != null && lastActivity.isStepping()) {
//      lastActivity.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
//      lastActivity = null;
//    }
//    if (super.addActivity(activity)) {
//      lastActivity = activity;
//      return true;
//    } else {
//      return false;
//    }
//  }

  public boolean isEmpty() {
    return getBoundsReference().isEmpty();
  }
}
