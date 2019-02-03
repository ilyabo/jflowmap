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
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import jflowmap.AbstractCanvasView;
import jflowmap.ColorSchemes;
import jflowmap.FlowEndpoint;
import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphAggLayers;
import jflowmap.data.EdgeListFlowMapStats;
import jflowmap.data.FlowMapNodeTotals;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import jflowmap.data.ViewConfig;
import jflowmap.geo.MapProjection;
import jflowmap.models.map.GeoMap;
import jflowmap.util.ColorUtils;
import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PButton;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.ColorCodes;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowmap.ColorSchemeAware;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.util.ColorLib;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesView extends AbstractCanvasView {

  private static final String ACTION_FIT_IN_VIEW = "fitInView";
  private static final String ACTION_FIT_WHOLE_IN_VIEW = "fitWholeInView";
  private static final String ACTION_CLEAR_SELECTION = "clearSelection";

  public static Logger logger = Logger.getLogger(FlowstratesView.class);

  public static final String VIEW_CONFIG_PROP_MAX_VISIBLE_TUPLES = "view.flowstrates.maxVisibleTuples";
  public static final String VIEW_CONFIG_PROP_MSG_ORIGINS_CAPTION = "view.flowstrates.messages.originsMapCaption";
  public static final String VIEW_CONFIG_PROP_MSG_DESTS_CAPTION = "view.flowstrates.messages.destsMapCaption";

  private static final int LEGEND_MARGIN_BOTTOM = 10;
  private static final String CAPTION_NODE_ATTR = "captionNode";

  public final static long fitInViewDuration(boolean animate) { return animate ? 500 : 0; }

  enum Properties {
    CUSTOM_EDGE_FILTER, NODE_SELECTION
  }

  private static final Font CAPTION_FONT = new Font("Arial", Font.BOLD, 23);
  private static final int MAX_NODE_LABEL_LENGTH = 30;


//  private static final boolean SHOW_TIME_CAPTION = true;

  private boolean interpolateColors = true;

  private final FlowstratesStyle style = new DefaultFlowstratesStyle();
  private final FlowMapGraph flowMapGraph;
  // private final PScrollPane scrollPane;

  private JPanel controlPanel;
  private ValueType valueType = ValueType.VALUE;
  private int maxVisibleTuples;

  private ColorSchemes sequentialColorScheme = ColorSchemes.OrRd;
  private ColorSchemes divergingColorScheme = ColorSchemes.RdBu5;

  private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

  private FlowLinesLayerNode flowLinesLayerNode;

  private List<Edge> visibleEdges;
  private Map<Edge, Integer> visibleEdgeToIndex;
  private Predicate<Edge> customEdgeFilter;

  private TemporalViewLayer temporalLayer;

  private final GeoMap areaMap;
  private MapLayer originMapLayer;
  private MapLayer destMapLayer;

  private boolean focusOnVisibleRows = true;


  private final ColorSchemeAware mapColorScheme = new ColorSchemeAware() {
    @Override
    public Color getColor(ColorCodes code) {
      return FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.get(code);
    }
  };
  private final FlowMapGraphAggLayers layers;
  private FlowstratesLegend legend;

  private final MapProjection mapProjection;

  private SeqStat valueStat;

  private PBoxLayoutNode mainButtonPanel;
  private PBoxLayoutNode originsMapButtonPanel;
  private PBoxLayoutNode destMapButtonPanel;

  private PBoxLayoutNode temporalViewButtonPanel;

  protected ViewLayer mouseOverLayer;
  protected List<ViewLayer> viewLayers;
  private final ViewConfig viewConfig;



  public FlowstratesView(FlowMapGraph fmg, GeoMap areaMap, MapProjection proj, ViewConfig config) {
    String aggName = config.getString(ViewConfig.PROP_DATA_AGGREGATOR);
    AggLayersBuilder aggregator;
    if (aggName != null) {
      aggregator = FlowMapGraphAggLayers.createBuilder(aggName);
    } else {
      aggregator = null;
    }

    logger.info("Opening flowstrates view");

    this.flowMapGraph = fmg;
    this.maxVisibleTuples = config.getIntOrElse(FlowstratesView.VIEW_CONFIG_PROP_MAX_VISIBLE_TUPLES, -1);
    this.mapProjection = proj;
    this.areaMap = areaMap;
    this.viewConfig = config;

    if (aggregator == null) {
      aggregator = new DefaultAggLayersBuilder();
    }
    this.layers = aggregator.build(fmg);

    for (FlowMapGraph g : layers.getFlowMapGraphs()) {
      g.addEdgeWeightDifferenceColumns();
      g.addEdgeWeightRelativeDifferenceColumns();

      FlowMapNodeTotals.supplyNodesWithWeightTotals(g);
      FlowMapNodeTotals.supplyNodesWithWeightTotals(g, g.getEdgeWeightDiffAttr());
      FlowMapNodeTotals.supplyNodesWithWeightTotals(g, g.getEdgeWeightRelativeDiffAttrNames());
    }


    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        beforeInitialize();
        initialize();
      }
    });
  }

  private void beforeInitialize() {
    VisualCanvas canvas = getVisualCanvas();
    canvas.setAutoFitOnBoundsChange(false);
    canvas.setBackground(style.getBackgroundColor());
    PPanEventHandler panHandler = new PPanEventHandler();
    panHandler.setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !Lasso.isControlOrAppleCmdDown(event) && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });
    canvas.setPanEventHandler(panHandler);
    canvas.getZoomHandler().setEventFilter(new PInputEventFilter() {
      @Override
      public boolean acceptsEvent(PInputEvent event, int type) {
        return !Lasso.isControlOrAppleCmdDown(event) && // shouldn't pan when using lasso
            (event.getCamera() != getVisualCanvas().getCamera());
      }
    });

    // canvas.setMinZoomScale(1e-1);
    // canvas.setMaxZoomScale(1e+2);

    canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    canvas.setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);


    originMapLayer = new MapLayer(FlowstratesView.this, areaMap, FlowEndpoint.ORIGIN);
    destMapLayer = new MapLayer(FlowstratesView.this, areaMap, FlowEndpoint.DEST);

    temporalLayer = new FastHeatmapLayer(this);
