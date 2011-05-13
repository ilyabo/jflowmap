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

import java.awt.geom.Rectangle2D;

import jflowmap.geo.MapProjection;
import jflowmap.models.map.MapArea;
import jflowmap.models.map.GeoMap;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import jflowmap.views.flowmap.ColorSchemeAware;
import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class PGeoMap extends PNode {

  private static final long serialVersionUID = 1L;
  private final ColorSchemeAware colorSchemaAware;
  private final GeoMap mapModel;
  private final MapProjection mapProjection;
  private Rectangle2D boundingBox;

  public PGeoMap(PGeoMap toCopy) {
    this(toCopy.getColorSchemaAware(), toCopy.mapModel, toCopy.mapProjection);
  }

  public PGeoMap(ColorSchemeAware cs, GeoMap mapModel, MapProjection proj) {
    this.mapModel = mapModel;
    this.colorSchemaAware = cs;
    this.mapProjection = proj;
    for (MapArea area : mapModel.getAreas()) {
      if (!area.isEmpty()) {
        addArea(area);
      }
    }
    setPaint(getColorSchemaAware().getColor(ColorCodes.BACKGROUND));
  }

  public Rectangle2D getBoundingBox() {
    if (boundingBox == null) {
      Rectangle2D b = null;
      for (PGeoMapArea va : PNodes.childrenOfType(this, PGeoMapArea.class)) {
        if (b == null) {
          b = va.getBoundingBox();
        } else {
          b.add(va.getBoundingBox());
        }
      }
      boundingBox = b;
    }
    return (Rectangle2D)boundingBox.clone();
  }

  public PGeoMapArea addArea(MapArea area) {
    return addArea(area, mapProjection);
  }

  public PGeoMapArea addArea(MapArea area, MapProjection proj) {
    PGeoMapArea visualArea = createVisualArea(area, proj);
    addChild(visualArea);
    boundingBox = null;
    return visualArea;
  }

  private PGeoMapArea createVisualArea(MapArea area, MapProjection proj) {
    return new PGeoMapArea(this, area, proj);
  }

  public MapProjection getMapProjection() {
    return mapProjection;
  }

  public ColorSchemeAware getColorSchemaAware() {
    return colorSchemaAware;
  }

  public void updateColors() {
    for (PGeoMapArea va : PNodes.childrenOfType(this, PGeoMapArea.class)) {
      va.updateColors();
    }
  }

  public PGeoMapArea getVisualAreaBy(String id) {
    for (PGeoMapArea va : PNodes.childrenOfType(this, PGeoMapArea.class)) {
      String vaid = va.getArea().getId();
      if (vaid != null  &&  id.equals(vaid)) return va;
    }
    return null;
  }
}
