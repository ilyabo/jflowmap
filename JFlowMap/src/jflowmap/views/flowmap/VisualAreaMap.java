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

import java.awt.geom.Rectangle2D;

import jflowmap.geo.MapProjection;
import jflowmap.models.map.MapArea;
import jflowmap.models.map.AreaMap;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class VisualAreaMap extends PNode {

  private static final long serialVersionUID = 1L;
  private final ColorSchemeAware colorSchemaAware;
  private final AreaMap mapModel;
  private final MapProjection mapProjection;
  private Rectangle2D boundingBox;

  public VisualAreaMap(VisualAreaMap toCopy) {
    this(toCopy.getColorSchemaAware(), toCopy.mapModel, toCopy.mapProjection);
  }

  public VisualAreaMap(ColorSchemeAware cs, AreaMap mapModel, MapProjection proj) {
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
      for (VisualArea va : PNodes.childrenOfType(this, VisualArea.class)) {
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

  public VisualArea addArea(MapArea area) {
    return addArea(area, mapProjection);
  }

  public VisualArea addArea(MapArea area, MapProjection proj) {
    VisualArea visualArea = createVisualArea(area, proj);
    addChild(visualArea);
    boundingBox = null;
    return visualArea;
  }

  private VisualArea createVisualArea(MapArea area, MapProjection proj) {
    return new VisualArea(this, area, proj);
  }

  public MapProjection getMapProjection() {
    return mapProjection;
  }

  public ColorSchemeAware getColorSchemaAware() {
    return colorSchemaAware;
  }

  public void updateColors() {
    for (VisualArea va : PNodes.childrenOfType(this, VisualArea.class)) {
      va.updateColors();
    }
  }

  public VisualArea getVisualAreaBy(String id) {
    for (VisualArea va : PNodes.childrenOfType(this, VisualArea.class)) {
      String vaid = va.getArea().getId();
      if (vaid != null  &&  id.equals(vaid)) return va;
    }
    return null;
  }
}
