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

package jflowmap.data;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import jflowmap.models.map.Polygon;
import prefuse.util.io.IOLib;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class XmlAreaMapModelReader2 {

  private static String charset = "utf-8";

  private XmlAreaMapModelReader2() {
  }

  public static AreaMap readMap(String location) throws IOException {
    return loadFrom(location, IOLib.streamFromString(location));
  }

  private static AreaMap loadFrom(String name, InputStream is) throws IOException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLStreamReader in;
    LineNumberReader lineNumberReader = null;
    try {
      lineNumberReader = new LineNumberReader(new InputStreamReader(is, charset));
      in = inputFactory.createXMLStreamReader(lineNumberReader);

      List<Area> areas = Lists.newArrayList();
      List<Polygon> polygons = Lists.newArrayList();

      String areaId = null;

      OUTER: while (in.hasNext()) {
        int eventType = in.nextTag();
        String tag = in.getLocalName();

        switch (eventType) {
          case XMLStreamReader.START_ELEMENT:

            if (tag.equals("area")) {
              areaId = in.getAttributeValue(null, "id");

            } else if (tag.equals("polygons")) {
              polygons.clear();

            } else if (tag.equals("poly")) {
              String coordsStr = in.getElementText();
              String[] coords = coordsStr.split("\\s*,\\s*");
              Point2D[] points = new Point2D[coords.length / 2];
              double x = Double.NaN, y;  // lat, lon
              int coordCnt = 0;
              for (String point : coords) {
                if (coordCnt % 2 == 0) {
                  x = Double.parseDouble(point);
                } else {
                  y = Double.parseDouble(point);
                  points[coordCnt / 2] = new Point2D.Double(y, x);
                }
                coordCnt++;
              }
              polygons.add(new Polygon(points));
            }
            break;

          case XMLStreamReader.END_ELEMENT:

            if (tag.equals("areas")) {
              break OUTER;
            } else if (tag.equals("area")) {
              areas.add(new Area(areaId, "", polygons));
            }

            break;
        }
      }

      return new AreaMap(name, areas);
    } catch (Exception e) {
      throw new IOException("Cannot load '" + name + "': " + e.getMessage() +
          (lineNumberReader != null ?
              " (in line " + lineNumberReader.getLineNumber() + ")" : ""), e);
    }


  }

}
