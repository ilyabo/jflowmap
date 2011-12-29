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

import jflowmap.data.XmlAreaMapModelReader2;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Geometry;


/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class GeoMap {

  private static Logger logger = Logger.getLogger(GeoMap.class);

  private final String name;
  private final List<MapArea> areas;

  public GeoMap(String name, List<MapArea> areas) {
    this.name = name;
    this.areas = ImmutableList.copyOf(areas);
  }

  public String getName() {
    return name;
  }

  public Collection<MapArea> getAreas() {
    return areas;
  }

  public static final GeoMap load(String filename) throws IOException {
    logger.info("Loading area map '" + filename + "'");
    return XmlAreaMapModelReader2.readMap(filename);
  }

  public static GeoMap asAreaMap(Iterable<Geometry> geoms) {
    List<MapArea> list = Lists.newArrayList();
    for (Geometry g : geoms) {
      String name = (g.getUserData() != null ? g.getUserData().toString() : null);
      list.add(MapArea.fromGeometry(name, name, g));
    }
    return new GeoMap(null, list);
  }
}
