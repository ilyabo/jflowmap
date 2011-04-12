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
import jflowmap.data.FlowMapStats;
import jflowmap.data.FlowMapSummaries;
import jflowmap.data.MinMax;
import jflowmap.geo.MapProjection;
import jflowmap.geom.GeomUtils;
import jflowmap.models.map.AreaMap;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import jflowmap.views.Legend;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowmap.AbstractLegendItemProducer;
import jflowmap.views.flowmap.ColorSchemeAware;
import jflowmap.views.flowmap.FlowMapView;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
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
  private FlowLinesColoringMode flowLinesColoringMode = FlowLinesColoringMode.ORIGIN;
  private int maxVisibleTuples;

  private ColorSchemes sequentialColorScheme = ColorSchemes.OrRd;
  private ColorSchemes divergingColorScheme = ColorSchemes.RdBu5;

  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

  private final PNode mapToMatrixLinesLayer;

  private Map<Edge, Pair<FlowLine, FlowLine>> edgesToLines;

  private List<Edge> visibleEdges;
  private Predicate<Edge> customEdgeFilter;

  private final HeatmapLayer heatmapLayer;

  private final MapLayer originsMapLayer;
  private final MapLayer destsMapLayer;

  private Map<String, Color> flowLinesPalette;

  private boolean focusOnVisibleRows = false;


  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.get(code);
    }
  };
  private final FlowMapGraphAggLayers layers;
  private Legend legend;

  private final MapProjection mapProjection;


  public FlowstratesView(
      FlowMapGraph flowMapGraph, AreaMap areaMap, AggLayersBuilder aggLayersBuilder,
      int maxVisibleTuples, MapProjection mapProjection) {


    logger.info("Opening flowstrates view");


    if (aggLayersBuilder == null) {
      aggLayersBuilder = new DefaultAggLayersBuilder();
    }
    this.layers = aggLayersBuilder.build(flowMapGraph);

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

    originsMapLayer = new MapLayer(this, areaMap, FlowEndpoints.ORIGIN);
    destsMapLayer = new MapLayer(this, areaMap, FlowEndpoints.DEST);

    addCaption(originsMapLayer.getMapLayerCamera(), "Origins");
    if (SHOW_TIME_CAPTION) {
      addCaption(heatmapLayer.getHeatmapCamera(), "Time");
    }
    addCaption(destsMapLayer.getMapLayerCamera(), "Destinations");

    PLayer canvasLayer = canvas.getLayer();
    canvasLayer.addChild(originsMapLayer.getMapLayerCamera());
    canvasLayer.addChild(heatmapLayer.getHeatmapCamera());
    canvasLayer.addChild(destsMapLayer.getMapLayerCamera());

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

          originsMapLayer.updateCentroids();
          destsMapLayer.updateCentroids();

          updateFlowLinePositions();
          // getVisualCanvas().setViewZoomPoint(getCamera().getViewBounds().getCenter2D());
        }
      }
    };
    originsMapLayer.getMapLayerCamera().addPropertyChangeListener(linesUpdater);
    destsMapLayer.getMapLayerCamera().addPropertyChangeListener(linesUpdater);
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
    legend = new Legend(
        new Color(220, 220, 220, 225),
        new Color(60, 60, 60, 200),

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
  }

  void updateLegend() {
    legend.update();
  }

  public void addPropertyChangeListener(Properties prop, PropertyChangeListener listener) {
    changes.addPropertyChangeListener(prop.name(), listener);
  }

  void fireNodeSelectionChanged(List<String> old, List<String> nodeIds) {
    changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
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

  public void setSelectedAggLayer(String layerName) {
    layers.setSelectedLayer(layerName);
    this.visibleEdges = null;
    heatmapLayer.renewHeatmap();
    heatmapLayer.fitHeatMapInView();
  }

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
        return
         (customEdgeFilter == null || customEdgeFilter.apply(edge))  &&

         (originsMapLayer.isNodeSelectionEmpty()  ||
          originsMapLayer.nodeSelectionContains(edge.getSourceNode()))  &&

         (destsMapLayer.isNodeSelectionEmpty()  ||
          destsMapLayer.nodeSelectionContains(edge.getTargetNode()));
      }
    };
  }

  private void updateFlowLinesPalette() {
    if (flowLinesColoringMode == FlowLinesColoringMode.SAME_COLOR) {
      if (flowLinesPalette != null)
        flowLinesPalette.clear();
      return;
    }
    Set<String> ids = Sets.newHashSet();
    for (Edge e : visibleEdges) {
      switch (flowLinesColoringMode) {
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

  private Iterable<Edge> removeEdgesWithOnlyNaNs(Iterable<Edge> edges) {
    return Iterables.filter(edges, new Predicate<Edge>() {
      @Override public boolean apply(Edge e) { return flowMapGraph.hasNonZeroWeight(e); }
    });
  }

  List<Edge> getVisibleEdges() {
    if (visibleEdges == null) {
      List<Edge> edges = Lists.newArrayList(
          getTopEdges(Iterables.filter(removeEdgesWithOnlyNaNs(layers.getVisibleEdges()),
          getEdgePredicate())));

      Collections.sort(edges, rowOrdering.getComparator(flowMapGraph));

      visibleEdges = edges;
      visibleEdgesStats = null;

      updateFlowLinesPalette();
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

  public void setShowLinesForHighligtedOnly(boolean showLinesForHighligtedOnly) {
//    this.showFlowLinesForHighligtedNodesOnly = showLinesForHighligtedOnly;
    renewFlowLines();
  }

  void renewFlowLines() {
    mapToMatrixLinesLayer.removeAllChildren();

    edgesToLines = Maps.newHashMap();

    for (Edge edge : getVisibleEdges()) {
      FlowLine origin = createFlowLine(edge);
      FlowLine dest = createFlowLine(edge);
      edgesToLines.put(edge, Pair.of(origin, dest));
    }

    updateFlowLineColors();
    updateFlowLinePositions();
  }

  public FlowLinesColoringMode getFlowLinesColoringMode() {
    return flowLinesColoringMode;
  }

  public void setFlowLinesColoringMode(FlowLinesColoringMode flowLinesColoringMode) {
    this.flowLinesColoringMode = flowLinesColoringMode;
    updateFlowLinesPalette();
    updateFlowLineColors();
  }

  private FlowLine createFlowLine(Edge edge) {
    FlowLine line = new FlowLine();
    mapToMatrixLinesLayer.addChild(line);
    return line;
  }

  private void updateFlowLineColors() {
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

  Pair<FlowLine, FlowLine> getFlowLinesOf(Edge edge) {
    return edgesToLines.get(edge);
  }

  private Pair<Color, Color> getFlowLineColors(Edge edge) {
    Color c;
    switch (flowLinesColoringMode) {
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


  private void updateFlowLinePositions() {
    int row = 0;

    for (Edge edge : getVisibleEdges()) {
      Pair<FlowLine, FlowLine> lines = edgesToLines.get(edge);
      Pair<PText, PText> labels = heatmapLayer.getEdgeLabels(edge);

      updateFlowLine(row, edge, lines.first(), labels.first(), FlowEndpoints.ORIGIN);
      updateFlowLine(row, edge, lines.second(), labels.second(), FlowEndpoints.DEST);

      row++;
    }

    mapToMatrixLinesLayer.repaint();
  }

  private void updateFlowLine(int row, Edge edge, FlowLine line, PText label, FlowEndpoints ep) {

    PCamera hmcam = heatmapLayer.getHeatmapCamera();
    PBounds viewBounds = hmcam.getViewBounds();

    MapLayer mapLayer = (ep == FlowEndpoints.ORIGIN ? originsMapLayer : destsMapLayer);

    Point2D p1 = mapLayer.getCentroidPoint(edge);
    boolean visible =
      (p1 != null  &&  mapLayer.getMapLayerCamera().getViewBounds().contains(p1));

    if (visible) {
      Point2D.Double p2 = heatmapLayer.getHeatmapRowFlowInOutPoint(row, ep);
      visible = (visible  &&  viewBounds.contains(p2));
      if (visible) {
        mapLayer.getMapLayerCamera().viewToLocal(p1);
        hmcam.viewToLocal(p2);
        Rectangle2D lb = hmcam.viewToLocal(label.getBounds());

        line.setPoint(0, p1.getX(), p1.getY());

        if (ep == FlowEndpoints.ORIGIN) {
          line.setPoint(1, p2.x - lb.getWidth(), p2.y + lb.getHeight() / 2);
          line.setPoint(2, p2.x, p2.y + lb.getHeight() / 2);
        } else {
          line.setPoint(1, p2.x + lb.getWidth(), p2.y + lb.getHeight() / 2);
          line.setPoint(2, p2.x, p2.y + lb.getHeight() / 2);
        }
      }
    }
    line.setVisible(visible);
    line.setPickable(false);
  }


  /**
   * @return True if the selection was not empty
   */
  private boolean clearNodeSelection() {
    if (originsMapLayer.clearNodeSelection()  ||  destsMapLayer.clearNodeSelection()) {
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
      !originsMapLayer.isNodeSelectionEmpty()  ||
      !destsMapLayer.isNodeSelectionEmpty() ||
      customEdgeFilter != null;
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

  void updateMapsOnHeatmapCellHover(HeatmapCell cell, boolean hover) {
    originsMapLayer.updateMapAreaColorsOnHeatmapCellHover(cell, hover);
    destsMapLayer.updateMapAreaColorsOnHeatmapCellHover(cell, hover);

    originsMapLayer.setEdgeCentroidsHighlighted(cell, hover);
    destsMapLayer.setEdgeCentroidsHighlighted(cell, hover);
  }

  void updateMapsOnHeatmapColumnHover(String columnAttr, boolean hover) {
    originsMapLayer.updateOnHeatmapColumnHover(columnAttr, hover);
    destsMapLayer.updateOnHeatmapColumnHover(columnAttr, hover);
  }

  void setEgdeForSimilaritySorting(Edge edge) {
    flowMapGraph.setEgdeForSimilaritySorting(edge);
    updateVisibleEdges();
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

  @Override
  public void fitInView() {
    layoutCameraNode(originsMapLayer.getMapLayerCamera(), -1, -1, .30, .96);
    layoutCameraNode(heatmapLayer.getHeatmapCamera(), 0, 0, .40, 1.0);
    layoutCameraNode(destsMapLayer.getMapLayerCamera(), +1, -1, .30, .96);

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
    fitInCameraView(originsMapLayer);
    heatmapLayer.fitHeatMapInView();
    fitInCameraView(destsMapLayer);

    fitInViewOnce = true;
  }

  private void fitInCameraView(MapLayer layer) {
    Rectangle2D nb = GeomUtils.growRectByPercent(layer.centroidsBounds(this), .2, .2, .2, .2);
    layer.getMapLayerCamera().setViewBounds(nb);
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

}