//    temporalLayer = new HeatmapLayer(this);

    viewLayers = ImmutableList.<ViewLayer>of(originMapLayer, temporalLayer, destMapLayer);


    addCaption(originMapLayer.getCamera(),
        viewConfig.getStringOrElse(VIEW_CONFIG_PROP_MSG_ORIGINS_CAPTION, "Origins"));

//    if (SHOW_TIME_CAPTION) {
//      addCaption(temporalLayer.getCamera(), "Time");
//    }
    addCaption(temporalLayer.getCamera(), " ");

    addCaption(destMapLayer.getCamera(),
        viewConfig.getStringOrElse(VIEW_CONFIG_PROP_MSG_DESTS_CAPTION, "Destinations"));

    PLayer canvasLayer = canvas.getLayer();
    canvasLayer.addChild(temporalLayer.getCamera()); // should come first so that lasso shows above it
    canvasLayer.addChild(originMapLayer.getCamera());
    canvasLayer.addChild(destMapLayer.getCamera());


    flowLinesLayerNode = new FlowLinesLayerNode(FlowstratesView.this);
    getCamera().addChild(flowLinesLayerNode);


    controlPanel = new FlowstratesControlPanel(FlowstratesView.this);

    // scrollPane = new PScrollPane(canvas);
    // scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // scrollPane.setVerticalScrollBarPolicy(PScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    FlowstratesView.this.valueStat = stdValueStat();

    legend = new FlowstratesLegend(FlowstratesView.this);
    getCamera().addChild(legend);

  }

  /**
   * This method must run in EDT
   */
  private void initialize() {
    resetVisibleEdges();
//  getVisualCanvas().repaint();

    getCamera().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PCamera.PROPERTY_BOUNDS) {
          layoutChildren();
          fitInView();
        }
      }
    });

    PropertyChangeListener linesUpdater = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        hideTooltip();

        originMapLayer.updateCentroids();
        destMapLayer.updateCentroids();

        flowLinesLayerNode.updateFlowLines();
        // getVisualCanvas().setViewZoomPoint(getCamera().getViewBounds().getCenter2D());
      }
    };
    originMapLayer.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, linesUpdater);
    destMapLayer.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, linesUpdater);
    temporalLayer.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, linesUpdater);

    createButtons();

