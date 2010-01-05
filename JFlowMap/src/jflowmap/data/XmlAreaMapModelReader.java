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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import jflowmap.models.map.Polygon;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

import prefuse.util.io.IOLib;

/**
 * @author Ilya Boyandin
 *         Date: 25-Sep-2009
 */
public class XmlAreaMapModelReader {

    private XmlAreaMapModelReader() {
    }

    public static AreaMap readMap(String location) throws IOException {
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        try {
            return loadFrom(location, builder.parseReader(new InputStreamReader(IOLib.streamFromString(location))));
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static AreaMap loadFrom(String name, XmlDocument doc) throws XmlPullParserException, IOException {
        Xb1XPath
                areaPath = new Xb1XPath("/areas/area"),
                polygonPath = new Xb1XPath("polygons"),
                polyPath = new Xb1XPath("poly");


        List<Area> areas = new ArrayList<Area>();
        List<XmlElement> areaNodes = areaPath.selectNodes(doc);
        for (XmlElement areaNode : areaNodes) {
            String id = areaNode.getAttributeValue(null, "id");
            for (XmlElement polygonsNode : (List<XmlElement>) polygonPath.selectNodes(areaNode)) {
                List<XmlElement> polyNodes = polyPath.selectNodes(polygonsNode);
                Polygon[] polygons = new Polygon[polyNodes.size()];
                int polyCnt = 0;
                for (XmlElement polyNode : polyNodes) {
                    Iterator it = polyNode.children();
                    if (it.hasNext()) {
//                        java.awt.geom.Area poly = new java.awt.geom.Area();
                        String coordsStr = it.next().toString().trim();
                        String[] coords = coordsStr.split("\\s*,\\s*");
                        Point2D[] points = new Point2D[coords.length / 2];
                        double x = Double.NaN, y;
                        int coordCnt = 0;
                        for (String point : coords) {
                            if (coordCnt % 2 == 0) {
                                x = Double.parseDouble(point);
                            } else {
                                y = Double.parseDouble(point);
                                points[coordCnt / 2] = new Point2D.Double(x, y);
                            }
                            coordCnt++;
                        }
                        polygons[polyCnt] = new Polygon(points);
                        polyCnt++;
                    }

                }
                areas.add(new Area(id, "", polygons));
            }
        }

        return new AreaMap(name, areas);
    }
}
