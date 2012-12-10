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

import static jflowmap.data.FlowMapGraphEdgeAggregator.getAggregateList;
import static jflowmap.data.FlowMapGraphEdgeAggregator.isAggregate;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapNodeTotals;
import jflowmap.data.Nodes;
import jflowmap.data.SeqStat;
import jflowmap.geo.MapProjections;
import jflowmap.geom.GeomUtils;
import jflowmap.models.map.GeoMap;
import jflowmap.models.map.MapArea;
import jflowmap.models.map.Polygon;
import jflowmap.util.CollectionUtils;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.map.PGeoMap;
import jflowmap.views.map.PGeoMapArea;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class MapLayer extends PLayer implements ViewLayer {

  private static final double CENTROID_DOT_SIZE = 2.0;

  private final PCamera geoLayerCamera;
  public final PGeoMap visualAreaMap;
  private final FlowstratesView flowstratesView;
  private final FlowEndpoint endpoint;
  private Map<String, Centroid> nodeIdsToCentroids;
  private List<String> selectedNodes;

  private final Lasso lasso;
  private PInputEventListener centroidMouseListener;
  private boolean centroidsOpaque = true;
  private Rectangle2D centroidsBounds;

  public MapLayer(FlowstratesView flowstratesView, GeoMap areaMap, FlowEndpoint s) {
    this.flowstratesView = flowstratesView;
    this.endpoint = s;

    geoLayerCamera = new PCamera();
    geoLayerCamera.addLayer(this);

    visualAreaMap = new PGeoMap(flowstratesView.getMapColorScheme(), areaMap,
        flowstratesView.getMapProjection());

    addChild(visualAreaMap);
    geoLayerCamera.setPaint(visualAreaMap.getPaint());

    createCentroids();
    addMouseOverListenersToMaps(visualAreaMap); // must be after createCentroids

    visualAreaMap.setBounds(visualAreaMap.getFullBoundsReference()); // enable mouse ev.

    lasso = createLasso(geoLayerCamera);
    geoLayerCamera.addInputEventListener(lasso);

  }

  public FlowEndpoint getEndpoint() {
    return endpoint;
  }

  public PCamera getCamera() {
    return geoLayerCamera;
  }

  public PGeoMap getVisualAreaMap() {
    return visualAreaMap;
  }

  public Map<String, Centroid> getNodeIdsToCentroids() {
    return nodeIdsToCentroids;
  }

  private FlowMapGraph getFlowMapGraph() {
    return flowstratesView.getFlowMapGraph();
  }

  public void setCentroidsOpaque(boolean value) {
    if (centroidsOpaque != value) {
      centroidsOpaque = value;
      updateCentroids();
    }
  }

  void updateCentroids() {
    RectSet occupied = new RectSet(nodeIdsToCentroids.size());

    Set<String> left;

    if (isNodeSelectionEmpty()) {
      left = nodeIdsToCentroids.keySet();
    } else {
      // give priority to the selected nodes
      left = Sets.newLinkedHashSet(nodeIdsToCentroids.keySet());
      for (String id : selectedNodes) {
        updateCentroid(nodeIdsToCentroids.get(id), occupied);
        left.remove(id);
      }
    }

    for (String id : left) {
      updateCentroid(nodeIdsToCentroids.get(id), occupied);
    }
  }

  private void updateCentroid(Centroid c, RectSet occupied) {
//    if (centroidsOpaque) {
      c.updateInCamera(geoLayerCamera);
      if (c.getVisible()) {
        c.getLabelNode().setVisible(occupied.addIfNotIntersects(c.getCollisionBounds()));
      }
      c.setOpaque(centroidsOpaque);
//      c.setOpaque(true);
//    } else {
      // boolean vis = false;
      // VisualArea va = getVisualAreaMap().getVisualAreaBy(c.getNodeId());
      // if (va != null) {
      // vis = va.getFullBoundsReference().contains(c.getLabelNode().getBoundsReference());
      // }
      // c.setVisible(vis);
//      c.setOpaque(false);
//    }
  }

  private void createCentroids() {
    nodeIdsToCentroids = Maps.newLinkedHashMap();

    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

    // sort centroids so that more important are shown first
    Iterable<Node> nodes = CollectionUtils.sort(
        fmg.nodesHavingEdges(endpoint.dir()),
        Collections.reverseOrder(FlowMapNodeTotals.createMaxNodeWeightTotalsComparator(fmg,
            endpoint.dir())));

    centroidMouseListener = createCentroidMouseListener();

    Iterable<Node> nodesWithCoords = Iterables.filter(nodes, fmg.haveCoordsPredicate());
    Iterable<Node> nodesWithoutCoords =
      fmg.sortByAttr(
          Iterables.filter(nodes, Predicates.not(fmg.haveCoordsPredicate())),
          fmg.getNodeLabelAttr());

    createCentroidsForNodesWithCoords(nodesWithCoords);
    createCentroidsAndAreasForNodesWithoutCoords(nodesWithoutCoords);
  }

  private void createCentroidsForNodesWithCoords(Iterable<Node> nodesWithCoords) {
    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();

    for (Node node : nodesWithCoords) {
      Point2D p = visualAreaMap.getMapProjection().project(node.getDouble(fmg.getNodeLonAttr()),
          node.getDouble(fmg.getNodeLatAttr()));

      createCentroid(node, p.getX(), p.getY());
    }
  }

  private void createCentroidsAndAreasForNodesWithoutCoords(Iterable<Node> nodesWithoutCoords) {

    FlowMapGraph fmg = flowstratesView.getFlowMapGraph();
    List<Rectangle2D> rects = visualAreaMap.createAreasForNodesWithoutCoords(nodesWithoutCoords);

    int cnt = 0;
    for (Node node : nodesWithoutCoords) {
      String nodeId = fmg.getNodeId(node);
      String label = fmg.getNodeLabel(node);

      Rectangle2D rect = rects.get(cnt);

      createCentroid(node, rect.getCenterX(), rect.getCenterY());

      Polygon polygon = new Polygon(new Point2D[] { new Point2D.Double(rect.getX(), rect.getY()),
          new Point2D.Double(rect.getMaxX(), rect.getY()),
          new Point2D.Double(rect.getMaxX(), rect.getMaxY()),
          new Point2D.Double(rect.getX(), rect.getMaxY()), new Point2D.Double(rect.getX(), rect.getY()) });

      visualAreaMap.addArea(new MapArea(nodeId, label, Arrays.asList(polygon)), MapProjections.NONE);
      cnt++;
    }
  }

  private void createCentroid(Node node, double x, double y) {
    FlowMapGraph flowMapGraph = flowstratesView.getFlowMapGraph();

    String nodeId = flowMapGraph.getNodeId(node);
    String nodeLabel = flowMapGraph.getNodeLabel(node);
    Centroid c = new Centroid(nodeId, nodeLabel, x, y, CENTROID_DOT_SIZE, flowstratesView.getStyle()
        .getMapAreaCentroidPaint(), flowstratesView);
    // c.setPickable(false);

    c.addInputEventListener(centroidMouseListener);
    geoLayerCamera.addChild(c);
    nodeIdsToCentroids.put(c.getNodeId(), c);
  }

  void setSelectedNodes(List<String> nodeIds) {
    if (selectedNodes != nodeIds) {
      flowstratesView.setCustomEdgeFilter(null);
      List<String> old = selectedNodes;
      selectedNodes = (nodeIds != null ? ImmutableList.copyOf(nodeIds) : null);
      flowstratesView.fireNodeSelectionChanged(old, nodeIds);
      flowstratesView.updateVisibleEdges();
      updateCentroidColors();
      flowstratesView.getTemporalLayer().fitInView(false, false);
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

  public boolean isPointVisible(Point2D p) {
    return getCamera().getViewBounds().contains(p);
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
        if (lasso.isSelecting())
          return;

        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), true);
        }
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        if (lasso.isSelecting())
          return;

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

  private void addMouseOverListenersToMaps(PGeoMap visualAreaMap) {
    PInputEventListener listener = new PTypedBasicInputEventHandler<PGeoMapArea>(PGeoMapArea.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        if (lasso.isSelecting())
          return;

        if (Lasso.isControlOrAppleCmdDown(event))
          return;

        PGeoMapArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          if (nodeIdsToCentroids.containsKey(areaId)) {
            setNodeHighlighted(areaId, true);
          }
        }
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        if (lasso.isSelecting())
          return;

        PGeoMapArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          if (nodeIdsToCentroids.containsKey(areaId)) {
            onCentroidOrAreaMouseClicked(event, areaId);
          }
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        PGeoMapArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          setNodeHighlighted(areaId, false);
        }
      }
    };
    for (PGeoMapArea va : PNodes.childrenOfType(visualAreaMap, PGeoMapArea.class)) {
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
        if (selectedNodes.size() == 1 && selectedNodes.get(0).equals(nodeId)) {
          // focusing on corresponding nodes in the opposite map when clicking on
          // the only selected node
          FlowEndpoint other = endpoint.opposite();
          MapLayer otherMap = flowstratesView.getMapLayer(other);
//          focusOnNode(nodeId);
          otherMap.focusOnNodesOfVisibleEdges();
        }
        newSelection = Arrays.asList(nodeId);
      }
    }
    setSelectedNodes(newSelection);
  }

  private void colorizeMapArea(String areaId, double value, boolean hover) {
    colorizeMapArea(areaId, value, hover, flowstratesView.getValueStat());
  }

  private void colorizeMapArea(String areaId, double value, boolean hover, SeqStat valueStat) {
    Centroid c = nodeIdsToCentroids.get(areaId);
    if (c != null) {
      PGeoMapArea area = visualAreaMap.getVisualAreaBy(areaId);
      if (area != null && !area.isEmpty()) {
        Color color;
        if (hover) {
          color = flowstratesView.getColorFor(value, valueStat);
        } else {
          color = flowstratesView.getMapColorScheme().getColor(ColorCodes.AREA_PAINT);
        }
        area.setPaint(color);
      } else {
        Color color;
        if (hover) {
          color = flowstratesView.getColorFor(value, valueStat);
        } else {
          color = flowstratesView.getStyle().getMapAreaCentroidLabelPaint();
        }
        c.getLabelNode().setPaint(color);
      }
    }
  }

  private void colorizeMapAreasWithNodeTotals(Iterable<Edge> edges, String weightAttr, boolean hover) {
    Map<String, Double> totals = calcNodeTotalsFor(edges, weightAttr);

    for (Node node : Nodes.distinctNodesOfEdges(edges, endpoint)) {
      String nodeId = getFlowMapGraph().getNodeId(node);
      colorizeMapArea(nodeId, totals.get(nodeId), hover);
    }
  }

  public Map<String, Double> calcNodeTotalsFor(Iterable<Edge> edges, String weightAttr) {
    return FlowMapNodeTotals.calcNodeTotalsFor(getFlowMapGraph(), edges, getColumnValueAttrName(weightAttr),
        endpoint);
  }

  void updateMapAreaColorsOnHeatmapCellHover(Edge edge, String weightAttr, boolean hover) {
    if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {

      List<Edge> edges = FlowMapGraphEdgeAggregator.getBaseAggregateList(edge);
      colorizeMapAreasWithNodeTotals(edges, weightAttr, hover);

    } else {
      double value = flowstratesView.getValue(edge, weightAttr);
      colorizeMapArea(flowstratesView.getAggLayers().getNodeId(edge, endpoint), value, hover);
    }
  }

  void updateOnHeatmapColumnHover(String columnAttr, boolean hover) {
    Iterable<Edge> edges;
    // if (flowstratesView.isFilterApplied()) {
    edges = flowstratesView.getVisibleEdges();
    // } else {
    // edges = getFlowMapGraph().edges();
    // }
    setCentroidsOpaque(!hover);
    colorizeMapAreasWithNodeTotals(edges, columnAttr, hover);
  }

  private String getColumnValueAttrName(String columnAttr) {
    ValueType vtype = flowstratesView.getValueType();

    return vtype.getColumnValueAttr(getFlowMapGraph().getAttrSpec(), columnAttr);
  }

  void setVisualAreaMapHighlighted(String nodeId, boolean highlighted) {
    PGeoMapArea va = visualAreaMap.getVisualAreaBy(nodeId);
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
    return centroidsBounds(nodeIdsToCentroids.values());
  }

  Rectangle2D centroidsBounds(Iterable<Centroid> centroids) {
    if (centroidsBounds == null) {
      Rectangle2D.Double b = new Rectangle2D.Double();
      boolean first = true;
      for (Centroid c : centroids) {

        if (first) {
          b.x = c.getOrigX();
          b.y = c.getOrigY();
          first = false;
        } else {
          b.add(c.getPoint());
        }

//        double x = c.getOrigX();
//        double y = c.getOrigY();
//        if (first) {
//          b.x = x;
//          b.y = y;
//          first = false;
//        } else {
//          if (x < b.x) {
//            b.x = x;
//          }
//          if (x > b.getMaxX()) {
//            b.width = x - b.x;
//          }
//          if (y < b.y) {
//            b.y = y;
//          }
//          if (y > b.getMaxY()) {
//            b.height = y - b.y;
//          }
//        }
      }
      // getVisualAreaMapCamera(s).localToView(b);
      centroidsBounds = b;
    }
    return new PBounds(centroidsBounds);
  }

  void updateOnHeatmapCellHover(Edge edge, String weightAttr, boolean hover) {
    updateMapAreaColorsOnHeatmapCellHover(edge, weightAttr, hover);
    setEdgeEndpointCentroidHighlighted(edge, hover);
    if (isAggregate(edge)  &&  getAggregateList(edge).size() > 1) {
      setCentroidsOpaque(!hover);
    }
  }

  void setEdgeEndpointCentroidHighlighted(Edge edge, boolean highlighted) {
    Node node = getFlowMapGraph().getNodeOf(edge, endpoint);
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

    FlowLinesLayerNode flowLinesLayer = flowstratesView.getFlowLinesLayerNode();

    flowLinesLayer.setFlowLinesColoringMode(FlowLinesColoringMode.of(endpoint));

    for (Edge e : endpoint.filterByNodePredicate(flowstratesView.getVisibleEdges(), acceptNodes)) {
      flowLinesLayer.setFlowLinesOfEdgeHighlighted(e, highlighted);
    }
    setVisualAreaMapHighlighted(nodeId, highlighted);
  }

  public boolean isNodeSelectionEmpty() {
    return selectedNodes == null || selectedNodes.isEmpty();
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

  public void focusOnNode(String nodeId) {
    focusOnNodes(Arrays.asList(nodeId));
  }

  public void focusOnNodes(Iterable<String> nodeIds) {
    Rectangle2D.Double rect = getBoundingBoxFor(nodeIds);

    if (rect != null) {
      Rectangle2D fbb = getVisualAreaMap().getBoundingBox();

      final double fw = fbb.getWidth(), fh = fbb.getHeight();
      double w = rect.getWidth(), h = rect.getHeight();

      // Show more context for smaller areas and less for larger ones
      final double minGrowth = 0.5;
      final double maxGrowth = 3.4;
      double alpha = Math.min((fw - Math.min(fw, w * 5)) / fw, (fh - Math.min(fh, h * 5)) / fh);
      double growBy = minGrowth + (maxGrowth - minGrowth) * Math.pow(alpha, 5);
      GeomUtils.growRectInPlaceByRelativeSize(rect, growBy, growBy, growBy, growBy);


//      if (rect.x < fbb.getX()) {
//        rect.x = fbb.getX();
//      }
//      if (rect.y < fbb.getY()) {
//        rect.y = fbb.getY();
//      }

      if (rect.width > fbb.getWidth()  /*||  rect.getMaxX() > fbb.getMaxX()*/) {
        rect.x = fbb.getX();
        rect.width = fbb.getWidth();
      }
      if (rect.height > fbb.getHeight()  /*||  rect.getMaxX() > fbb.getMaxY()*/) {
        rect.y = fbb.getY();
        rect.height = fbb.getHeight();
      }

      getCamera().animateViewToCenterBounds(rect, true, FlowstratesView.fitInViewDuration(true));
    }
  }

  public void focusOnNodesOfVisibleEdges() {
    focusOnNodes(Nodes.nodeIdsOf(
        Nodes.distinctNodesOfEdges(flowstratesView.getVisibleEdges(), endpoint)));
  }

  private Rectangle2D.Double getBoundingBoxFor(Iterable<String> nodeIds) {
    Rectangle2D.Double rect = null;

    for (String nodeId : nodeIds) {
      Centroid c = nodeIdsToCentroids.get(nodeId);
      if (c != null) {
        if (rect == null) {
          rect = new Rectangle2D.Double(c.getOrigX(), c.getOrigY(), 0, 0);
        } else {
          rect.add(c.getOrigX(), c.getOrigY());
        }
      }

      PGeoMapArea va = visualAreaMap.getVisualAreaBy(nodeId);
      if (va != null) {
        if (rect == null) {
          rect = new PBounds(va.getBoundingBox());
        } else {
          rect.add(va.getBoundingBox());
        }
      }
    }
    return rect;
  }

  public void fitInView(boolean animate, boolean whole) {
    if (whole  ||  isNodeSelectionEmpty()) {
      Rectangle2D bounds = centroidsBounds();
      //GeomUtils.growRectInPlaceByRelativeSize(bounds, .1,,.1,.1);
      fit(bounds, animate);
    } else {

      /*
      PBounds current = getCamera().getViewBounds();
      Rectangle2D full = centroidsBounds();
//      Rectangle2D sel = getBoundingBoxFor(selectedNodes);

      double diff2full = Math.abs(MathUtils.relativeDiff(current.width, full.getWidth()));
      if (diff2full > 0.5) {
        fit(full, animate);
      } else {
//        boundsToFit = getBoundingBoxFor(selectedNodes);
        focusOnNodes(selectedNodes);
      }
      */
      focusOnNodes(selectedNodes);

    }
  }

  private void fit(Rectangle2D boundsToFit, boolean animate) {
    GeomUtils.growRectInPlaceByRelativeSize(boundsToFit, 0, .1, 0, .1);
    getCamera().animateViewToCenterBounds(boundsToFit, true, FlowstratesView.fitInViewDuration(animate));
  }

}
