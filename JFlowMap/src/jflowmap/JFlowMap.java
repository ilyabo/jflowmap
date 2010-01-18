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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.data.FlowMapStats;
import jflowmap.data.GraphFileFormats;
import jflowmap.models.FlowMapModel;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.ColorCodes;
import jflowmap.visuals.ColorScheme;
import jflowmap.visuals.VisualAreaMap;
import jflowmap.visuals.VisualFlowMap;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 *         Date: 23-Sep-2009
 */
public class JFlowMap extends JComponent {

    private static final long serialVersionUID = -1898747650184999568L;

    private static Logger logger = Logger.getLogger(JFlowMap.class);

    private final PCanvas canvas;
    private final ControlPanel controlPanel;
    private VisualFlowMap visualFlowMap;
    private final Frame app;
    private ColorScheme colorScheme;

    public JFlowMap(FlowMapMain app, boolean showControlPanel) {
        this(app, (List<DatasetSpec>)null, showControlPanel);
    }

    public JFlowMap(FlowMapMain app, DatasetSpec datasetSpec, boolean showControlPanel) {
        this(app, ImmutableList.of(datasetSpec), showControlPanel);
    }

    public JFlowMap(FlowMapMain app, List<DatasetSpec> datasetSpecs, boolean showControlPanel) {
        setLayout(new BorderLayout());

        this.app = app;

        Builder<ColorCodes, Color> colors = ImmutableMap.builder();
        this.colorScheme =
//            ColorScheme.of("Dark", colors
//                .put(ColorCodes.BACKGROUND, new Color(0x20, 0x20, 0x20))
//                .put(ColorCodes.AREA_PAINT, new Color(45, 45, 45))
//                .put(ColorCodes.AREA_STROKE, new Color(55, 55, 55))
//                .put(ColorCodes.NODE_PAINT, new Color(255, 255, 255, 90))
//                .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
//                .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
//                .put(ColorCodes.NODE_STROKE_PAINT, new Color(255, 255, 255, 200))
//                .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))
//                .put(ColorCodes.NODE_CLUSTER_ORIG_NODE_PAINT, new Color(100, 100, 100, 100))
//                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
//                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
//                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))
//                .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(255, 255, 0))
//                .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(255, 255, 255))
//                .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 0, 0))
//                .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 255, 0))
//                .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 0, 0))
//                .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(0, 0, 0))
//                .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 255, 0))
//                .build());
//
            ColorScheme.of("Light Blue", colors
                    .put(ColorCodes.BACKGROUND, new Color(196, 224, 255))
//                .put(ColorCodes.BACKGROUND, new Color(216, 234, 255))
                .put(ColorCodes.AREA_PAINT, new Color(255, 255, 255))
                .put(ColorCodes.AREA_STROKE, new Color(225, 225, 225))

                .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
                .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(200, 200, 0, 200))
                .put(ColorCodes.NODE_SELECTED_PAINT, new Color(200, 200, 0, 200))
                .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
                .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(255, 255, 0, 255))
                .put(ColorCodes.NODE_CLUSTER_ORIG_NODE_PAINT, new Color(100, 100, 100, 100))

                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(0, 0, 255, 200))
                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(255, 0, 0, 200))
                .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(0, 255, 0, 200))
                .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(255, 255, 0))
                .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(0, 0, 0))

                .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(255, 255, 255))
                .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(20, 20, 200))

                .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(255, 255, 255))
                .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 0, 0))

//                .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
//                .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 255, 0))
                .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(255, 255, 255))
                .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(48, 156, 1))

                .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
                .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 0, 0))
                .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(255, 255, 255))
//                .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 255, 0))
                .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(8, 156, 1))

                .put(ColorCodes.LEDGEND_BOX_PAINT, new Color(240, 240, 240, 200))
                .put(ColorCodes.LEDGEND_TEXT, new Color(0, 0, 0, 120))
                .put(ColorCodes.LEDGEND_ARROW, new Color(0, 0, 0, 120))
                .build());

//            ColorScheme.of("Inverted", colors
//                  .put(ColorCodes.BACKGROUND, new Color(223, 223, 223))
//                  .put(ColorCodes.AREA_PAINT, new Color(210, 210, 210))
//                  .put(ColorCodes.AREA_STROKE, new Color(200, 200, 200))
//                  .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
//
//                  .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(55, 55, 255, 200))
//                  .put(ColorCodes.NODE_SELECTED_PAINT, new Color(55, 55, 255, 200))
//                  .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
//                  .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(0, 0, 255, 255))
//                  .put(ColorCodes.NODE_CLUSTER_ORIG_NODE_PAINT, new Color(155, 155, 155, 100))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(255, 255, 0, 200))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(0, 255, 255, 200))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(255, 0, 255, 200))
//
//                  .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(0, 0, 255))
//
//                  .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))
//
//                  .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(255, 0, 255))
//                  .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(0, 255, 255))
//
//                  .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(255, 0, 255))
//                  .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(0, 255, 255))
//                  .build());

