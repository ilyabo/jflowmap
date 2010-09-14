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

package jflowmap;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import jflowmap.data.GraphMLReader3;
import jflowmap.data.XmlRegionsReader;
import jflowmap.views.timeline.FlowTimelineView;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class FlowTimelineApplet extends BaseApplet {

  private final static FlowMapAttrSpec REFUGEES_ATTR_SPECS = new FlowMapAttrSpec(
      // NOTE: using rityp and ritypnv is wrong, because the summaries then only include positive differences
//      "rity",
      "r",
      "name", "x", "y");


  private static final long serialVersionUID = 1L;

  public FlowTimelineApplet() {
    super("FlowTimelineApplet");
  }

  @Override
  protected IView createView() throws IOException {
      return loadGraphsWithRegions(getParameter("flowmaps"));
  }



  public static FlowTimelineView loadGraphsWithRegions(String filename) throws IOException {

    List<Graph> graphs = Lists.newArrayList(new GraphMLReader3().readFromLocation(filename));

    Collections.reverse(graphs);

    String columnToGroupNodesBy = "region";
    addRegionsAsNodeColumn(columnToGroupNodesBy, graphs);

    // TODO: let the user choose the attr specs
    FlowTimelineView ft = new FlowTimelineView(new FlowMapGraphSet(graphs, REFUGEES_ATTR_SPECS), columnToGroupNodesBy);
    return ft;
  }



  public static void addRegionsAsNodeColumn(String regionColumn, List<Graph> graphs) throws IOException {
    // TODO: introduce regions as node attrs in GraphML
    Map<String, String> nodeIdToRegion;
    try {
      nodeIdToRegion =
              XmlRegionsReader.readFrom("data/refugees/regions.xml");
//      XmlRegionsReader.readFrom("http://jflowmap.googlecode.com/svn/trunk/FlowMapView/data/refugees/regions.xml");
    } catch (XMLStreamException ex) {
      throw new IOException(ex);
    }
    for (Graph graph : graphs) {
      graph.getNodeTable().addColumn(regionColumn, String.class);
//      graph.getNodeTable().addColumn(FlowTimelineView.NODE_COLUMN__REGION_COLOR, int.class);

      for (Map.Entry<String, String> e : nodeIdToRegion.entrySet()) {
        Node node = FlowMapGraph.findNodeById(graph, e.getKey());
        if (node != null) {
          String region = e.getValue();
          node.set(regionColumn, region);
//          node.setInt(FlowTimelineView.NODE_COLUMN__REGION_COLOR, regionToColor.get(region));
        }
      }
    }
  }
}
