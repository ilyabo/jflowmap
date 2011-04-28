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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoints;
import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.Nodes;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.Area;
import jflowmap.models.map.AreaMap;
import jflowmap.models.map.Polygon;
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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
  private Map<String, Centroid> nodeIdsToCentroids;
  private List<String> selectedNodes;

  private final Lasso lasso;
  private PInputEventListener centroidMouseListener;


  public MapLayer(FlowstratesView flowstratesView, AreaMap areaMap, FlowEndpoints s) {
    this.flowstratesView = flowstratesView;
    this.endpoint = s;

    geoLayerCamera = new PCamera();
    geoLayerCamera.addLayer(this);

    visualAreaMap = new VisualAreaMap(
        flowstratesView.getMapColorScheme(), areaMap, flowstratesView.getMapProjection());

    addChild(visualAreaMap);
    geoLayerCamera.setPaint(visualAreaMap.getPaint());

    createCentroids();
    addMouseOverListenersToMaps(visualAreaMap);  // must be after createCentroids

    visualAreaMap.setBounds(visualAreaMap.getFullBoundsReference()); // enable mouse ev.

    lasso = createLasso(geoLayerCamera);
    geoLayerCamera.addInputEventListener(lasso);

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

  private void createCentroids() {
    nodeIdsToCentroids = Maps.newLinkedHashMap();

    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    // sort centroids so that more important are shown first
    Iterable<Node> nodes = CollectionUtils.sort(
        flowMapGraph.nodesHavingEdges(endpoint.dir()),
        Collections.reverseOrder(
            FlowMapSummaries.createMaxNodeWeightSummariesComparator(
                flowMapGraph, endpoint.dir())));

    centroidMouseListener = createCentroidMouseListener();

    Iterable<Node> nodesWithCoords = Iterables.filter(nodes, haveCoordsPredicate());
    Iterable<Node> nodesWithoutCoords = flowMapGraph.sortByAttr(
        Iterables.filter(nodes, Predicates.not(haveCoordsPredicate())),
        flowMapGraph.getNodeLabelAttr());

    createCentroidsForNodesWithCoords(nodesWithCoords);
    createCentroidsAndVisualAreasForNodesWithoutCoords(nodesWithoutCoords);
  }

  private void createCentroidsForNodesWithCoords(Iterable<Node> nodesWithCoords) {
    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

    for (Node node : nodesWithCoords) {
      Point2D p = visualAreaMap.getMapProjection().project(
          node.getDouble(fmg.getNodeLonAttr()),
          node.getDouble(fmg.getNodeLatAttr()));

      createCentroid(node, p.getX(), p.getY());
    }
  }

  private void createCentroidsAndVisualAreasForNodesWithoutCoords(Iterable<Node> nodesWithoutCoords) {
    Rectangle2D bounds = centroidsBounds();
    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();


    int num = Iterables.size(nodesWithoutCoords);
    if (num > 0) {
      final int maxPerRow = 4;
      int cnt = 0;
      final int numPerRow = Math.min(maxPerRow, num);
      final int r = num % numPerRow;
      final double hspacing = bounds.getWidth() * 0.7 / (numPerRow - 1);
      final double vspacing = (bounds.getHeight() / 10);
      final double topMargin = bounds.getHeight() / 5;

      for (Node node : nodesWithoutCoords) {
        final int numInThisRow = (cnt >= (num - r) ? r : numPerRow);
        double hcentering = (bounds.getWidth() - (numInThisRow - 1) * hspacing)/2;

        double x = hcentering + bounds.getMinX() + (cnt % numPerRow) * hspacing;
        double y = bounds.getMaxY() + topMargin + Math.floor(cnt / numPerRow) * vspacing;
        createCentroid(node, x, y);

        String nodeId = fmg.getNodeId(node);
        String label = fmg.getNodeLabel(node);

        visualAreaMap.addArea(new Area(nodeId, label, Arrays.asList(new Polygon(
            new Point2D[] {
                new Point2D.Double(x - hspacing/2, y - vspacing/3),
                new Point2D.Double(x + hspacing/2, y - vspacing/3),
                new Point2D.Double(x + hspacing/2, y + vspacing*2/3),
                new Point2D.Double(x - hspacing/2, y + vspacing*2/3),
                new Point2D.Double(x - hspacing/2, y - vspacing/3)
            }))),
            MapProjections.NONE);

        cnt++;
      }
    }
  }

  private void createCentroid(Node node, double x, double y) {
    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    String nodeId = flowMapGraph.getNodeId(node);
    String nodeLabel = flowMapGraph.getNodeLabel(node);
    Centroid c = new Centroid(
        nodeId, nodeLabel, x, y, CENTROID_DOT_SIZE,
        flowstratesView.getStyle().getMapAreaCentroidPaint(),
        flowstratesView);
    // c.setPickable(false);

    c.addInputEventListener(centroidMouseListener);
    geoLayerCamera.addChild(c);
    nodeIdsToCentroids.put(c.getNodeId(), c);
  }

  private Predicate<Node> haveCoordsPredicate() {
    return new Predicate<Node>() {
      @Override
      public boolean apply(Node node) {
        FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

        double lon = node.getDouble(fmg.getNodeLonAttr());
        double lat = node.getDouble(fmg.getNodeLatAttr());

        return (!Double.isNaN(lon)  &&  !Double.isNaN(lat));
      }
    };
  }

  void setSelectedNodes(List<String> nodeIds) {
    if (selectedNodes != nodeIds) {
      flowstratesView.setCustomEdgeFilter(null);
      List<String> old = selectedNodes;
      selectedNodes = (nodeIds != null ? ImmutableList.copyOf(nodeIds) : null);
      flowstratesView.fireNodeSelectionChanged(old, nodeIds);
      flowstratesView.updateVisibleEdges();
      updateCentroidColors();
      flowstratesView.fitHeatmapInView();
    }
  }

  private Lasso createLasso(PCamera targetCamera) {
    return new Lasso(targetCamera, flowstratesView.getStyle().getLassoStrokePaint(endpoint)) {
      @Override
      public void selectionMade(Shape shape) {
        setSelectedNodes(applyLassoToNodeCentroids(shape));
      }
    };
  }

  /**
   * @returns List of ids of selected nodes, or null if no nodes were selected
   */
  private List<String> applyLassoToNodeCentroids(Shape shape) {
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
    for (Map.Entry<String, Centroid> e : nodeIdsToCentroids.entrySet()) {
      String nodeId = e.getKey();
      Centroid centroid = e.getValue();
      centroid.setSelected(nodeSelectionContains(nodeId));
    }
  }

  private PInputEventListener createCentroidMouseListener() {
    return new PTypedBasicInputEventHandler<Centroid>(Centroid.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        if (lasso.isSelecting()) return;

        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), true);
        }
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        if (lasso.isSelecting()) return;

        Centroid centroid = node(event);
        if (centroid != null) {
          String nodeId = centroid.getNodeId();
          onCentroidOrAreaMouseClicked(event, nodeId);
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
        if (lasso.isSelecting()) return;

        if (event.isControlDown()) return;

        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          if (nodeIdsToCentroids.containsKey(areaId)) {
            setNodeHighlighted(areaId, true);
          }
        }
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        if (lasso.isSelecting()) return;

        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          if (nodeIdsToCentroids.containsKey(areaId)) {
            onCentroidOrAreaMouseClicked(event, areaId);
          }
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

  private void onCentroidOrAreaMouseClicked(PInputEvent event, String nodeId) {
    List<String> newSelection;
    if (isNodeSelectionEmpty()) {
      newSelection = Arrays.asList(nodeId);
    } else {
      if (event.isControlDown()) {
        newSelection = Lists.newArrayList(selectedNodes);
        if (selectedNodes.contains(nodeId)) {
          newSelection.remove(nodeId);
        } else {
          newSelection.add(nodeId);
        }
      } else {
        newSelection = Arrays.asList(nodeId);
      }
    }
    setSelectedNodes(newSelection);
  }

  private void colorizeMapArea(String areaId, double value, boolean hover) {
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

  void updateMapAreaColorsOnHeatmapCellHover(HeatmapCell cell, boolean hover) {
    Edge edge = cell.getEdge();
    String attr = cell.getWeightAttr();

    if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {

      List<Edge> edges = FlowMapGraphEdgeAggregator.getBaseAggregateList(edge);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, endpoint);

    } else {

      ValueType vtype = flowstratesView.getValueType();
      FlowMapAttrSpec attrSpec = getFlowMapGraph().getAttrSpec();

      double value = edge.getDouble(vtype.getColumnValueAttr(attrSpec, attr));
      colorizeMapArea(flowstratesView.getAggLayers().getNodeId(edge, endpoint), value, hover);
    }
  }


  void updateOnHeatmapColumnHover(String columnAttr, boolean hover) {
    Iterable<Edge> edges;
    if (flowstratesView.isFilterApplied()) {
      edges = flowstratesView.getVisibleEdges();
    } else {
      edges = getFlowMapGraph().edges();
    }
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, endpoint);
  }


  private void colorizeMapAreasWithBaseNodeSummaries(String weightAttr, boolean hover, Iterable<Edge> edges,
      FlowEndpoints ep) {
    for (Node node : Nodes.nodesOfEdges(edges, ep)) {
      double value = FlowMapSummaries.getWeightSummary(node, getColumnValueAttrName(weightAttr), ep.dir());
      String areaId = getFlowMapGraph().getNodeId(node);
      colorizeMapArea(areaId, value, hover);
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

  Rectangle2D centroidsBounds() {
    Rectangle2D.Double b = new Rectangle2D.Double();
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
    if (nodeId == null) {
      return;
    }
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
      Pair<FlowLine, FlowLine> pair = flowstratesView.getFlowLinesLayerNode().getFlowLinesOf(e);
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

  public boolean isNodeSelectionEmpty() {
    return selectedNodes == null  ||  selectedNodes.isEmpty();
  }

  public boolean nodeSelectionContains(String nodeId) {
    if (selectedNodes == null) {
      return false;
    }
    return selectedNodes.contains(nodeId);
  }

  public boolean nodeSelectionContains(Node node) {
    return nodeSelectionContains(getFlowMapGraph().getNodeId(node));
  }

  public boolean clearNodeSelection() {
    if (selectedNodes == null) {
      return false;
    } else {
      selectedNodes = null;
      updateCentroidColors();
      return true;
    }
  }

}