//  getVisualCanvas().requestFocus();
    initKeystrokes();

  }

  private void initKeystrokes() {
    getCamera().addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseMoved(PInputEvent event) {
        for (ViewLayer layer : viewLayers) {
          if (event.getCamera() == layer.getCamera()) {
            mouseOverLayer = layer;
          }
        }
      }
    });

    VisualCanvas canvas = getVisualCanvas();

    canvas.getInputMap().put(KeyStroke.getKeyStroke("F5"), ACTION_FIT_IN_VIEW);
    canvas.getActionMap().put(ACTION_FIT_IN_VIEW, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mouseOverLayer != null) {
          mouseOverLayer.fitInView(true, false);
        }
      }
    });

    canvas.getInputMap().put(KeyStroke.getKeyStroke("F6"), ACTION_FIT_WHOLE_IN_VIEW);
    canvas.getActionMap().put(ACTION_FIT_WHOLE_IN_VIEW, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mouseOverLayer != null) {
          mouseOverLayer.fitInView(true, true);
        }
      }
    });

    canvas.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), ACTION_CLEAR_SELECTION);
    canvas.getActionMap().put(ACTION_CLEAR_SELECTION, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mouseOverLayer == originMapLayer) {
          originMapLayer.setSelectedNodes(null);
        }
        if (mouseOverLayer == destMapLayer) {
          destMapLayer.setSelectedNodes(null);
        }
      }
    });

//    canvas.setFocusable(true);
//    canvas.requestFocus();
  }

  private void createButtons() {

    //  History buttons

//    PBoxLayoutNode buttonPanel0 = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
//    PNodes.setPosition(buttonPanel0, 4, 4);
//    getVisualCanvas().getLayer().addChild(buttonPanel0);
//    buttonPanel0.addChild(new PButton(" < ", false));
//    buttonPanel0.addChild(new PButton(" > ", false));




    //  Main buttons panel

    mainButtonPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);

