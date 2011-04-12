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

package jflowmap.views.flowstrates;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoints;
import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.Nodes;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.Lasso;
import jflowmap.util.CollectionUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.flowmap.VisualArea;
import jflowmap.views.flowmap.VisualAreaMap;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;

/**
 * @author Ilya Boyandin
 */
public class MapLayer extends PLayer {

  private static final double CENTROID_DOT_SIZE = 2.0;

  private final PCamera geoLayerCamera;
  private final VisualAreaMap visualAreaMap;
  private final FlowstratesView flowstratesView;
  private final FlowEndpoints endpoint;
  private final Map<String, Centroid> nodeIdsToCentroids;


  public MapLayer(FlowstratesView flowstratesView, AreaMap areaMap, FlowEndpoints s) {
    this.flowstratesView = flowstratesView;
    this.endpoint = s;

    geoLayerCamera = new PCamera();
    geoLayerCamera.addLayer(this);

    visualAreaMap = new VisualAreaMap(
        flowstratesView.getMapColorScheme(),
        areaMap, flowstratesView.getMapProjection());

    addChild(visualAreaMap);
    geoLayerCamera.setPaint(visualAreaMap.getPaint());

    addMouseOverListenersToMaps(visualAreaMap);

    nodeIdsToCentroids = createCentroids();

    visualAreaMap.setBounds(visualAreaMap.getFullBoundsReference()); // enable mouse ev.

    geoLayerCamera.addInputEventListener(createLasso(geoLayerCamera));
  }

  public PCamera getMapLayerCamera() {
    return geoLayerCamera;
  }

  public VisualAreaMap getVisualAreaMap() {
    return visualAreaMap;
  }

  public Map<String, Centroid> getNodeIdsToCentroids() {
    return nodeIdsToCentroids;
  }

  private FlowMapGraph getFlowMapGraph() {
    return flowstratesView.getFlowMapGraph();
  }

  void updateCentroids() {
    RectSet occupied = new RectSet(nodeIdsToCentroids.size());
    for (Centroid c : nodeIdsToCentroids.values()) {
      c.updateInCamera(geoLayerCamera);
      if (c.getVisible()) {
        c.getLabelNode().setVisible(occupied.addIfNotIntersects(c.getLabelNode().getBounds()));
      }
    }
  }

  private Centroid createCentroid(double x, double y, Node node) {
    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    String nodeId = flowMapGraph.getNodeId(node);
    String nodeLabel = flowMapGraph.getNodeLabel(node);
    Centroid c = new Centroid(
        nodeId, nodeLabel, x, y, CENTROID_DOT_SIZE,
        flowstratesView.getStyle().getMapAreaCentroidPaint(),
        flowstratesView);
    // c.setPickable(false);
    return c;
  }

  private Map<String, Centroid> createCentroids() {
    Map<String, Centroid> map = Maps.newLinkedHashMap();

    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    // sort centroids so that more important are shown first
    Iterable<Node> nodes = CollectionUtils.sort(
        flowMapGraph.nodesHavingEdges(endpoint.dir()),
        Collections.reverseOrder(
            FlowMapSummaries.createMaxNodeWeightSummariesComparator(flowMapGraph,
            endpoint.dir())));

    PInputEventListener listener = createCentroidHoverListener();

    for (Node node : nodes) {
      double lon = node.getDouble(flowMapGraph.getXNodeAttr());
      double lat = node.getDouble(flowMapGraph.getYNodeAttr());

      Point2D p = visualAreaMap.getMapProjection().project(lon, lat);

      Centroid centroid = createCentroid(p.getX(), p.getY(), node);
      centroid.addInputEventListener(listener);

      geoLayerCamera.addChild(centroid);
      // VisualArea va = getVisualAreaMap(s).getVisualAreaBy(flowMapGraph.getNodeId(node));

      map.put(flowMapGraph.getNodeId(node), centroid);
    }
    return map;
  }