//            ColorScheme.of("Gray red-green", colors
//                  .put(ColorCodes.BACKGROUND, new Color(255, 255, 255))
//                  .put(ColorCodes.AREA_PAINT, new Color(210, 210, 210))
//                  .put(ColorCodes.AREA_STROKE, new Color(200, 200, 200))
//                  .put(ColorCodes.NODE_PAINT, new Color(0, 0, 0, 90))
//
//                  .put(ColorCodes.NODE_HIGHLIGHTED_PAINT, new Color(55, 55, 255, 200))
//                  .put(ColorCodes.NODE_SELECTED_PAINT, new Color(55, 55, 255, 200))
//                  .put(ColorCodes.NODE_STROKE_PAINT, new Color(0, 0, 0, 200))
//                  .put(ColorCodes.NODE_SELECTED_STROKE_PAINT, new Color(0, 0, 255, 255))
//                  .put(ColorCodes.NODE_CLUSTER_ORIG_NODE_PAINT, new Color(155, 155, 155, 100))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_PAINT, new Color(255, 255, 0, 200))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_INCOMING_PAINT, new Color(0, 255, 255, 200))
//                  .put(ColorCodes.EDGE_STROKE_HIGHLIGHTED_OUTGOING_PAINT, new Color(255, 0, 255, 200))
//
//                  .put(ColorCodes.EDGE_SELF_LOOP_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_SELF_LOOP_MAX_WEIGHT, new Color(255, 255, 0))
//
//                  .put(ColorCodes.EDGE_NO_GRADIENT_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_NO_GRADIENT_MAX_WEIGHT, new Color(0, 0, 0))
//
//                  .put(ColorCodes.EDGE_GRADIENT_START_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_GRADIENT_START_MAX_WEIGHT, new Color(188, 35, 39))
//                  .put(ColorCodes.EDGE_GRADIENT_END_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_GRADIENT_END_MAX_WEIGHT, new Color(48, 156, 1))
//
//                  .put(ColorCodes.EDGE_START_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_START_MARKER_MAX_WEIGHT, new Color(188, 35, 39))
//                  .put(ColorCodes.EDGE_END_MARKER_MIN_WEIGHT, new Color(223, 223, 223))
//                  .put(ColorCodes.EDGE_END_MARKER_MAX_WEIGHT, new Color(48, 156, 1))
//                  .build());


        canvas = new PCanvas();
        canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));
//        canvas.addInputEventListener(new ZoomHandler(.5, 50));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);
//        addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                visualFlowMap.fitInCameraView();
//            }
//        });

        if (datasetSpecs != null) {
            loadFlowMap(datasetSpecs.get(0));
            canvas.getLayer().addChild(visualFlowMap);
        }

        if (showControlPanel) {
            controlPanel = new ControlPanel(this, datasetSpecs);
            add(controlPanel.getPanel(), BorderLayout.SOUTH);
        } else {
            controlPanel = null;
        }

//        fitFlowMapInView();
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public Color getColor(ColorCodes code) {
        return colorScheme.get(code);
    }

    public PCanvas getCanvas() {
        return canvas;
    }

    public Frame getApp() {
        return app;
    }

    public VisualFlowMap getVisualFlowMap() {
		return visualFlowMap;
	}

    public void setVisualFlowMap(VisualFlowMap newFlowMap) {
        if (visualFlowMap != null) {
            canvas.getLayer().removeChild(visualFlowMap);
            visualFlowMap.removeChildrenFromCamera();
        }
        canvas.getLayer().addChild(newFlowMap);
        visualFlowMap = newFlowMap;
        if (controlPanel != null) {
            controlPanel.loadVisualFlowMap(newFlowMap);
        }
    }

	public void fitFlowMapInView() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                visualFlowMap.fitInCameraView();
            }
        });
	}

	public void loadFlowMap(DatasetSpec dataset) {
	    loadFlowMap(dataset, null);
	}

    public void loadFlowMap(DatasetSpec dataset, FlowMapStats stats) {
        logger.info("> Loading flow map \"" + dataset + "\"");
        try {
            load(dataset, stats);
        } catch (Exception e) {
            logger.error("Couldn't load flow map " + dataset, e);
            JOptionPane.showMessageDialog(this,
                    "Couldn't load flow map: [" + e.getClass().getSimpleName()+ "] " + e.getMessage());
        }
    }

    private void load(DatasetSpec dataset, FlowMapStats stats) throws IOException, DataIOException {
        FlowMapGraphWithAttrSpecs graphAndSpecs =
            new FlowMapGraphWithAttrSpecs(loadGraph(dataset.getFilename()), dataset.getAttrsSpec());
        VisualFlowMap visualFlowMap = createVisualFlowMap(graphAndSpecs, stats);
        if (dataset.getAreaMapFilename() != null) {
            visualFlowMap.setAreaMap(new VisualAreaMap(visualFlowMap, AreaMap.load(dataset.getAreaMapFilename())));
        }
        setVisualFlowMap(visualFlowMap);
    }

    public VisualFlowMap createVisualFlowMap(FlowMapGraphWithAttrSpecs graphAndSpecs, FlowMapStats stats) {
        if (stats == null) {
            stats = FlowMapStats.createFor(graphAndSpecs);
        }
        FlowMapModel params = new FlowMapModel(graphAndSpecs, stats);
        logger.info("Edge weight stats: " + params.getStats().getEdgeWeightStats());
        double minWeight = graphAndSpecs.getAttrsSpec().getWeightFilterMin();
        if (!Double.isNaN(minWeight)) {
            params.setEdgeWeightFilterMin(minWeight);
        }
        return new VisualFlowMap(this, graphAndSpecs.getGraph(), params.getStats(), params);
    }

    public VisualFlowMap createVisualFlowMap(String edgeWeightAttr, String nodeLabelAttr,
            String xNodeAttr, String yNodeAttr, double weightFilterMin, Graph graph, FlowMapStats stats) {
        return createVisualFlowMap(
                new FlowMapGraphWithAttrSpecs(graph,
                new FlowMapAttrsSpec(edgeWeightAttr, nodeLabelAttr, xNodeAttr, yNodeAttr, weightFilterMin)),
                stats);
    }

    public static Graph loadGraph(String filename) throws DataIOException {
        logger.info("Loading \"" + filename + "\"");
        Graph graph = GraphFileFormats.createReaderFor(filename).readGraph(filename);
        logger.info("Graph loaded: " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");
        return graph;
    }

}
