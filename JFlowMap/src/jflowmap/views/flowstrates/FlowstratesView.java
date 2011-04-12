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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import jflowmap.AbstractCanvasView;
import jflowmap.ColorSchemes;
import jflowmap.FlowEndpoints;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.data.EdgeListFlowMapStats;
import jflowmap.data.FlowMapGraphEdgeAggregator;
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.data.Nodes;
import jflowmap.geo.MapProjection;
import jflowmap.geom.GeomUtils;
import jflowmap.models.map.AreaMap;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
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
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesView extends AbstractCanvasView {

  public static Logger logger = Logger.getLogger(FlowstratesView.class);

  public static final String VIEW_CONFIG_PROP_MAX_VISIBLE_TUPLES = "view.flowstrates.maxVisibleTuples";

  private static final int LEGEND_MARGIN_BOTTOM = 10;
  private static final String CAPTION_NODE_ATTR = "captionNode";

  enum Properties {
    CUSTOM_EDGE_FILTER, NODE_SELECTION
  }

  private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 23);

  private final float FLOW_LINES_ALPHA = .1f; // .3f;

  private static final boolean SHOW_TIME_CAPTION = false;

  private boolean interpolateColors = true;

  private final FlowstratesStyle style = new DefaultFlowstratesStyle();
  private final FlowMapGraph flowMapGraph;
  // private final PScrollPane scrollPane;

  private final JPanel controlPanel;
  private ValueType valueType = ValueType.VALUE;
  private FlowLinesColoringMode flowtiLinesColoringMode = FlowLinesColoringMode.ORIGIN;
  private int maxVisibleTuples;

  private ColorSchemes sequentialColorScheme = ColorSchemes.OrRd;
  private ColorSchemes divergingColorScheme = ColorSchemes.RdBu5;

  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

  private final PNode mapToMatrixLinesLayer;

  private Map<Edge, Pair<FlowLine, FlowLine>> edgesToLines;

  private List<Edge> visibleEdges;
  private Predicate<Edge> customEdgeFilter;

  private final HeatmapLayer heatmapLayer;

  private final GeoLayer originsGeoLayer;
  private final GeoLayer destsGeoLayer;

  private Map<String, Color> flowLinesPalette;

  private boolean showFlowtiLinesForHighligtedNodesOnly = false;
  private boolean focusOnVisibleRows = false;

  private List<String> selOriginNodes, selDestNodes;

  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.get(code);
    }
  };
  private final FlowMapGraphAggLayers layers;
  private Legend legend;

  private final MapProjection mapProjection;

  // public FlowstratesView(FlowMapGraph flowMapGraph, AreaMap areaMap) {
  // this(flowMapGraph, areaMap, null, -1, MapProjections.MERCATOR);
  // }

  public FlowstratesView(FlowMapGraph flowMapGraph, AreaMap areaMap, AggLayersBuilder aggLayersBuilder,
      int maxVisibleTuples, MapProjection mapProjection) {

    logger.info("Opening flowstrates view");


    if (aggLayersBuilder == null) {
      aggLayersBuilder = new DefaultAggLayersBuilder();
    }
    this.layers = aggLayersBuilder.build(flowMapGraph);
    // this.layers = RefugeeAggLayers.createAggLayers(flowMapGraph);
    for (FlowMapGraph fmg : layers.getFlowMapGraphs()) {
      fmg.addEdgeWeightDifferenceColumns();
      fmg.addEdgeWeightRelativeDifferenceColumns();
    }

    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph);
    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph, flowMapGraph.getEdgeWeightDiffAttr());
    FlowMapSummaries.supplyNodesWithWeightSummaries(flowMapGraph,
        flowMapGraph.getEdgeWeightRelativeDiffAttrNames());


    this.flowMapGraph = flowMapGraph;
    this.maxVisibleTuples = maxVisibleTuples;
    this.mapProjection = mapProjection;

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

    heatmapLayer = new HeatmapLayer(this);

    originsGeoLayer = new GeoLayer(this, areaMap, FlowEndpoints.ORIGIN);
    destsGeoLayer = new GeoLayer(this, areaMap, FlowEndpoints.DESTINATION);

    addCaption(originsGeoLayer.getGeoLayerCamera(), "Origins");
    if (SHOW_TIME_CAPTION) {
      addCaption(heatmapLayer.getHeatmapCamera(), "Time");
    }
    addCaption(destsGeoLayer.getGeoLayerCamera(), "Destinations");

    PLayer canvasLayer = canvas.getLayer();
    canvasLayer.addChild(originsGeoLayer.getGeoLayerCamera());
    canvasLayer.addChild(heatmapLayer.getHeatmapCamera());
    canvasLayer.addChild(destsGeoLayer.getGeoLayerCamera());

    controlPanel = new FlowstratesControlPanel(this);

    // scrollPane = new PScrollPane(canvas);
    // scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // scrollPane.setVerticalScrollBarPolicy(PScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


    mapToMatrixLinesLayer = new PNode();
    getCamera().addChild(mapToMatrixLinesLayer);

    createLegend();

    logger.info("Creating heatmap");
    heatmapLayer.renewHeatmap();
    logger.info("Done creating heatmap");

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
    originsGeoLayer.getGeoLayerCamera().addPropertyChangeListener(linesUpdater);
    destsGeoLayer.getGeoLayerCamera().addPropertyChangeListener(linesUpdater);
    heatmapLayer.getHeatmapCamera().addPropertyChangeListener(linesUpdater);
  }

  ColorSchemeAware getMapColorScheme() {
    return mapColorScheme;
  }

  MapProjection getMapProjection() {
    return mapProjection;
  }

  private void addCaption(PCamera camera, String caption) {
    PText textNode = new PText(caption);
    textNode.setFont(CAPTION_FONT);
    camera.addAttribute(CAPTION_NODE_ATTR, textNode);
    camera.addChild(textNode);
  }

  private void createLegend() {
    legend = new Legend(new Color(220, 220, 220, 225), new Color(60, 60, 60, 200),
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
            // return new PText(getFlowMapGraph().getId() + "\n" + getValueType());
            return null;
          }

          @Override
          public MinMax getMinMax() {
            return getValueType().getMinMax(getStats());
          }

        });
    legend.setOffset(5, 12);
    legend.setScale(1.4);
    heatmapLayer.getHeatmapCamera().addChild(legend);
    // PBounds b = heatmapLayer.getHeatmapCamera().getViewBounds();
    // legend.offset(b.x, b.y);
  }

  public void updateLegend() {
    legend.update();
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

  FlowMapGraphAggLayers getAggLayers() {
    return layers;
  }

  public void setActiveAggLayer(String layerName) {
    layers.setActiveLayer(layerName);
    this.visibleEdges = null;
    heatmapLayer.renewHeatmap();
    heatmapLayer.fitHeatMapInView();
  }

  // public void addPropertyChangeListener(Properties prop, PropertyChangeListener listener) {
  // changes.addPropertyChangeListener(prop.name(), listener);
  // }

  public void setMaxVisibleTuples(int maxVisibleTuples) {
    if (this.maxVisibleTuples != maxVisibleTuples) {
      this.maxVisibleTuples = maxVisibleTuples;
      this.visibleEdges = null;
      heatmapLayer.renewHeatmap();
      heatmapLayer.fitHeatMapInView();
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
      heatmapLayer.updateHeatmapColors();
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

  void updateVisibleEdges() {
    visibleEdges = null;
    heatmapLayer.renewHeatmap();
    heatmapLayer.fitHeatMapInView();
  }

  private Predicate<Edge> getEdgePredicate() {
    return new Predicate<Edge>() {
      @Override
      public boolean apply(Edge edge) {
        return (customEdgeFilter == null || customEdgeFilter.apply(edge))
            && (selOriginNodes == null || selOriginNodes
                .contains(flowMapGraph.getNodeId(edge.getSourceNode())))
            && (selDestNodes == null || selDestNodes.contains(flowMapGraph.getNodeId(edge.getTargetNode())));
      }
    };
  }

  private void updateFlowtiLinesPalette() {
    if (flowtiLinesColoringMode == FlowLinesColoringMode.SAME_COLOR) {
      if (flowLinesPalette != null)
        flowLinesPalette.clear();
      return;
    }
    Set<String> ids = Sets.newHashSet();
    for (Edge e : visibleEdges) {
      switch (flowtiLinesColoringMode) {
      case ORIGIN:
        ids.add(flowMapGraph.getSourceNodeId(e));
        break;
      case DEST:
        ids.add(flowMapGraph.getTargetNodeId(e));
        break;
      }
    }
    flowLinesPalette = new HashMap<String, Color>(ids.size());
    Color[] palette = ColorUtils.createCategoryColors(ids.size(), FLOW_LINES_ALPHA);
    int i = 0;
    for (String origin : ids) {
      flowLinesPalette.put(origin, palette[i++]);
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

  List<Edge> getVisibleEdges() {
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

  public void setValueType(ValueType valueType) {
    if (this.valueType != valueType) {
      this.valueType = valueType;
      heatmapLayer.updateHeatmapColors();
    }
  }

  public ValueType getValueType() {
    return valueType;
  }

  public void setDivergingColorScheme(ColorSchemes divergingColorScheme) {
    if (this.divergingColorScheme != divergingColorScheme) {
      this.divergingColorScheme = divergingColorScheme;
      heatmapLayer.updateHeatmapColors();
    }
  }

  public ColorSchemes getDivergingColorScheme() {
    return divergingColorScheme;
  }

  public void setSequentialColorScheme(ColorSchemes sequentialColorScheme) {
    if (this.sequentialColorScheme != sequentialColorScheme) {
      this.sequentialColorScheme = sequentialColorScheme;
      heatmapLayer.updateHeatmapColors();
    }
  }

  public ColorSchemes getSequentialColorScheme() {
    return sequentialColorScheme;
  }

  public void setInterpolateColors(boolean interpolateColors) {
    if (this.interpolateColors != interpolateColors) {
      this.interpolateColors = interpolateColors;
      heatmapLayer.updateHeatmapColors();
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

  private GeoLayer getGeoLayer(FlowEndpoints s) {
    switch (s) {
    case ORIGIN:
      return originsGeoLayer;
    case DESTINATION:
      return destsGeoLayer;
    }
    throw new AssertionError();
  }

  private VisualAreaMap getVisualAreaMap(FlowEndpoints s) {
    return getGeoLayer(s).getVisualAreaMap();
  }

  List<String> getSelectedNodes(FlowEndpoints s) {
    switch (s) {
    case ORIGIN:
      return selOriginNodes;
    case DESTINATION:
      return selDestNodes;
    }
    throw new AssertionError();
  }

  private Map<String, Centroid> getNodeIdsToCentroids(FlowEndpoints s) {
    return getGeoLayer(s).getNodeIdsToCentroids();
  }

  public void setShowLinesForHighligtedOnly(boolean showLinesForHighligtedOnly) {
    this.showFlowtiLinesForHighligtedNodesOnly = showLinesForHighligtedOnly;
    renewFlowtiLines();
  }

  void renewFlowtiLines() {
    mapToMatrixLinesLayer.removeAllChildren();

    edgesToLines = Maps.newHashMap();

    // if (!showFlowtiLinesForHighligtedNodesOnly)
    {
      for (Edge edge : getVisibleEdges()) {
        FlowLine origin = createFlowtiLine(edge);
        FlowLine dest = createFlowtiLine(edge);
        // origin.setVisible(!showFlowtiLinesForHighligtedNodesOnly || origin.isHighlighted());
        // dest.setVisible(!showFlowtiLinesForHighligtedNodesOnly || dest.isHighlighted());
        edgesToLines.put(edge, Pair.of(origin, dest));
      }
    }

    updateFlowtiLineColors();
    updateFlowLinePositions();
  }

  public FlowLinesColoringMode getFlowtiLinesColoringMode() {
    return flowtiLinesColoringMode;
  }

  public void setFlowtiLinesColoringMode(FlowLinesColoringMode flowLinesColoringMode) {
    this.flowtiLinesColoringMode = flowLinesColoringMode;
    updateFlowtiLinesPalette();
    updateFlowtiLineColors();
  }

  private FlowLine createFlowtiLine(Edge edge) {
    FlowLine line = new FlowLine();
    mapToMatrixLinesLayer.addChild(line);
    return line;
  }

  private void updateFlowtiLineColors() {
    // if (showFlowtiLinesForHighligtedNodesOnly) {
    // return;
    // }

    for (Map.Entry<Edge, Pair<FlowLine, FlowLine>> e : edgesToLines.entrySet()) {
      Pair<FlowLine, FlowLine> p = e.getValue();
      Pair<Color, Color> colors = getFlowLineColors(e.getKey());

      FlowLine originLine = p.first();
      originLine.setColor(colors.first());
      originLine.setHighlightedColor(colors.second());

      FlowLine destLine = p.second();
      destLine.setColor(colors.first());
      destLine.setHighlightedColor(colors.second());
    }
  }

  private Pair<Color, Color> getFlowLineColors(Edge edge) {
    Color c;
    switch (flowtiLinesColoringMode) {
    case SAME_COLOR:
      return Pair.of(style.getFlowLineColor(), style.getFlowLineHighlightedColor());

    case ORIGIN:
      c = flowLinesPalette.get(flowMapGraph.getSourceNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    case DEST:
      c = flowLinesPalette.get(flowMapGraph.getTargetNodeId(edge));
      return Pair.of(c, ColorUtils.setAlpha(c, 255));

    }
    throw new AssertionError();
  }

  private void updateCentroids() {
    getGeoLayer(FlowEndpoints.ORIGIN).updateCentroids();
    getGeoLayer(FlowEndpoints.DESTINATION).updateCentroids();
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

  private Point2D getCentroidPoint(Edge edge, FlowEndpoints s) {
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
    PBounds heatMapViewBounds = heatmapLayer.getHeatmapCamera().getViewBounds();
    int row = 0;
    for (Edge edge : getVisibleEdges()) {
      Pair<FlowLine, FlowLine> lines = edgesToLines.get(edge);
      Pair<PText, PText> labels = heatmapLayer.getEdgeLabels(edge);

      Point2D originCentroidPoint = getCentroidPoint(edge, FlowEndpoints.ORIGIN);
      boolean inVis = originCentroidPoint != null
          && originsGeoLayer.getGeoLayerCamera().getViewBounds().contains(originCentroidPoint);
      FlowLine lineIn = lines.first();
      if (inVis) {
        Point2D.Double matrixIn = heatmapLayer.getMatrixInPoint(row);
        inVis = inVis && heatMapViewBounds.contains(matrixIn);
        if (inVis) {
          originsGeoLayer.getGeoLayerCamera().viewToLocal(originCentroidPoint);
          heatmapLayer.getHeatmapCamera().viewToLocal(matrixIn);
          lineIn.setPoint(0, originCentroidPoint.getX(), originCentroidPoint.getY());
          Rectangle2D fromLabelBounds = heatmapLayer.getHeatmapCamera().viewToLocal(
              labels.first().getBounds());
          lineIn.setPoint(1, matrixIn.x - fromLabelBounds.getWidth(),
              matrixIn.y + fromLabelBounds.getHeight() / 2);
          lineIn.setPoint(2, matrixIn.x, matrixIn.y + fromLabelBounds.getHeight() / 2);
        }
      }
      lineIn.setVisible(inVis);
      lineIn.setPickable(false);

      Point2D destCentroidPoint = getCentroidPoint(edge, FlowEndpoints.DESTINATION);
      boolean outVis = destCentroidPoint != null
          && destsGeoLayer.getGeoLayerCamera().getViewBounds().contains(destCentroidPoint);
      FlowLine lineOut = lines.second();
      if (outVis) {
        Point2D.Double matrixOut = heatmapLayer.getMatrixOutPoint(row);
        outVis = outVis && heatMapViewBounds.contains(matrixOut);
        if (outVis) {
          destsGeoLayer.getGeoLayerCamera().viewToLocal(destCentroidPoint);
          heatmapLayer.getHeatmapCamera().viewToLocal(matrixOut);
          lineOut.setPoint(0, destCentroidPoint.getX(), destCentroidPoint.getY());
          Rectangle2D toLabelBounds = heatmapLayer.getHeatmapCamera()
              .viewToLocal(labels.second().getBounds());
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

  /**
   * @returns List of ids of selected nodes, or null if no nodes were selected
   */
  List<String> applyLassoToNodeCentroids(Shape shape, FlowEndpoints s) {
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

  /**
   * @return True if the selection was not empty
   */
  private boolean clearNodeSelection() {
    if (selOriginNodes != null || selDestNodes != null) {
      selOriginNodes = selDestNodes = null;
      getGeoLayer(FlowEndpoints.ORIGIN).updateCentroidColors();
      getGeoLayer(FlowEndpoints.DESTINATION).updateCentroidColors();
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
    return (selOriginNodes != null && selOriginNodes.size() > 0)
        || (selDestNodes != null && selDestNodes.size() > 0) || (customEdgeFilter != null);
  }

  void setSelectedNodes(List<String> nodeIds, FlowEndpoints s) {
    switch (s) {
    case ORIGIN:
      if (selOriginNodes != nodeIds) {
        List<String> old = selOriginNodes;
        selOriginNodes = nodeIds;
        changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
      }
      break;
    case DESTINATION:
      if (selDestNodes != nodeIds) {
        List<String> old = selDestNodes;
        selDestNodes = nodeIds;
        changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
      }
      break;
    default:
      throw new AssertionError();
    }
  }

  //
  // private void anchorRightVisualAreaMap() {
  // PNodes.moveTo(originsVisualAreaMap, getCamera().getViewBounds().getMaxX() -
  // originsVisualAreaMap.getFullBoundsReference().getWidth(), 0);
  // }

  // private void updateMapColors(FlowMapGraph fmg) {
  // colorizeMap(originVisualAreaMap, fmg, EdgeDirection.INCOMING);
  // colorizeMap(destVisualAreaMap, fmg, EdgeDirection.OUTGOING);
  // }

  // private void schedule(PActivity activity) {
  // getCamera().getRoot().getActivityScheduler().addActivity(activity);
  // }


  void setNodeHighlighted(final String nodeId, FlowEndpoints s, boolean highlighted) {
    Centroid c = getNodeIdsToCentroids(s).get(nodeId);
    if (c != null) {
      c.setHighlighted(highlighted);
      c.setTimeSliderVisible(highlighted);
    }
    Predicate<Node> acceptNodes = new Predicate<Node>() {
      @Override
      public boolean apply(Node node) {
        return nodeId.equals(flowMapGraph.getNodeId(node));
      }
    };
    for (Edge e : s.filterByNodePredicate(visibleEdges, acceptNodes)) {
      Pair<FlowLine, FlowLine> pair = edgesToLines.get(e);
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

  @Override
  public JComponent getViewComponent() {
    // return scrollPane;
    return getVisualCanvas();
  }

  @Override
  public JComponent getControls() {
    return controlPanel;
  }

  @Override
  public String getControlsLayoutConstraint() {
    return BorderLayout.NORTH;
  }

  private void setEdgeCentroidsHighlighted(HeatmapCell hmcell, FlowEndpoints npos, boolean highlighted) {
    Node node = flowMapGraph.getNodeOf(hmcell.getEdge(), npos);
    Centroid c = getNodeIdsToCentroids(npos).get(flowMapGraph.getNodeId(node));
    if (c != null) {
      c.setHighlighted(highlighted);
    }
  }

  PTypedBasicInputEventHandler<HeatmapCell> createHeatMapCellHoverListener() {
    return new PTypedBasicInputEventHandler<HeatmapCell>(HeatmapCell.class) {
      @Override
      public void mouseEntered(PInputEvent event) {
        HeatmapCell cell = node(event);
        // updateMapColors(node.getFlowMapGraph());

        // highlight cell
        cell.moveToFront();
        cell.setStroke(style.getSelectedTimelineCellStroke());
        cell.setStrokePaint(style.getHeatmapSelectedCellStrokeColor());

        // highlight flow lines
        Pair<FlowLine, FlowLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(true);
          lines.second().setHighlighted(true);
        }
        setEdgeCentroidsHighlighted(cell, FlowEndpoints.ORIGIN, true);
        setEdgeCentroidsHighlighted(cell, FlowEndpoints.DESTINATION, true);

        updateMapAreaColorsOnHeatMapCellHover(cell, true);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        // updateMapColors(null);
        HeatmapCell cell = node(event);
        cell.setStroke(style.getTimelineCellStroke());
        cell.setStrokePaint(style.getTimelineCellStrokeColor());

        Pair<FlowLine, FlowLine> lines = lines(event);
        if (lines != null) {
          lines.first().setHighlighted(false);
          lines.second().setHighlighted(false);
        }
        setEdgeCentroidsHighlighted(cell, FlowEndpoints.ORIGIN, false);
        setEdgeCentroidsHighlighted(cell, FlowEndpoints.DESTINATION, false);

        updateMapAreaColorsOnHeatMapCellHover(cell, false);
      }

      private Pair<FlowLine, FlowLine> lines(PInputEvent event) {
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

  private void colorizeMapArea(String areaId, double value, boolean hover, FlowEndpoints s) {
    VisualArea area = getVisualAreaMap(s).getVisualAreaBy(areaId);
    if (area != null && !area.isEmpty()) {
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

  public Color getColorFor(double value) {
    return getColorForWeight(value, valueType.getMinMax(getStats()));
  }

  public Color getColorForWeight(double weight, MinMax wstats) {
    FlowstratesStyle style = getStyle();
    if (Double.isNaN(weight)) {
      return style.getMissingValueColor();
    }
    double val =
    // wstats.normalizeAroundZero(
    wstats.normalizeLogAroundZero(weight, true);
    if (val < -1.0 || val > 1.0) {
      return Color.green;
    }
    if (wstats.getMin() < 0 && wstats.getMax() > 0) {
      // use diverging color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(divergingColorScheme.getColors(), val, -1.0, 1.0, 255,
          interpolateColors));
    } else {
      // use sequential color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(sequentialColorScheme.getColors(),
      // wstats.normalizeLog(weight),
          val, 0.0, 1.0, 255, interpolateColors));
    }
  }

  public FlowstratesStyle getStyle() {
    return style;
  }

  @Override
  protected String getTooltipHeaderFor(PNode node) {
    return ((HeatmapCell) node).getTooltipHeader();
  }

  @Override
  protected String getTooltipLabelsFor(PNode node) {
    return ((HeatmapCell) node).getTooltipLabels();
  }

  @Override
  protected String getTooltipValuesFor(PNode node) {
    return ((HeatmapCell) node).getTooltipValues();
  }

  @Override
  protected Point2D getTooltipPosition(PNode node) {
    if (PNodes.getRootAncestor(node) == heatmapLayer) {
      PBounds bounds = node.getGlobalBounds();
      heatmapLayer.getHeatmapCamera().viewToLocal(bounds);
      heatmapLayer.getHeatmapCamera().localToGlobal(bounds);
      return new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
    } else {
      return super.getTooltipPosition(node);
    }
  }

  private boolean fitInViewOnce = false;
  private RowOrderings rowOrdering = RowOrderings.SRC_VPOS;
  private FlowMapStats visibleEdgesStats;

  private Rectangle2D centroidsBounds(FlowEndpoints s) {
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
    layoutCameraNode(originsGeoLayer.getGeoLayerCamera(), -1, -1, .30, .96);
    layoutCameraNode(heatmapLayer.getHeatmapCamera(), 0, 0, .40, 1.0);
    layoutCameraNode(destsGeoLayer.getGeoLayerCamera(), +1, -1, .30, .96);

    if (!fitInViewOnce) {
      fitMapsInView();
    }
    updateFlowLinePositions();

    PBounds lb = legend.getFullBoundsReference();
    PBounds vb = getCamera().getViewBounds();
    PNodes.moveTo(legend, legend.getX(), (vb.getMaxY() - lb.getHeight()) - lb.getY() - LEGEND_MARGIN_BOTTOM);

    /*
     * getVisualCanvas().setViewZoomPoint(camera.getViewBounds().getCenter2D());
     */
  }

  private void fitMapsInView() {
    fintInCameraView(FlowEndpoints.ORIGIN);
    heatmapLayer.fitHeatMapInView();
    fintInCameraView(FlowEndpoints.DESTINATION);
    fitInViewOnce = true;
  }

  private void fintInCameraView(FlowEndpoints s) {
    getGeoLayer(s).getGeoLayerCamera().setViewBounds(
        GeomUtils.growRectByPercent(centroidsBounds(s), .2, .2, .2, .2));
  }

  private void layoutCameraNode(PCamera camera, double halign, double valign, double hsizeProportion,
      double vsizeProportion) {

    PBounds globalViewBounds = getCamera().getViewBounds();

    double topMargin4Caption = 0;

    // reserve space for the caption header
    PText caption = (PText) camera.getAttribute(CAPTION_NODE_ATTR);
    if (caption != null) {
      PBounds capb = caption.getFullBoundsReference();
      topMargin4Caption = capb.getMaxY() + capb.getHeight() * .25;
      globalViewBounds.y += topMargin4Caption;
      globalViewBounds.height -= topMargin4Caption;
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
      PNodes.setPosition(caption, camb.x + (camb.width - capb.width) / 2,
          (topMargin4Caption - capb.height) / 2);
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

  void updateMapAreaColorsOnHeatMapCellHover(HeatmapCell cell, boolean hover) {
    Edge edge = cell.getEdge();
    String attr = cell.getWeightAttr();
    if (FlowMapGraphEdgeAggregator.isAggregate(edge)) {
      List<Edge> edges = FlowMapGraphEdgeAggregator.getBaseAggregateList(edge);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, FlowEndpoints.ORIGIN);
      colorizeMapAreasWithBaseNodeSummaries(attr, hover, edges, FlowEndpoints.DESTINATION);
    } else {
      double value = edge.getDouble(getValueType().getColumnValueAttr(flowMapGraph.getAttrSpec(), attr));
      colorizeMapArea(layers.getSourceNodeId(edge), value, hover, FlowEndpoints.ORIGIN);
      colorizeMapArea(layers.getTargetNodeId(edge), value, hover, FlowEndpoints.DESTINATION);
    }
  }

  void updateMapAreaColorsOnHeatMapColumnLabelHover(String columnAttr, boolean hover) {
    Iterable<Edge> edges;
    if (isFilterApplied()) {
      edges = getVisibleEdges();
    } else {
      edges = flowMapGraph.edges();
    }
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, FlowEndpoints.ORIGIN);
    colorizeMapAreasWithBaseNodeSummaries(columnAttr, hover, edges, FlowEndpoints.DESTINATION);
  }

  private void colorizeMapAreasWithBaseNodeSummaries(String weightAttr, boolean hover, Iterable<Edge> edges,
      FlowEndpoints s) {
    for (Node node : Nodes.nodesOfEdges(edges, s)) {
      double value = FlowMapSummaries.getWeightSummary(node, getColumnValueAttrName(weightAttr), s.dir());
      String areaId = flowMapGraph.getNodeId(node);
      colorizeMapArea(areaId, value, hover, s);
    }
  }

}