  private Lasso createLasso(PCamera targetCamera) {
    return new Lasso(targetCamera, flowstratesView.getStyle().getLassoStrokePaint(endpoint)) {
      @Override
      public void selectionMade(Shape shape) {
        flowstratesView.setCustomEdgeFilter(null);
        flowstratesView.setSelectedNodes(applyLassoToNodeCentroids(shape, endpoint), endpoint);
        flowstratesView.updateVisibleEdges();
        updateCentroidColors();
      }
    };
  }

  /**
   * @returns List of ids of selected nodes, or null if no nodes were selected
   */
  private List<String> applyLassoToNodeCentroids(Shape shape, FlowEndpoints s) {
    List<String> nodeIds = null;
    for (Map.Entry<String, Centroid> e : nodeIdsToCentroids.entrySet()) {
      Centroid centroid = e.getValue();
      if (shape.contains(centroid.getPoint())) {
        if (nodeIds == null) {
          nodeIds = Lists.newArrayList();
        }
        nodeIds.add(e.getKey());
      }
    }
    return nodeIds;
  }


  Point2D getCentroidPoint(Edge edge) {
    Centroid centroid = nodeIdsToCentroids.get(getFlowMapGraph().getNodeId(endpoint.nodeOf(edge)));
    Point2D point;
    if (centroid == null) {
      point = null;
    } else {
      point = centroid.getPoint();
    }
    return point;
  }

  void updateCentroidColors() {
    List<String> selNodes = flowstratesView.getSelectedNodes(endpoint);
    for (Map.Entry<String, Centroid> e : nodeIdsToCentroids.entrySet()) {
      String nodeId = e.getKey();
      Centroid centroid = e.getValue();
      centroid.setSelected(selNodes != null && selNodes.contains(nodeId));
    }
  }

