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

package jflowmap.views.flowstrates;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import jflowmap.AbstractCanvasView;
import jflowmap.ColorSchemes;
import jflowmap.EdgeDirection;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.NodeEdgePos;
import jflowmap.data.EdgeListFlowMapStats;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.data.Nodes;
import jflowmap.geo.MapProjections;
import jflowmap.geom.GeomUtils;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.Lasso;
import jflowmap.util.CollectionUtils;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;
import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.ColorCodes;
import jflowmap.views.Legend;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowmap.AbstractLegendItemProducer;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.flowmap.FlowMapView;
import jflowmap.views.flowmap.VisualArea;
import jflowmap.views.flowmap.VisualAreaMap;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.util.ColorLib;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesView extends AbstractCanvasView {

  private static final String CAPTION_NODE_ATTR = "captionNode";
  private static final int CAPTION_HEADER_HEIGHT = 50;
  private static final double CENTROID_DOT_SIZE = 2.0;

  enum Properties {
    CUSTOM_EDGE_FILTER, NODE_SELECTION
  }

  enum GroupBy {
    SOURCE
  }

  private static final Font HEATMAP_ROW_LABELS_FONT = new Font("Arial", Font.PLAIN, 22 /*18*/);
  private static final Font HEATMAP_COLUMN_LABELS_FONT = new Font("Arial", Font.PLAIN, 25 /*19*/);
  private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 23);

  public static Logger logger = Logger.getLogger(FlowstratesView.class);

  private static final double cellWidth = 40;
  private static final double cellHeight = 40;
  private boolean interpolateColors = true;

  private final FlowstratesStyle style = new DefaultFlowstratesStyle();
  private final FlowMapGraph flowMapGraph;
  // private final PScrollPane scrollPane;

  private final JPanel controlPanel;
  private ValueType valueType = ValueType.VALUE;
  private FlowtiLinesColoringMode flowtiLinesColoringMode = FlowtiLinesColoringMode.SOURCE;
  private int maxVisibleTuples;

  private ColorSchemes sequentialColorScheme = ColorSchemes.OrRd;
  private ColorSchemes divergingColorScheme = ColorSchemes.RdBu5;

  private final PInputEventListener heatmapCellTooltipListener = createTooltipListener(HeatMapCell.class);
  private final PInputEventListener heatmapCellHoverListener = createHeatMapCellHoverListener();
  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

  private final PNode heatmapNode;
  private final PNode mapToMatrixLinesLayer;

  private Map<String, Centroid> srcNodeIdsToCentroids;
  private Map<String, Centroid> targetNodeIdsToCentroids;
  private Map<Edge, Pair<FlowtiLine, FlowtiLine>> edgesToLines;
  private Map<Edge, Pair<PText, PText>> edgesToLabels;

  private VisualAreaMap sourceVisualAreaMap;
  private VisualAreaMap targetVisualAreaMap;

  private List<Edge> visibleEdges;
  private Predicate<Edge> customEdgeFilter;

  private final PCamera sourcesCamera = new PCamera();
  private final PCamera heatmapCamera = new PCamera();
  private final PCamera targetsCamera = new PCamera();

  private final PLayer sourcesLayer = new PLayer();
  private final PLayer heatmapLayer = new PLayer();
  private final PLayer targetsLayer = new PLayer();
  private Map<String, Color> flowLinesPalette;

  private boolean showFlowtiLinesForHighligtedNodesOnly = false;
  private boolean focusOnVisibleRows = false;

  private List<String> selSrcNodes, selTargetNodes;

  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.get(code);
    }
  };
  private final FlowMapGraphAggLayers layers;
  private Legend legend;

  public FlowstratesView(FlowMapGraph flowMapGraph, AreaMap areaMap) {
    this(flowMapGraph, areaMap, -1);
  }

  public FlowstratesView(FlowMapGraph flowMapGraph, AreaMap areaMap, int maxVisibleTuples) {

    this.layers = RefugeeAggLayers.createAggLayers(flowMapGraph);


    this.flowMapGraph = flowMapGraph;
    this.maxVisibleTuples = maxVisibleTuples;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setAutoFitOnBoundsChange(false);
    canvas.setBackground(style.getBackgroundColor());
    PPanEventHandler panHandler = new PPanEventHandler();
    panHandler.setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !event.isControlDown() && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });
    canvas.setPanEventHandler(panHandler);
    canvas.getZoomHandler().setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !event.isControlDown() && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });
    // canvas.setMinZoomScale(1e-1);
    // canvas.setMaxZoomScale(1e+2);
    canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

    // getCamera().addLayer(sourcesLayer);
    sourcesCamera.addLayer(sourcesLayer);
    heatmapCamera.addLayer(heatmapLayer);
    targetsCamera.addLayer(targetsLayer);

    addCaption(sourcesCamera, "Origins");
    addCaption(heatmapCamera, "Time");
    addCaption(targetsCamera, "Destinations");

    PLayer canvasLayer = canvas.getLayer();
    canvasLayer.addChild(sourcesCamera);
    canvasLayer.addChild(heatmapCamera);
    canvasLayer.addChild(targetsCamera);

    controlPanel = new FlowstratesControlPanel(this);

    // scrollPane = new PScrollPane(canvas);
    // scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // scrollPane.setVerticalScrollBarPolicy(PScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph);
    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph,
        flowMapGraph.getEdgeWeightDiffAttr());
    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph,
        flowMapGraph.getEdgeWeightRelativeDiffAttrNames());

    createAreaMaps(areaMap);
    heatmapNode = new PNode();
    heatmapLayer.addChild(heatmapNode);

    mapToMatrixLinesLayer = new PNode();
    getCamera().addChild(mapToMatrixLinesLayer);

    createLegend();
    renewHeatmap();

    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          fitInView();
        }
      }
    });

    PropertyChangeListener linesUpdater = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_VIEW_TRANSFORM) {
          hideTooltip();
          updateCentroids();
          updateFlowLinePositions();
          // getVisualCanvas().setViewZoomPoint(getCamera().getViewBounds().getCenter2D());
        }
      }
    };
    sourcesCamera.addPropertyChangeListener(linesUpdater);
    targetsCamera.addPropertyChangeListener(linesUpdater);
    heatmapCamera.addPropertyChangeListener(linesUpdater);

  }

  private void addCaption(PCamera camera, String caption) {
    PText textNode = new PText(caption);
    textNode.setFont(CAPTION_FONT);
    camera.addAttribute(CAPTION_NODE_ATTR, textNode);
    camera.addChild(textNode);
  }

  private void createLegend() {
    legend = new Legend(new Color(220,220,220,200), new Color(60,60,60,200),
      new AbstractLegendItemProducer(4) {

        @Override
        public PNode createItem(double value) {
          PPath item = new PPath(new Rectangle2D.Double(0, 0, 10, 10));
          item.setPaint(getColorFor(value));
          item.setName(FlowMapView.NUMBER_FORMAT.format(value));
          item.setStroke(null);
          return item;
        }

        @Override
        public PNode createHeader() {
//          return new PText(getFlowMapGraph().getId() + "\n" + getValueType());
          return null;
        }

        @Override
        public MinMax getMinMax() {
          return getValueType().getMinMax(getStats());
        }

      });
    legend.setOffset(5, 12);
    legend.setScale(1.4);
    heatmapCamera.addChild(legend);
//    PBounds b = heatmapCamera.getViewBounds();
//    legend.offset(b.x, b.y);
  }

  public void addPropertyChangeListener(Properties prop, PropertyChangeListener listener) {
    changes.addPropertyChangeListener(prop.name(), listener);
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  public Iterable<String> getAggLayerNames() {
    return layers.getLayerNames();
  }

  public void setActiveAggLayer(String layerName) {
    layers.setActiveLayer(layerName);
    this.visibleEdges = null;
    renewHeatmap();
    fitHeatMap();
  }

  // public void addPropertyChangeListener(Properties prop, PropertyChangeListener listener) {
  // changes.addPropertyChangeListener(prop.name(), listener);
  // }

  public void setMaxVisibleTuples(int maxVisibleTuples) {
    if (this.maxVisibleTuples != maxVisibleTuples) {
      this.maxVisibleTuples = maxVisibleTuples;
      this.visibleEdges = null;
      renewHeatmap();
    }
  }

  public int getMaxVisibleTuples() {
    return maxVisibleTuples;
  }

  public boolean getFocusOnVisibleRows() {
    return focusOnVisibleRows;
  }

  public void setFocusOnVisibleRows(boolean focusOnVisibleRows) {
    if (this.focusOnVisibleRows != focusOnVisibleRows) {
      this.focusOnVisibleRows = focusOnVisibleRows;
      updateHeatmapColors();
    }
  }

  public void setCustomEdgeFilter(Predicate<Edge> edgeFilter) {
    if (customEdgeFilter != edgeFilter) {
      Predicate<Edge> oldValue = customEdgeFilter;
      this.customEdgeFilter = edgeFilter;
      if (!clearNodeSelection()) {
        updateVisibleEdges();
      }
      changes.firePropertyChange(Properties.CUSTOM_EDGE_FILTER.name(), oldValue, edgeFilter);
    }
  }

  private void updateVisibleEdges() {
    visibleEdges = null;
    renewHeatmap();
  }

  private Predicate<Edge> getEdgePredicate() {
    return new Predicate<Edge>() {
      @Override
      public boolean apply(Edge edge) {
        return (customEdgeFilter == null || customEdgeFilter.apply(edge))
            && (selSrcNodes == null || selSrcNodes.contains(flowMapGraph.getNodeId(edge.getSourceNode())))
            && (selTargetNodes == null || selTargetNodes
                .contains(flowMapGraph.getNodeId(edge.getTargetNode())));
      }
    };
  }

  private void updateFlowtiLinesPalette() {
    if (flowtiLinesColoringMode == FlowtiLinesColoringMode.SAME_COLOR) {
      if (flowLinesPalette != null)
        flowLinesPalette.clear();
      return;
    }
    Set<String> ids = Sets.newHashSet();
    for (Edge e : visibleEdges) {
      switch (flowtiLinesColoringMode) {
      case SOURCE:
        ids.add(flowMapGraph.getSourceNodeId(e));
        break;
      case TARGET:
        ids.add(flowMapGraph.getTargetNodeId(e));
        break;
      }
    }
    flowLinesPalette = new HashMap<String, Color>(ids.size());
    Color[] palette = ColorUtils.createCategoryColors(ids.size(), .1f);
    int i = 0;
    for (String src : ids) {
      flowLinesPalette.put(src, palette[i++]);
    }
  }

  private List<Edge> getTopEdges(Iterable<Edge> edges) {
    List<Edge> list = Lists.newArrayList(edges);

    // Sort by magnitude
    Collections.sort(list, RowOrderings.MAX_MAGNITUDE_IN_ROW.getComparator(flowMapGraph));

    // Take first maxVisibleTuples
    if (maxVisibleTuples >= 0) {
      if (list.size() > maxVisibleTuples) {
        list = list.subList(0, maxVisibleTuples);
      }
    }
    return list;
  }

  private Iterable<Edge> removeEdgesWithNaNs(Iterable<Edge> edges) {
    return // Remove rows with no weights
    Iterables.filter(edges, new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
        return flowMapGraph.hasNonZeroWeight(e);
      }
    });
  }

  private List<Edge> getVisibleEdges() {
    if (visibleEdges == null) {
      List<Edge> edges = Lists.newArrayList(getTopEdges(Iterables.filter(removeEdgesWithNaNs(
      // flowMapGraph.edges()
          layers.getVisibleEdges()), getEdgePredicate())));

      Collections.sort(edges, rowOrdering.getComparator(flowMapGraph));

      visibleEdges = edges;
      visibleEdgesStats = null;

      updateFlowtiLinesPalette();
    }

    return visibleEdges;
  }

  public void setHeatMapCellValueType(ValueType valueType) {
    if (this.valueType != valueType) {
      this.valueType = valueType;
      updateHeatmapColors();
    }
  }

  public ValueType getValueType() {
    return valueType;
  }

  public void setDivergingColorScheme(ColorSchemes divergingColorScheme) {
    if (this.divergingColorScheme != divergingColorScheme) {
      this.divergingColorScheme = divergingColorScheme;
      updateHeatmapColors();
    }
  }

  public ColorSchemes getDivergingColorScheme() {
    return divergingColorScheme;
  }

  public void setSequentialColorScheme(ColorSchemes sequentialColorScheme) {
    if (this.sequentialColorScheme != sequentialColorScheme) {
      this.sequentialColorScheme = sequentialColorScheme;
      updateHeatmapColors();
    }
  }

  public ColorSchemes getSequentialColorScheme() {
    return sequentialColorScheme;
  }

  public void setInterpolateColors(boolean interpolateColors) {
    if (this.interpolateColors != interpolateColors) {
      this.interpolateColors = interpolateColors;
      updateHeatmapColors();
    }
  }

  public boolean getInterpolateColors() {
    return interpolateColors;
  }

  public FlowMapGraph getFlowMapGraph() {
    return flowMapGraph;
  }

  private PCamera getCamera() {
    return getVisualCanvas().getCamera();
  }

  private void createCentroids() {
    srcNodeIdsToCentroids = createCentroids(NodeEdgePos.SOURCE);
    targetNodeIdsToCentroids = createCentroids(NodeEdgePos.TARGET);
  }

  private Map<String, Centroid> createCentroids(final NodeEdgePos s) {
    Map<String, Centroid> map = Maps.newLinkedHashMap();

    // sort centroids so that more important are shown first
    Iterable<Node> nodes = CollectionUtils.sort(
        flowMapGraph.nodesHavingEdges(s.dir()),
        Collections.reverseOrder(FlowMapSummaries.createMaxNodeWeightSummariesComparator(flowMapGraph,
            s.dir())));

    PInputEventListener listener = createCentroidHoverListener(s);

    for (Node node : nodes) {
      double lon = node.getDouble(flowMapGraph.getXNodeAttr());
      double lat = node.getDouble(flowMapGraph.getYNodeAttr());

      Point2D p = getVisualAreaMap(s).getMapProjection().project(lon, lat);

      Centroid centroid = createCentroid(p.getX(), p.getY(), node);
      centroid.addInputEventListener(listener);

      getVisualAreaMapCamera(s).addChild(centroid);
      // VisualArea va = getVisualAreaMap(s).getVisualAreaBy(flowMapGraph.getNodeId(node));

      map.put(flowMapGraph.getNodeId(node), centroid);
    }
    return map;
  }

  private PInputEventListener createCentroidHoverListener(final NodeEdgePos s) {
    return new PTypedBasicInputEventHandler<Centroid>(Centroid.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), s, true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        Centroid c = node(event);
        if (c != null) {
          setNodeHighlighted(c.getNodeId(), s, false);
        }
      }
    };
  }

  private PCamera getVisualAreaMapCamera(NodeEdgePos s) {
    switch (s) {
    case SOURCE:
      return sourcesCamera;
    case TARGET:
      return targetsCamera;
    }
    throw new AssertionError();
  }

  private VisualAreaMap getVisualAreaMap(NodeEdgePos s) {
    switch (s) {
    case SOURCE:
      return sourceVisualAreaMap;
    case TARGET:
      return targetVisualAreaMap;
    }
    throw new AssertionError();
  }

  private List<String> getSelectedNodes(NodeEdgePos s) {
    switch (s) {
    case SOURCE:
      return selSrcNodes;
    case TARGET:
      return selTargetNodes;
    }
    throw new AssertionError();
  }

  private Map<String, Centroid> getNodeIdsToCentroids(NodeEdgePos s) {
    switch (s) {
    case SOURCE:
      return srcNodeIdsToCentroids;
    case TARGET:
      return targetNodeIdsToCentroids;
    }
    throw new AssertionError();
  }

  private Centroid createCentroid(double x, double y, Node node) {
    String nodeId = flowMapGraph.getNodeId(node);
    String nodeLabel = flowMapGraph.getNodeLabel(node);
    Centroid c = new Centroid(nodeId, nodeLabel, x, y, CENTROID_DOT_SIZE, style.getMapAreaCentroidPaint(),
        this);
    // c.setPickable(false);
    return c;
  }

  public void setShowLinesForHighligtedOnly(boolean showLinesForHighligtedOnly) {
    this.showFlowtiLinesForHighligtedNodesOnly = showLinesForHighligtedOnly;
    renewFlowtiLines();
  }

  private void renewFlowtiLines() {
    mapToMatrixLinesLayer.removeAllChildren();

    edgesToLines = Maps.newHashMap();

    // if (!showFlowtiLinesForHighligtedNodesOnly)
    {
      for (Edge edge : getVisibleEdges()) {
        FlowtiLine src = createFlowtiLine(edge);
        FlowtiLine target = createFlowtiLine(edge);
        // src.setVisible(!showFlowtiLinesForHighligtedNodesOnly || src.isHighlighted());
        // target.setVisible(!showFlowtiLinesForHighligtedNodesOnly || target.isHighlighted());
        edgesToLines.put(edge, Pair.of(src, target));
      }
    }

    updateFlowtiLineColors();
    updateFlowLinePositions();
  }

  public FlowtiLinesColoringMode getFlowtiLinesColoringMode() {
    return flowtiLinesColoringMode;
  }

  public void setFlowtiLinesColoringMode(FlowtiLinesColoringMode flowLinesColoringMode) {
    this.flowtiLinesColoringMode = flowLinesColoringMode;
    updateFlowtiLinesPalette();
    updateFlowtiLineColors();
  }

  private FlowtiLine createFlowtiLine(Edge edge) {
    FlowtiLine line = new FlowtiLine();
    mapToMatrixLinesLayer.addChild(line);
    return line;
  }

  private void updateFlowtiLineColors() {
    // if (showFlowtiLinesForHighligtedNodesOnly) {
    // return;
    // }

    for (Map.Entry<Edge, Pair<FlowtiLine, FlowtiLine>> e : edgesToLines.entrySet()) {
      Pair<FlowtiLine, FlowtiLine> p = e.getValue();
      Pair<Color, Color> colors = getFlowLineColors(e.getKey());

      FlowtiLine srcLine = p.first();
      srcLine.setColor(colors.first());
      srcLine.setHighlightedColor(colors.second());

      FlowtiLine targetLine = p.second();
      targetLine.setColor(colors.first());
      targetLine.setHighlightedColor(colors.second());
    }
  }

  private Pair<Color, Color> getFlowLineColors(Edge edge) {
    Color c;
    switch (flowtiLinesColoringMode) {
    case SAME_COLOR:
      return Pair.of(style.getFlowLineColor(), style.getFlowLineHighlightedColor());

    case SOURCE:
      c = flowLinesPalette.get(flowMapGraph.getSourceNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    case TARGET:
      c = flowLinesPalette.get(flowMapGraph.getTargetNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    }
    throw new AssertionError();
  }

  private void updateCentroids() {
    updateCentroids(NodeEdgePos.SOURCE);
    updateCentroids(NodeEdgePos.TARGET);
  }

  private void updateCentroids(NodeEdgePos s) {
    Map<String, Centroid> centroidsMap = getNodeIdsToCentroids(s);
    RectSet occupied = new RectSet(centroidsMap.size());
    for (Centroid c : centroidsMap.values()) {
      c.updateInCamera(getVisualAreaMapCamera(s));
      if (c.getVisible()) {
        c.getLabelNode().setVisible(occupied.addIfNotIntersects(c.getLabelNode().getBounds()));
      }
    }
  }

  // private boolean addIfNotIntersects(Area occupied, Centroid c) {
  // PBounds clb = c.getLabelNode().getBounds();
  // if (!occupied.intersects(clb)) {
  // occupied.add(new Area(clb));
  // return true;
  // } else {
  // return false;
  // }
  // }

  private Point2D getCentroidPoint(Edge edge, NodeEdgePos s) {
    Centroid centroid = getNodeIdsToCentroids(s).get(flowMapGraph.getNodeId(s.nodeOf(edge)));
    Point2D point;
    if (centroid == null) {
      point = null;
    } else {
      point = centroid.getPoint();
    }
    return point;
  }

  private void updateFlowLinePositions() {
    // if (showFlowtiLinesForHighligtedNodesOnly) {
    // return;
    // }
    PBounds heatMapViewBounds = heatmapCamera.getViewBounds();
    int row = 0;
    for (Edge edge : getVisibleEdges()) {
      Pair<FlowtiLine, FlowtiLine> lines = edgesToLines.get(edge);
      Pair<PText, PText> labels = edgesToLabels.get(edge);

      Point2D srcCentroidPoint = getCentroidPoint(edge, NodeEdgePos.SOURCE);
      boolean inVis = srcCentroidPoint != null && sourcesCamera.getViewBounds().contains(srcCentroidPoint);
      FlowtiLine lineIn = lines.first();
      if (inVis) {
        Point2D.Double matrixIn = getMatrixInPoint(row);
        inVis = inVis && heatMapViewBounds.contains(matrixIn);
        if (inVis) {
          sourcesCamera.viewToLocal(srcCentroidPoint);
          heatmapCamera.viewToLocal(matrixIn);
          lineIn.setPoint(0, srcCentroidPoint.getX(), srcCentroidPoint.getY());
          Rectangle2D fromLabelBounds = heatmapCamera.viewToLocal(labels.first().getBounds());
          lineIn.setPoint(1, matrixIn.x - fromLabelBounds.getWidth(),
              matrixIn.y + fromLabelBounds.getHeight() / 2);
          lineIn.setPoint(2, matrixIn.x, matrixIn.y + fromLabelBounds.getHeight() / 2);
        }
      }
      lineIn.setVisible(inVis);
      lineIn.setPickable(false);

      Point2D targetCentroidPoint = getCentroidPoint(edge, NodeEdgePos.TARGET);
      boolean outVis = targetCentroidPoint != null
          && targetsCamera.getViewBounds().contains(targetCentroidPoint);
      FlowtiLine lineOut = lines.second();
      if (outVis) {
        Point2D.Double matrixOut = getMatrixOutPoint(row);
        outVis = outVis && heatMapViewBounds.contains(matrixOut);
        if (outVis) {
          targetsCamera.viewToLocal(targetCentroidPoint);
          heatmapCamera.viewToLocal(matrixOut);
          lineOut.setPoint(0, targetCentroidPoint.getX(), targetCentroidPoint.getY());
          Rectangle2D toLabelBounds = heatmapCamera.viewToLocal(labels.second().getBounds());
          lineOut.setPoint(1, matrixOut.x + toLabelBounds.getWidth(), matrixOut.y + toLabelBounds.getHeight()
              / 2);
          lineOut.setPoint(2, matrixOut.x, matrixOut.y + toLabelBounds.getHeight() / 2);
        }
      }
      lineOut.setVisible(outVis);
      lineOut.setPickable(false);

      row++;
    }

    mapToMatrixLinesLayer.repaint();
  }

  private Point2D.Double getMatrixInPoint(int row) {
    return new Point2D.Double(-10, getTupleY(row) + cellHeight / 2);
  }

  private Point2D.Double getMatrixOutPoint(int row) {
    return new Point2D.Double(10 + cellWidth * flowMapGraph.getEdgeWeightAttrsCount(), getTupleY(row)
        + cellHeight / 2);
  }

  /**
   * @returns List of ids of selected nodes, or null if no nodes were selected
   */
  private List<String> applyLassoToNodeCentroids(Shape shape, NodeEdgePos s) {
    List<String> nodeIds = null;
    for (Map.Entry<String, Centroid> e : getNodeIdsToCentroids(s).entrySet()) {
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

  private void updateCentroidColors() {
    updateCentroidColors(NodeEdgePos.SOURCE);
    updateCentroidColors(NodeEdgePos.TARGET);
  }

  private void updateCentroidColors(NodeEdgePos s) {
    List<String> selNodes = getSelectedNodes(s);
    for (Map.Entry<String, Centroid> e : getNodeIdsToCentroids(s).entrySet()) {
      String nodeId = e.getKey();
      Centroid centroid = e.getValue();
      centroid.setSelected(selNodes != null && selNodes.contains(nodeId));
    }

  }

  // private void setSelectedNodes(List<String> nodes, EdgeDirection dir) {
  // switch (dir) {
  // case INCOMING:
  // selectedSrcNodes = nodes;
  // selectedTargetNodes = Collections.emptyList();
  // break;
  // case OUTGOING:
  // selectedSrcNodes = Collections.emptyList();
  // selectedTargetNodes = nodes;
  // break;
  // }
  // createAreaCentroids()
  // }

  private void createAreaMaps(AreaMap areaMap) {
    sourceVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap, MapProjections.MERCATOR);
    targetVisualAreaMap = new VisualAreaMap(mapColorScheme, areaMap, MapProjections.MERCATOR);

    sourcesLayer.addChild(sourceVisualAreaMap);
    targetsLayer.addChild(targetVisualAreaMap);
    sourcesCamera.setPaint(sourceVisualAreaMap.getPaint());
    targetsCamera.setPaint(targetVisualAreaMap.getPaint());

    // sourcesCamera.setViewBounds(sourcesLayer.getFullBounds());
    // targetsCamera.setViewBounds(targetsLayer.getFullBounds());

    addMouseOverListenersToMaps(sourceVisualAreaMap, NodeEdgePos.SOURCE);
    addMouseOverListenersToMaps(targetVisualAreaMap, NodeEdgePos.TARGET);

    // updateMapColors(null);

    createCentroids();

    sourceVisualAreaMap.setBounds(sourceVisualAreaMap.getFullBoundsReference()); // enable mouse ev.
    targetVisualAreaMap.setBounds(targetVisualAreaMap.getFullBoundsReference()); // enable mouse ev.

    sourcesCamera.addInputEventListener(createLasso(sourcesCamera, NodeEdgePos.SOURCE));
    targetsCamera.addInputEventListener(createLasso(targetsCamera, NodeEdgePos.TARGET));
  }

  /**
   * @return True if the selection was not empty
   */
  private boolean clearNodeSelection() {
    if (selSrcNodes != null || selTargetNodes != null) {
      selSrcNodes = selTargetNodes = null;
      updateCentroidColors();
      updateVisibleEdges();
      return true;
    } else {
      return false;
    }
  }

  public void clearFilters() {
    clearNodeSelection();
    setCustomEdgeFilter(null);
  }

  public boolean isFilterApplied() {
    return
      (selSrcNodes != null  &&  selSrcNodes.size() > 0) ||
      (selTargetNodes != null  &&  selTargetNodes.size() > 0) ||
      (customEdgeFilter != null);
  }

  private Lasso createLasso(PCamera targetCamera, final NodeEdgePos s) {
    return new Lasso(targetCamera, style.getLassoStrokePaint(s)) {
      @Override
      public void selectionMade(Shape shape) {
        setCustomEdgeFilter(null);
        setSelectedNodes(applyLassoToNodeCentroids(shape, s), s);
        updateVisibleEdges();
        updateCentroidColors();
      }
    };
  }

  private void setSelectedNodes(List<String> nodeIds, NodeEdgePos s) {
    switch (s) {
    case SOURCE:
      if (selSrcNodes != nodeIds) {
        List<String> old = selSrcNodes;
        selSrcNodes = nodeIds;
        changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
      }
      break;
    case TARGET:
      if (selTargetNodes != nodeIds) {
        List<String> old = selTargetNodes;
        selTargetNodes = nodeIds;
        changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
      }
      break;
    default:
      throw new AssertionError();
    }
  }

  //
  // private void anchorRightVisualAreaMap() {
  // PNodes.moveTo(sourcesVisualAreaMap, getCamera().getViewBounds().getMaxX() -
  // sourcesVisualAreaMap.getFullBoundsReference().getWidth(), 0);
  // }

  // private void updateMapColors(FlowMapGraph fmg) {
  // colorizeMap(sourceVisualAreaMap, fmg, EdgeDirection.INCOMING);
  // colorizeMap(targetVisualAreaMap, fmg, EdgeDirection.OUTGOING);
  // }

  // private void schedule(PActivity activity) {
  // getCamera().getRoot().getActivityScheduler().addActivity(activity);
  // }

  private Iterable<Edge> findVisibleEdges(final List<String> nodeIds, final EdgeDirection dir) {
    return Iterables.filter(visibleEdges, new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
        String nodeId;
        switch (dir) {
        case INCOMING:
          nodeId = flowMapGraph.getNodeId(e.getTargetNode());
          break;
        case OUTGOING:
          nodeId = flowMapGraph.getNodeId(e.getSourceNode());
          break;
        default:
          throw new AssertionError();
        }
        return (nodeIds.contains(nodeId));
      }
    });
  }


  private void setNodeHighlighted(String nodeId, NodeEdgePos s, boolean highlighted) {
    Centroid c = getNodeIdsToCentroids(s).get(nodeId);
    if (c != null) {
      c.setHighlighted(highlighted);
      c.setTimeSliderVisible(highlighted);
    }
    for (Edge e : findVisibleEdges(Arrays.asList(nodeId), s.dir())) {
      Pair<FlowtiLine, FlowtiLine> pair = edgesToLines.get(e);
      if (pair != null) {
        pair.first().setHighlighted(highlighted);
        pair.second().setHighlighted(highlighted);
        // if (showFlowtiLinesForHighligtedNodesOnly) {
        // pair.first().setVisible(highlighted);
        // pair.second().setVisible(highlighted);
        // }
      }
    }

    VisualArea va = getVisualAreaMap(s).getVisualAreaBy(nodeId);
    if (va != null) {
      va.moveToFront();
      if (highlighted) {
        va.setStroke(style.getMapAreaHighlightedStroke());
        va.setStrokePaint(style.getMapAreaHighlightedStrokePaint());
        va.setPaint(style.getMapAreaHighlightedPaint());
      } else {
        va.setStroke(style.getMapAreaStroke());
        va.setStrokePaint(mapColorScheme.getColor(ColorCodes.AREA_STROKE));
        va.setPaint(mapColorScheme.getColor(ColorCodes.AREA_PAINT));
      }
      va.repaint();
    }

  }

  private void addMouseOverListenersToMaps(VisualAreaMap visualAreaMap, final NodeEdgePos s) {
    PInputEventListener listener = new PTypedBasicInputEventHandler<VisualArea>(VisualArea.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        if (event.isControlDown())
          return;
        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          setNodeHighlighted(areaId, s, true);
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        VisualArea va = node(event);
        if (va != null) {
          String areaId = va.getArea().getId();
          setNodeHighlighted(areaId, s, false);
        }
      }
    };
    for (VisualArea va : PNodes.childrenOfType(visualAreaMap, VisualArea.class)) {
      va.addInputEventListener(listener);
    }
  }

  @Override
  public JComponent getViewComponent() {
    // return scrollPane;
    return getVisualCanvas();
  }

  @Override
  public JComponent getControls() {
    return controlPanel;
  }

  private void renewHeatmap() {
    heatmapNode.removeAllChildren();

    int row = 0, maxCol = 0;

    edgesToLabels = Maps.newHashMap();

    for (Edge edge : getVisibleEdges()) {
      int col = 0;

      double y = getTupleY(row);

      // "from" label
      PText srcLabel = new PText(flowMapGraph.getNodeLabel(edge.getSourceNode()));
      srcLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 6);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(srcLabel);

      // "value" box node
      for (String weightAttr : flowMapGraph.getEdgeWeightAttrs()) {
        double x = col * cellWidth;

        HeatMapCell cell = new HeatMapCell(this, x, y, cellWidth, cellHeight, weightAttr,
            layers.getFlowMapGraphOf(edge), edge);

        cell.addInputEventListener(heatmapCellHoverListener);
        // if (!Double.isNaN(cell.getWeight())) {
        cell.addInputEventListener(heatmapCellTooltipListener);
        // }
        heatmapNode.addChild(cell);

        col++;
        if (col > maxCol)
          maxCol = col;
      }

      // "to" label
      PText targetLabel = new PText(flowMapGraph.getNodeLabel(edge.getTargetNode()));
      targetLabel.setFont(HEATMAP_ROW_LABELS_FONT);
      targetLabel.setX(cellWidth * maxCol + 6);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight()) / 2);
      heatmapNode.addChild(targetLabel);

      edgesToLabels.put(edge, Pair.of(srcLabel, targetLabel));

      row++;
    }

    // heatmapCamera.setViewBounds(heatmapNode.getFullBounds());

    createColumnLabels();
    renewFlowtiLines();
    legend.update();

    heatmapLayer.repaint();
    // layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight *
    // row)));
  }

  private void createColumnLabels() {
    List<String> attrNames = flowMapGraph.getEdgeWeightAttrs();
    // String cp = StringUtils.getCommonPrefix(attrNames.toArray(new String[attrNames.size()]));
    int col = 0;
    for (String attr : attrNames) {
      // attr = attr.substring(cp.length());
      PLabel label = new PLabel(attr);
      label.setName(attr);
      label.setFont(HEATMAP_COLUMN_LABELS_FONT);
      PBounds b = label.getFullBoundsReference();
      double x = col * cellWidth; // + (cellWidth - b.getWidth()) / 2;
      double y = -b.getHeight() / 1.5;
      label.setPaint(Color.white);
      label.rotateAboutPoint(-Math.PI * .65 / 2, x, y);
      label.setX(x);
      label.setY(y);
//      label.setX(5 + col * 6.3);
//      label.setY(col * cellWidth + (cellWidth - b.getWidth()) / 2);
//      label.translate(5, col * cellWidth + (cellWidth - b.getWidth()) / 2);
      heatmapNode.addChild(label);

      label.addInputEventListener(new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          label.moveToFront();
          updateMapAreaColorsOnHeatMapColumnLabelHover(label.getName(), true);
        }

        @Override
        public void mouseExited(PInputEvent event) {
          PLabel label = PNodes.getAncestorOfType(event.getPickedNode(), PLabel.class);
          updateMapAreaColorsOnHeatMapColumnLabelHover(label.getName(), false);
        }
      });
      col++;
    }
  }

  private void updateHeatmapColors() {
    for (HeatMapCell cell : PNodes.childrenOfType(heatmapNode, HeatMapCell.class)) {
      cell.updateColor();
    }
    legend.update();
    getVisualCanvas().repaint();
  }

  private void setEdgeCentroidsHighlighted(HeatMapCell hmcell, NodeEdgePos npos, boolean highlighted) {
    Node node = flowMapGraph.getNodeOf(hmcell.getEdge(), npos);
    Centroid c = getNodeIdsToCentroids(npos).get(flowMapGraph.getNodeId(node));
    if (c != null) {
      c.setHighlighted(highlighted);
    }
  }

  private PTypedBasicInputEventHandler<HeatMapCell> createHeatMapCellHoverListener() {
    return new PTypedBasicInputEventHandler<HeatMapCell>(HeatMapCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        HeatMapCell cell = node(event);
        // updateMapColors(node.getFlowMapGraph());

        // highlight cell
        cell.moveToFront();
        cell.setStroke(style.getSelectedTimelineCellStroke());
        cell.setStrokePaint(style.getSelectedTimelineCellStrokeColor());

        // highlight flow lines
        Pair<FlowtiLine, FlowtiLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(true);
          lines.second().setHighlighted(true);
        }
        setEdgeCentroidsHighlighted(cell, NodeEdgePos.SOURCE, true);
        setEdgeCentroidsHighlighted(cell, NodeEdgePos.TARGET, true);

        updateMapAreaColorsOnHeatMapCellHover(cell, true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        // updateMapColors(null);
        HeatMapCell cell = node(event);
        cell.setStroke(style.getTimelineCellStroke());
        cell.setStrokePaint(style.getTimelineCellStrokeColor());

        Pair<FlowtiLine, FlowtiLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(false);
          lines.second().setHighlighted(false);
        }
        setEdgeCentroidsHighlighted(cell, NodeEdgePos.SOURCE, false);
        setEdgeCentroidsHighlighted(cell, NodeEdgePos.TARGET, false);

        updateMapAreaColorsOnHeatMapCellHover(cell, false);
      }

      private Pair<FlowtiLine, FlowtiLine> lines(PInputEvent event) {
        return edgesToLines.get(node(event).getEdge());
      }

      @Override
      public void mouseClicked(PInputEvent event) {
        if (event.isControlDown()) {
          setEgdeForSimilaritySorting(node(event).getEdge());
        }
      }
    };
  }

  private void setEgdeForSimilaritySorting(Edge edge) {
    flowMapGraph.setEgdeForSimilaritySorting(edge);
    updateVisibleEdges();
  }

  private String getColumnValueAttrName(String columnAttr) {
    return valueType.getColumnValueAttr(getFlowMapGraph().getAttrSpec(), columnAttr);
  }

  private void updateMapAreaColorsOnHeatMapCellHover(HeatMapCell cell, boolean hover) {
    Edge edge = cell.getEdge();
    String attr = cell.getWeightAttr();
    if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {
      List<Edge> edges = FlowMapGraphEdgeAggregator.getBaseAggregateList(edge);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, NodeEdgePos.SOURCE);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, NodeEdgePos.TARGET);
    } else {
      double value = edge.getDouble(getValueType().getColumnValueAttr(
          flowMapGraph.getAttrSpec(), attr));
      colorizeMapArea(layers.getSourceNodeId(edge), value, hover, NodeEdgePos.SOURCE);
      colorizeMapArea(layers.getTargetNodeId(edge), value, hover, NodeEdgePos.TARGET);
    }
  }

  private void updateMapAreaColorsOnHeatMapColumnLabelHover(String columnAttr, boolean hover) {
    Iterable<Edge> edges;
    if (isFilterApplied()) {
      edges = getVisibleEdges();
    } else {
      edges = flowMapGraph.edges();
    }
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, NodeEdgePos.SOURCE);
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, NodeEdgePos.TARGET);
  }

  private void colorizeMapAreasWithBaseNodeSummaries(String weightAttr, boolean hover,
      Iterable<Edge> edges, NodeEdgePos s) {
    for (Node node : Nodes.nodesOfEdges(edges, s)) {
      double value = FlowMapSummaries.getWeightSummary(node, getColumnValueAttrName(weightAttr), s.dir());
      String areaId = flowMapGraph.getNodeId(node);
      colorizeMapArea(areaId, value, hover, s);
    }
  }

  private void colorizeMapArea(String areaId, double value, boolean hover, NodeEdgePos s) {
    VisualArea area = getVisualAreaMap(s).getVisualAreaBy(areaId);
    if (area != null  &&  !area.isEmpty()) {
      Color color;
      if (hover) {
        color = getColorFor(value);
      } else {
        color = mapColorScheme.getColor(ColorCodes.AREA_PAINT);
      }
      area.setPaint(color);
    } else {
      Color color;
      if (hover) {
        color = getColorFor(value);
      } else {
        color = style.getMapAreaCentroidLabelPaint();
      }
      getNodeIdsToCentroids(s).get(areaId).getLabelNode().setPaint(color);
    }
  }

  public Color getColorFor(HeatMapCell cell) {
    String attr = valueType.getColumnValueAttr(cell.getFlowMapGraph().getAttrSpec(), cell.getWeightAttr());
    double value = cell.getEdge().getDouble(attr);
    return getColorFor(value);
  }

  public Color getColorFor(double value) {
    return getColorForWeight(value, valueType.getMinMax(getStats()));
  }

  public Color getColorForWeight(double weight, MinMax wstats) {
    FlowstratesStyle style = getStyle();
    if (Double.isNaN(weight)) {
      return style.getMissingValueColor();
    }
    double val =
//      wstats.normalizeAroundZero(
      wstats.normalizeLogAroundZero(
        weight, true);
    if (val < -1.0 || val > 1.0) {
      return Color.green;
    }
    if (wstats.getMin() < 0 && wstats.getMax() > 0) {
      // use diverging color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(divergingColorScheme.getColors(),
          val, -1.0, 1.0, 255, interpolateColors));
    } else {
      // use sequential color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(sequentialColorScheme.getColors(),
//          wstats.normalizeLog(weight),
          val,
          0.0, 1.0, 255, interpolateColors));
    }
  }

  public FlowstratesStyle getStyle() {
    return style;
  }

  private double getTupleY(int row) {
    return row * cellHeight;
  }

  @Override
  protected String getTooltipHeaderFor(PNode node) {
    return ((HeatMapCell) node).getTooltipHeader();
  }

  @Override
  protected String getTooltipLabelsFor(PNode node) {
    return ((HeatMapCell) node).getTooltipLabels();
  }

  @Override
  protected String getTooltipValuesFor(PNode node) {
    return ((HeatMapCell) node).getTooltipValues();
  }

  @Override
  protected Point2D getTooltipPosition(PNode node) {
    if (PNodes.getRootAncestor(node) == heatmapLayer) {
      PBounds bounds = node.getGlobalBounds();
      heatmapCamera.viewToLocal(bounds);
      heatmapCamera.localToGlobal(bounds);
      return new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
    } else {
      return super.getTooltipPosition(node);
    }
  }

  private boolean fitInViewOnce = false;
  private RowOrderings rowOrdering = RowOrderings.SRC_VPOS;
  private FlowMapStats visibleEdgesStats;

  private Rectangle2D centroidsBounds(NodeEdgePos s) {
    Rectangle2D.Double b = new Rectangle2D.Double();
    boolean first = true;
    for (Centroid c : getNodeIdsToCentroids(s).values()) {
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

  @Override
  public void fitInView() {
    layoutCameraNode(sourcesCamera, -1, 0, .30, 1.0);
    layoutCameraNode(heatmapCamera, 0, 0, .40, 1.0);
    layoutCameraNode(targetsCamera, +1, 0, .30, 1.0);

    if (!fitInViewOnce) {
      fitHeatMap();
    }
    updateFlowLinePositions();

    /*
     * getVisualCanvas().setViewZoomPoint(camera.getViewBounds().getCenter2D());
     */
  }

  private void fitHeatMap() {
    fintInCameraView(NodeEdgePos.SOURCE);

    PBounds heatmapBounds = heatmapLayer.getFullBounds();
    if (heatmapBounds.height > heatmapBounds.width * 10) {
      heatmapBounds.height = heatmapBounds.width * heatmapCamera.getViewBounds().height
          / heatmapCamera.getWidth();
    }
    heatmapCamera.setViewBounds(GeomUtils.growRect(heatmapBounds, .025, .1, .025, .1));

    fintInCameraView(NodeEdgePos.TARGET);

    fitInViewOnce = true;
  }

  private void fintInCameraView(NodeEdgePos s) {
    getVisualAreaMapCamera(s).setViewBounds(GeomUtils.growRect(centroidsBounds(s), .2, .2, .2, .2));
  }

  private void layoutCameraNode(PCamera camera, double halign, double valign, double hsizeProportion,
      double vsizeProportion) {

    PBounds globalViewBounds = getCamera().getViewBounds();

    // reserve space for the caption header
    PText caption = (PText)camera.getAttribute(CAPTION_NODE_ATTR);
    if (caption != null) {
      globalViewBounds.y += CAPTION_HEADER_HEIGHT;
      globalViewBounds.height -= CAPTION_HEADER_HEIGHT;
    }

    // align the camera node
    PBounds viewBounds = camera.getViewBounds();
    PNodes.alignNodeInBounds_bySetBounds(camera, globalViewBounds, halign, valign, hsizeProportion,
        vsizeProportion);
    camera.setViewBounds(viewBounds);

    // align caption
    if (caption != null) {
      PBounds capb = caption.getFullBoundsReference();
      PBounds camb = camera.getBoundsReference();
      PNodes.setPosition(caption,
          camb.x + (camb.width - capb.width)/2,
          (CAPTION_HEADER_HEIGHT - capb.height)/2);
    }
  }

  public void setRowOrdering(RowOrderings rowOrder) {
    if (this.rowOrdering != rowOrder) {
      this.rowOrdering = rowOrder;
      flowMapGraph.setEgdeForSimilaritySorting(null);
      updateVisibleEdges();
    }
  }

  public RowOrderings getRowOrdering() {
    return rowOrdering;
  }

  public FlowMapStats getStats() {
    if (focusOnVisibleRows) {
      return getVisibleEdgesStats();
    } else {
      // return flowMapGraph.getStats();
      return layers.getStatsForVisibleEdges();
    }
  }

  private FlowMapStats getVisibleEdgesStats() {
    List<Edge> edges = getVisibleEdges();
    if (visibleEdgesStats == null) {
      visibleEdgesStats = EdgeListFlowMapStats.createFor(edges, flowMapGraph.getAttrSpec());
    }
    return visibleEdgesStats;
  }

}

