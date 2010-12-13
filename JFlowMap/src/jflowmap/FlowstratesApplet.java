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

import java.util.Collections;

import javax.swing.UIManager;

import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import jflowmap.views.flowstrates.AggLayersBuilder;
import jflowmap.views.flowstrates.FlowstratesView;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesApplet extends BaseApplet {
  public static Logger logger = Logger.getLogger(FlowstratesApplet.class);

//  static {
//    // The statements below cause the objects to go straight for the default implementations
//    System.setProperty("javax.xml.parsers.SAXParserFactory",
//                                       "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
//    System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//                                       "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
//    System.setProperty("javax.xml.transform.TransformerFactory",
//                                       "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
//  }

  public FlowstratesApplet() {
    super("Flowstrates");
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
  protected IView createView() throws Exception {
    AreaMap areaMap = AreaMap.loadFor(getDatasetSpec());
    String maxVisibleTuples = getParameter("maxVisibleTuples");
    String aggName = getParameter("aggLayersBuilder");
    AggLayersBuilder aggBuilder = null;
    if (aggName != null) {
      try {
        aggBuilder = (AggLayersBuilder)Class.forName(aggName).newInstance();
      } catch (Exception e) {
        logger.error(e);
        throw e;
      }
    }
    if (areaMap == null) {
      areaMap = new AreaMap("<Empty>", Collections.<Area>emptyList());
    }
    return new FlowstratesView(
        FlowMapGraph.loadGraphML(getDatasetSpec()),
        areaMap,
        aggBuilder,
        maxVisibleTuples == null ? -1 : Integer.parseInt(maxVisibleTuples)
    );
  }


}