  private PInputEventListener createCentroidHoverListener() {
    return new PTypedBasicInputEventHandler<Centroid>(Centroid.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), false);
        }
      }
    };
  }

  private void addMouseOverListenersToMaps(VisualAreaMap visualAreaMap) {
    PInputEventListener listener = new PTypedBasicInputEventHandler<VisualArea>(VisualArea.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        if (event.isControlDown())
          return;
        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          setNodeHighlighted(areaId, true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          setNodeHighlighted(areaId, false);
        }
      }
    };
    for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
      va.addInputEventListener(listener);
    }
  }

  private void colorizeMapArea(String areaId, double value, boolean hover, FlowEndpoints s) {
    Centroid c = nodeIdsToCentroids.get(areaId);
    if (c != null) {
      VisualArea area = visualAreaMap.getVisualAreaBy(areaId);
      if (area != null && !area.isEmpty()) {
        Color color;
        if (hover) {
          color = flowstratesView.getColorFor(value);
        } else {
          color = flowstratesView.getMapColorScheme().getColor(ColorCodes.AREA_PAINT);
        }
        area.setPaint(color);
      } else {
        Color color;
        if (hover) {
          color = flowstratesView.getColorFor(value);
        } else {
          color = flowstratesView.getStyle().getMapAreaCentroidLabelPaint();
        }
        c.getLabelNode().setPaint(color);
      }
    }
  }

  void updateMapAreaColorsOnHeatMapCellHover(HeatmapCell cell, boolean hover) {
    Edge edge = cell.getEdge();
    String attr = cell.getWeightAttr();

    if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {

      List<Edge> edges = FlowMapGraphEdgeAggregator.getBaseAggregateList(edge);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, endpoint);

    } else {

      ValueType vtype = flowstratesView.getValueType();
      FlowMapAttrSpec attrSpec = getFlowMapGraph().getAttrSpec();

      double value = edge.getDouble(vtype.getColumnValueAttr(attrSpec, attr));
      colorizeMapArea(flowstratesView.getAggLayers().getSourceNodeId(edge), value, hover, endpoint);
    }
  }


  void updateMapAreaColorsOnHeatMapColumnLabelHover(String columnAttr, boolean hover) {
    Iterable<Edge> edges;
    if (flowstratesView.isFilterApplied()) {
      edges = flowstratesView.getVisibleEdges();
    } else {
      edges = getFlowMapGraph().edges();
    }
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, endpoint);
  }


  private void colorizeMapAreasWithBaseNodeSummaries(String weightAttr, boolean hover, Iterable<Edge> edges,
      FlowEndpoints s) {
    for (Node node : Nodes.nodesOfEdges(edges, s)) {
      double value = FlowMapSummaries.getWeightSummary(node, getColumnValueAttrName(weightAttr), s.dir());
      String areaId = getFlowMapGraph().getNodeId(node);
      colorizeMapArea(areaId, value, hover, s);
    }
  }

  private String getColumnValueAttrName(String columnAttr) {
    ValueType vtype = flowstratesView.getValueType();

    return vtype.getColumnValueAttr(getFlowMapGraph().getAttrSpec(), columnAttr);
  }

  void setVisualAreaMapHighlighted(String nodeId, boolean highlighted) {
    VisualArea va = visualAreaMap.getVisualAreaBy(nodeId);
    FlowstratesStyle style = flowstratesView.getStyle();

    if (va != null) {
      va.moveToFront();
      if (highlighted) {
        va.setStroke(style.getMapAreaHighlightedStroke());
        va.setStrokePaint(style.getMapAreaHighlightedStrokePaint());
        va.setPaint(style.getMapAreaHighlightedPaint());
      } else {
        ColorSchemeAware cs = flowstratesView.getMapColorScheme();

        va.setStroke(style.getMapAreaStroke());
        va.setStrokePaint(cs.getColor(ColorCodes.AREA_STROKE));
        va.setPaint(cs.getColor(ColorCodes.AREA_PAINT));
      }
      va.repaint();
    }
  }

  Rectangle2D centroidsBounds(FlowstratesView flowstratesView) {
    Double b = new Double();
    boolean first = true;
    for (Centroid c : nodeIdsToCentroids.values()) {
      double x = c.getOrigX();
      double y = c.getOrigY();
      if (first) {
        b.x = x;
        b.y = y;
        first = false;
      } else {
        if (x < b.x) {
          b.x = x;
        }
        if (x > b.getMaxX()) {
          b.width = x - b.x;
        }
        if (y < b.y) {
          b.y = y;
        }
        if (y > b.getMaxY()) {
          b.height = y - b.y;
        }
      }
    }
    // getVisualAreaMapCamera(s).localToView(b);

    return b;
  }


  void setEdgeCentroidsHighlighted(HeatmapCell hmcell, boolean highlighted) {
    Node node = getFlowMapGraph().getNodeOf(hmcell.getEdge(), endpoint);
    Centroid c = nodeIdsToCentroids.get(getFlowMapGraph().getNodeId(node));
    if (c != null) {
      c.setHighlighted(highlighted);
    }
  }

  public void setNodeHighlighted(final String nodeId, boolean highlighted) {
    Centroid c = nodeIdsToCentroids.get(nodeId);
    if (c != null) {
      c.setHighlighted(highlighted);
      c.setTimeSliderVisible(highlighted);
    }
    Predicate<Node> acceptNodes = new Predicate<Node>() {
      @Override
      public boolean apply(Node node) {
        return nodeId.equals(getFlowMapGraph().getNodeId(node));
      }
    };
    for (Edge e : endpoint.filterByNodePredicate(flowstratesView.getVisibleEdges(), acceptNodes)) {
      Pair<FlowLine, FlowLine> pair = flowstratesView.getFlowLinesOf(e);
      if (pair != null) {
        pair.first().setHighlighted(highlighted);
        pair.second().setHighlighted(highlighted);
        // if (showFlowtiLinesForHighligtedNodesOnly) {
        // pair.first().setVisible(highlighted);
        // pair.second().setVisible(highlighted);
        // }
      }
    }

    setVisualAreaMapHighlighted(nodeId, highlighted);
  }


}
