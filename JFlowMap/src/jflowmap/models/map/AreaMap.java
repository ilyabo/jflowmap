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

package jflowmap.models.map;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import jflowmap.data.DatasetSpec;
import jflowmap.data.ShapefileReader;
import jflowmap.data.XmlAreaMapModelReader2;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;


/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class AreaMap {

  private static Logger logger = Logger.getLogger(AreaMap.class);

  private final String name;
  private final List<MapArea> areas;

  public AreaMap(String name, List<MapArea> areas) {
    this.name = name;
    this.areas = ImmutableList.copyOf(areas);
  }

  public String getName() {
    return name;
  }

  public Collection<MapArea> getAreas() {
    return areas;
  }

  public static AreaMap loadFor(DatasetSpec dataset) throws IOException {
    AreaMap areaMap = null;
    if (dataset.getAreaMapFilename() != null) {
      areaMap = load(dataset.getAreaMapFilename());
    } else if (dataset.getShapefileName() != null) {
      areaMap = asAreaMap(ShapefileReader.loadShapefile(
          dataset.getShapefileName(), dataset.getDbfAreaIdField()));
    }
    return areaMap;
  }

  public static final AreaMap load(String filename) throws IOException {
    logger.info("Loading area map '" + filename + "'");
    return XmlAreaMapModelReader2.readMap(filename);
  }

  public static AreaMap asAreaMap(GeometryCollection geoms) {
    List<MapArea> list = Lists.newArrayList();
    for (int i = 0; i < geoms.getNumGeometries(); i++) {
      Geometry g = geoms.getGeometryN(i);
      String name = (g.getUserData() != null ? g.getUserData().toString() : null);
      list.add(MapArea.fromGeometry(name, name, g));
    }
    return new AreaMap(null, list);
  }
}
