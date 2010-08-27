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

package jflowmap;

import java.io.IOException;
import java.util.Collections;

import javax.swing.UIManager;

import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import jflowmap.views.timeline.DuoTimelineView;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineApplet extends BaseApplet {

//  static {
//    // The statements below cause the objects to go straight for the default implementations
//    System.setProperty("javax.xml.parsers.SAXParserFactory",
//                                       "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
//    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//                                       "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
//    System.setProperty("javax.xml.transform.TransformerFactory",
//                                       "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
//  }

  public DuoTimelineApplet() {
    super("DuoTimeline");
  }


  @Override
  public void init() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
        System.err.println("Can't set look & feel:" + e);
    }
    super.init();
  }


  @Override
  protected IView createView() throws IOException {
    DatasetSpec ds = getDatasetSpec();
    String areaMapSrc = getParameter("areaMapSrc");
    AreaMap areaMap;
    if (areaMapSrc == null) {
      areaMap = new AreaMap(null, Collections.<Area>emptyList());
    } else {
      areaMap = AreaMap.load(areaMapSrc);
    }
    String maxVisibleTuples = getParameter("maxVisibleTuples");
    return new DuoTimelineView(
        FlowMapGraph.loadGraphML(ds.getFilename(), ds.getAttrsSpec()),
//        FlowMapGraphSet.loadGraphML(ds.getFilename(), ds.getAttrsSpec()),
        areaMap,
        maxVisibleTuples == null ? -1 : Integer.parseInt(maxVisibleTuples)
    );
  }


}