//    final PButton explainButton = new PButton("EXPLAIN", true);
//    buttonPanel.addChild(explainButton);


    getVisualCanvas().getLayer().addChild(mainButtonPanel);

    final PButton linesButton = new PButton("LINES", true);
    linesButton.setPressed(flowLinesLayerNode.getShowAllFlowLines());
    linesButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        getFlowLinesLayerNode().setShowAllFlowLines(linesButton.isPressed());
      }
    });
    mainButtonPanel.addChild(linesButton);

    final PButton diffButton = new PButton("DIFF", true);
    diffButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        if (diffButton.isPressed()) {
          setValueType(ValueType.DIFF);
        } else {
          setValueType(ValueType.VALUE);
        }
      }
    });
    mainButtonPanel.addChild(diffButton);


    mainButtonPanel.addChild(new PText("   "));
    final PButton groupByOriginButton = new PButton("BY ORIGIN", true);
    final PButton groupByDestButton = new PButton(" BY DEST ", true);
    final PButton allToAllButton = new PButton("GROUP ALL", true);
    mainButtonPanel.addChild(groupByOriginButton);
    mainButtonPanel.addChild(groupByDestButton);
    mainButtonPanel.addChild(allToAllButton);

    groupByOriginButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        if (groupByOriginButton.isPressed()) {
          groupByDestButton.setPressed(false);
          allToAllButton.setPressed(false);
          setSelectedAggLayer(DefaultAggLayersBuilder.BY_ORIGIN_LAYER);
          temporalLayer.fitInView(true, true);
        } else {
          setSelectedAggLayer(null);
          temporalLayer.fitInView(false, false);
        }
      }
    });


    groupByDestButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        if (groupByDestButton.isPressed()) {
          groupByOriginButton.setPressed(false);
          allToAllButton.setPressed(false);
          setSelectedAggLayer(DefaultAggLayersBuilder.BY_DEST_LAYER);
          temporalLayer.fitInView(true, true);
        } else {
          setSelectedAggLayer(null);
          temporalLayer.fitInView(false, false);
        }
      }
    });

    allToAllButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        if (allToAllButton.isPressed()) {
          groupByOriginButton.setPressed(false);
          groupByDestButton.setPressed(false);
          setSelectedAggLayer(DefaultAggLayersBuilder.ALL_TO_ALL_LAYER);
          temporalLayer.fitInView(true, true);
        } else {
          setSelectedAggLayer(null);
          temporalLayer.fitInView(false, false);
        }
      }
    });


    mainButtonPanel.addChild(new PText("   "));

    final PButton sortByMaxButton = new PButton("SORT BY MAX", false);
    mainButtonPanel.addChild(sortByMaxButton);
    sortByMaxButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        setRowOrdering(getValueType().getHeatmapRowByMaxOrdering());
        temporalLayer.repaint();
      }
    });


    final PButton sortByAvgButton = new PButton("SORT BY AVG", false);
    mainButtonPanel.addChild(sortByAvgButton);
    sortByAvgButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        setRowOrdering(getValueType().getHeatmapRowByAvgOrdering());
        temporalLayer.repaint();
      }
    });



    //  Origins map buttons

    originsMapButtonPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
    getVisualCanvas().getLayer().addChild(originsMapButtonPanel);

    PButton originsClearButton = new PButton("CLEAR");
    originsClearButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        originMapLayer.setSelectedNodes(null);
      }
    });
    originsMapButtonPanel.addChild(originsClearButton);

    PButton originsFitButton = new PButton("FIT");
    originsFitButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        originMapLayer.fitInView(true, false);
      }
    });
    originsMapButtonPanel.addChild(originsFitButton);



    //  Destinations map buttons

    destMapButtonPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
    getVisualCanvas().getLayer().addChild(destMapButtonPanel);


    PButton destClearButton = new PButton("CLEAR");
    destClearButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        destMapLayer.setSelectedNodes(null);
      }
    });
    destMapButtonPanel.addChild(destClearButton);

    PButton destFitButton = new PButton("FIT");
    destFitButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        destMapLayer.fitInView(true, false);
      }
    });
    destMapButtonPanel.addChild(destFitButton);


    //  Temporal view buttons

    temporalViewButtonPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
    getVisualCanvas().getLayer().addChild(temporalViewButtonPanel);


    PButton tempFitButton = new PButton("FIT");
    tempFitButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        temporalLayer.fitInView(true, false);
      }
    });
    temporalViewButtonPanel.addChild(tempFitButton);

    PButton tempFitAllButton = new PButton("SHOW ALL");
    tempFitAllButton.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        temporalLayer.fitInView(true, true);
      }
    });
    temporalViewButtonPanel.addChild(tempFitAllButton);


  }

  private void anchorButtonPanels() {
    PNodes.setPosition(mainButtonPanel, temporalLayer.getCamera().getBounds().x + 5, 4);

//    PNodes.anchorNodeToBoundsOf(mainButtonPanel, temporalLayer.getCamera(),
//        PCanvas.LEFT_ALIGNMENT, PCanvas.TOP_ALIGNMENT, 5, 5);

    PNodes.anchorNodeToBoundsOf(originsMapButtonPanel, originMapLayer.getCamera(),
        PCanvas.RIGHT_ALIGNMENT, PCanvas.BOTTOM_ALIGNMENT, 5, 5);
    PNodes.anchorNodeToBoundsOf(destMapButtonPanel, destMapLayer.getCamera(),
        PCanvas.RIGHT_ALIGNMENT, PCanvas.BOTTOM_ALIGNMENT, 5, 5);
    PNodes.anchorNodeToBoundsOf(temporalViewButtonPanel, temporalLayer.getCamera(),
        PCanvas.RIGHT_ALIGNMENT, PCanvas.BOTTOM_ALIGNMENT, 5, 5);
  }

  public void resetVisibleEdges() {
    this.visibleEdges = null;
    this.visibleEdgeToIndex = null;
    resetValueStat();
    temporalLayer.renew();
    getFlowLinesLayerNode().renewFlowLines();
    updateLegend();
  }

  void updateColors() {
    temporalLayer.updateColors();
    updateLegend();
    getVisualCanvas().repaint();
  }

  public TemporalViewLayer getTemporalLayer() {
    return temporalLayer;
  }

  public MapLayer getMapLayer(FlowEndpoint ep) {
    switch (ep) {
      case ORIGIN: return originMapLayer;
      case DEST: return destMapLayer;
      default: throw new AssertionError();
    }
  }

  public FlowLinesLayerNode getFlowLinesLayerNode() {
    return flowLinesLayerNode;
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

  void updateLegend() {
    legend.update();
  }

  @Override
  public String getName() {
    return viewConfig.getName();
  }

  @Override
  public String getSpec() {
    return getName(); // TODO: add more info
  }

  public Iterable<String> getAggLayerNames() {
    return layers.getLayerNames();
  }

  FlowMapGraphAggLayers getAggLayers() {
    return layers;
  }

  public void setSelectedAggLayer(String layerName) {
    layers.setSelectedLayer(layerName);
    resetVisibleEdges();
    temporalLayer.fitInView(false, false);
  }

  public void setMaxVisibleTuples(int maxVisibleTuples) {
    if (this.maxVisibleTuples != maxVisibleTuples) {
      this.maxVisibleTuples = maxVisibleTuples;
      resetVisibleEdges();
      temporalLayer.fitInView(false, false);
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
      updateColors();
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
    updateVisibleEdges(true);
  }

  void updateVisibleEdges(boolean fitInView) {
    resetVisibleEdges();
    if (fitInView) {
      temporalLayer.fitInView(false, false);
    }
  }

  private Predicate<Edge> getEdgePredicate() {
    return new Predicate<Edge>() {
      @Override
      public boolean apply(Edge edge) {
        return
         (customEdgeFilter == null || customEdgeFilter.apply(edge))  &&

         (originMapLayer.isNodeSelectionEmpty()  ||
          originMapLayer.nodeSelectionContains(edge.getSourceNode()))  &&

         (destMapLayer.isNodeSelectionEmpty()  ||
          destMapLayer.nodeSelectionContains(edge.getTargetNode()));
      }
    };
  }

  private List<Edge> getTopEdges(Iterable<Edge> edges) {
    List<Edge> list = Lists.newArrayList(edges);

    // Sort by magnitude
    Collections.sort(list, RowOrderings.MAX_MAGNITUDE_IN_ROW.getComparator(this));

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
          getTopEdges(Iterables.filter(removeEdgesWithOnlyNaNs(layers.getEdges()),
          getEdgePredicate())));

      Collections.sort(edges, rowOrdering.getComparator(this));

      visibleEdges = edges;
      visibleEdgesStats = null;

      if (flowLinesLayerNode != null) {
        flowLinesLayerNode.updatePalette();
      }
    }

    return visibleEdges;
  }

  public int getVisibleEdgeIndex(Edge edge) {
    Integer idx = getOrInitVisibleEdgesToIndex().get(edge);
    if (idx == null) {
      return -1;
    }
    return idx;
  }

  public Edge getVisibleEdge(int index) {
    return getVisibleEdges().get(index);
  }

  public String getEdgeWeightAttr(int index) {
    return getFlowMapGraph().getEdgeWeightAttrs().get(index);
  }

  public int getEdgeWeightAttrIndex(String attr) {
    return getFlowMapGraph().getEdgeWeightAttrs().indexOf(attr);
  }

  private Map<Edge, Integer> getOrInitVisibleEdgesToIndex() {
    if (visibleEdgeToIndex == null) {
      Map<Edge, Integer> map = Maps.newHashMap();
      int row = 0;
      for (Edge e : getVisibleEdges()) {
        map.put(e, row++);
      }
      visibleEdgeToIndex = map;
    }
    return visibleEdgeToIndex;
  }


  public void setValueType(ValueType valueType) {
    if (this.valueType != valueType) {
      this.valueType = valueType;
      resetValueStat();
    }
  }

  public ValueType getValueType() {
    return valueType;
  }

  public double getValue(Edge edge, String attr) {
    ValueType vtype = getValueType();
    FlowMapAttrSpec attrSpec = getFlowMapGraph().getAttrSpec();

    return edge.getDouble(vtype.getColumnValueAttr(attrSpec, attr));
  }

  public void setDivergingColorScheme(ColorSchemes divergingColorScheme) {
    if (this.divergingColorScheme != divergingColorScheme) {
      this.divergingColorScheme = divergingColorScheme;
      updateColors();
    }
  }

  public ColorSchemes getDivergingColorScheme() {
    return divergingColorScheme;
  }

  public void setSequentialColorScheme(ColorSchemes sequentialColorScheme) {
    if (this.sequentialColorScheme != sequentialColorScheme) {
      this.sequentialColorScheme = sequentialColorScheme;
      updateColors();
    }
  }

  public ColorSchemes getSequentialColorScheme() {
    return sequentialColorScheme;
  }

  public void setInterpolateColors(boolean interpolateColors) {
    if (this.interpolateColors != interpolateColors) {
      this.interpolateColors = interpolateColors;
      updateColors();
    }
  }

  public boolean getInterpolateColors() {
    return interpolateColors;
  }

  public FlowMapGraph getFlowMapGraph() {
    return flowMapGraph;
  }

  public void setShowLinesForHighligtedOnly(boolean showLinesForHighligtedOnly) {
//    this.showFlowLinesForHighligtedNodesOnly = showLinesForHighligtedOnly;
    flowLinesLayerNode.renewFlowLines();
  }

  /**
   * @return True if the selection was not empty
   */
  private boolean clearNodeSelection() {
    if (originMapLayer.clearNodeSelection()  ||  destMapLayer.clearNodeSelection()) {
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
      !originMapLayer.isNodeSelectionEmpty()  ||
      !destMapLayer.isNodeSelectionEmpty() ||
      customEdgeFilter != null;
  }

//  @Override
//  public JComponent getViewComponent() {
//     return scrollPane;
//  }

  @Override
  public JComponent getControls() {
    return controlPanel;
  }

  public RowOrdering getRowOrdering() {
    return rowOrdering;
  }

  public SeqStat getValueStat() {
    return valueStat;
  }

  /**
   * Used for coloring the heatmap cells, the areas in the maps and the legend.
   */
  public void setValueStat(SeqStat wstat) {
    if (!wstat.equals(valueStat)) {
      this.valueStat = wstat;
      temporalLayer.resetWeightAttrTotals();
      updateColors();
    }
  }

  /**
   * Reset to the normal value (only those values which are shown in the heatmap).
   */
  void resetValueStat() {
    setValueStat(stdValueStat());
  }

  private SeqStat stdValueStat() {
    return valueType.getSeqStat(getFlowMapStats());
  }

  private FlowMapStats getFlowMapStats() {
    if (focusOnVisibleRows) {
      return getVisibleEdgesStats();
    } else {
      return layers.getStats();
    }
  }

  private FlowMapStats getVisibleEdgesStats() {
    List<Edge> edges = getVisibleEdges();
    if (visibleEdgesStats == null) {
      visibleEdgesStats = EdgeListFlowMapStats.createFor(edges, flowMapGraph.getAttrSpec());
    }
    return visibleEdgesStats;
  }

  void setEgdeForSimilaritySorting(Edge edge) {
    flowMapGraph.setEgdeForSimilaritySorting(edge);
    updateVisibleEdges();
  }

  public Color getColorFor(double value) {
    return getColorFor(value, getValueStat());
  }

  public Color getColorFor(double value, SeqStat wstats) {
    FlowstratesStyle style = getStyle();
    if (Double.isNaN(value)) {
      return style.getMissingValueColor();
    }
    double val = wstats.normalizer().normalizeLogAroundZero(value, true);
              // wstats.normalizeAroundZero(
    if (wstats.isDiverging()) {
      if (val < -1.0 || val > 1.0) {
        return Color.green;  // out of color scale
      }
      // use diverging color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(divergingColorScheme.getColors(),
          val, -1.0, 1.0, 255, interpolateColors));
    } else {
      if (val < 0.0 || val > 1.0) {
        return Color.green;
      }
      // use sequential color scheme
      return ColorLib.getColor(ColorUtils.colorFromMap(sequentialColorScheme.getColors(),
          val, 0.0, 1.0, 255, interpolateColors));
      // wstats.normalizeLog(weight),
    }
  }

  public FlowstratesStyle getStyle() {
    return style;
  }

  @Override
  protected Point2D getTooltipPosition(PNode node) {
    if (PNodes.getRootAncestor(node) == temporalLayer) {
      PBounds bounds = node.getGlobalBounds();
      temporalLayer.getCamera().viewToLocal(bounds);
      temporalLayer.getCamera().localToGlobal(bounds);
      return new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
    } else {
      return super.getTooltipPosition(node);
    }
  }

  private boolean fitInViewOnce = false;
  private RowOrdering rowOrdering = RowOrderings.MAX_MAGNITUDE_IN_ROW;
  private FlowMapStats visibleEdgesStats;

  @Override
  public void fitInView() {
    if (!fitInViewOnce) {
      fitAllInView();
    }
    flowLinesLayerNode.updateFlowLines();

    /*
     * getVisualCanvas().setViewZoomPoint(camera.getViewBounds().getCenter2D());
     */
  }

  private void layoutChildren() {
    layoutCameraNode(originMapLayer.getCamera(), PCanvas.LEFT_ALIGNMENT, PCanvas.TOP_ALIGNMENT, .30, 1.0);
    layoutCameraNode(temporalLayer.getCamera(), PCanvas.CENTER_ALIGNMENT, PCanvas.CENTER_ALIGNMENT, .40, 1.0);
    layoutCameraNode(destMapLayer.getCamera(), PCanvas.RIGHT_ALIGNMENT, PCanvas.TOP_ALIGNMENT, .30, 1.0);


    anchorButtonPanels();


    PBounds lb = legend.getFullBoundsReference();
    PBounds vb = getCamera().getViewBounds();
    PNodes.moveTo(legend, legend.getX(), (vb.getMaxY() - lb.getHeight()) - lb.getY() - LEGEND_MARGIN_BOTTOM);

    flowLinesLayerNode.updateFlowLines();
  }

  private void fitAllInView() {
    originMapLayer.fitInView(false, false);
    temporalLayer.fitInView(false, false);
    destMapLayer.fitInView(false, false);

    fitInViewOnce = true;
  }

  @Override
  protected TooltipText getTooltipTextFor(PNode node) {
    if (node instanceof HeatmapCell) {
      HeatmapCell cell = (HeatmapCell)node;
      return new TooltipText(getFlowMapGraph(), cell.getEdge(), cell.getWeightAttr());
    }
    return super.getTooltipTextFor(node);
  }

  private void layoutCameraNode(PCamera camera, float halign, float valign, double hsizeProportion,
      double vsizeProportion) {

    PBounds globalViewBounds = getCamera().getViewBounds();

    double topMargin4Caption = 0;

    // reserve space for the caption header
    PText caption = (PText) camera.getAttribute(CAPTION_NODE_ATTR);
    if (caption != null) {
      PBounds capb = caption.getFullBoundsReference();
      topMargin4Caption = capb.getMaxY() + capb.getHeight() * .5;
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

  public void setRowOrdering(RowOrdering rowOrder) {
    if (this.rowOrdering != rowOrder) {
      this.rowOrdering = rowOrder;
      flowMapGraph.setEgdeForSimilaritySorting(null);
      updateVisibleEdges(false);
    }
  }

  public void addPropertyChangeListener(Properties prop, PropertyChangeListener listener) {
    changes.addPropertyChangeListener(prop.name(), listener);
  }

  void fireNodeSelectionChanged(List<String> old, List<String> nodeIds) {
    changes.firePropertyChange(Properties.NODE_SELECTION.name(), old, nodeIds);
  }

  public List<String> getEdgeWeightAttrs() {
    return flowMapGraph.getEdgeWeightAttrs();
  }

  public static String shortenNodeLabel(String label) {
    if (label.length() < MAX_NODE_LABEL_LENGTH) return label;
    return label.substring(0, MAX_NODE_LABEL_LENGTH-2) + "...";
  }
}
