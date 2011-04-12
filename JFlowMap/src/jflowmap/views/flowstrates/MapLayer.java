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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jflowmap.FlowEndpoints;
import jflowmap.FlowMapGraph;
import jflowmap.data.FlowMapSummaries;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.Lasso;
import jflowmap.util.CollectionUtils;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.flowmap.VisualArea;
import jflowmap.views.flowmap.VisualAreaMap;
import prefuse.data.Node;

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
  private final FlowEndpoints nodeEdgePos;
  private final Map<String, Centroid> nodeIdsToCentroids;


  public MapLayer(FlowstratesView flowstratesView, AreaMap areaMap, FlowEndpoints s) {
    this.flowstratesView = flowstratesView;
    this.nodeEdgePos = s;

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
        flowMapGraph.nodesHavingEdges(nodeEdgePos.dir()),
        Collections.reverseOrder(
            FlowMapSummaries.createMaxNodeWeightSummariesComparator(flowMapGraph,
            nodeEdgePos.dir())));

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
    return new Lasso(targetCamera, flowstratesView.getStyle().getLassoStrokePaint(nodeEdgePos)) {
      @Override
      public void selectionMade(Shape shape) {
        flowstratesView.setCustomEdgeFilter(null);
        flowstratesView.setSelectedNodes(flowstratesView.applyLassoToNodeCentroids(shape, nodeEdgePos), nodeEdgePos);
        flowstratesView.updateVisibleEdges();
        updateCentroidColors();
      }
    };
  }

  void updateCentroidColors() {
    List<String> selNodes = flowstratesView.getSelectedNodes(nodeEdgePos);
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
          flowstratesView.setNodeHighlighted(c.getNodeId(), nodeEdgePos, true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        Centroid c = node(event);
        if (c != null) {
          flowstratesView.setNodeHighlighted(c.getNodeId(), nodeEdgePos, false);
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
          flowstratesView.setNodeHighlighted(areaId, nodeEdgePos, true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          flowstratesView.setNodeHighlighted(areaId, nodeEdgePos, false);
        }
      }
    };
    for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
      va.addInputEventListener(listener);
    }
  }

}
