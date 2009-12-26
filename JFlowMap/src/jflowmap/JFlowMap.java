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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jflowmap.data.GraphFileFormats;
import jflowmap.models.FlowMapModel;
import jflowmap.models.map.AreaMap;
import jflowmap.ui.ControlPanel;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.VisualAreaMap;
import jflowmap.visuals.VisualFlowMap;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
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

    private final List<DatasetSpec> datasetSpecs;

    public JFlowMap(FlowMapMain app, List<DatasetSpec> datasetSpecs) {
        setLayout(new BorderLayout());

        this.app = app;

        canvas = new PCanvas();
        canvas.setBackground(Color.BLACK);
//        canvas.setBackground(Color.WHITE);
//        canvas.addInputEventListener(new ZoomHandler(.5, 50));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                visualFlowMap.fitInCameraView();
            }
        });

        this.datasetSpecs = datasetSpecs;
        visualFlowMap = loadFlowMap(datasetSpecs.get(0));
        canvas.getLayer().addChild(visualFlowMap);

        controlPanel = new ControlPanel(this);
        add(controlPanel.getPanel(), BorderLayout.SOUTH);

        fitFlowMapInView();
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
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
        }
        canvas.getLayer().addChild(newFlowMap);
        visualFlowMap = newFlowMap;
    }

	public void fitFlowMapInView() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                visualFlowMap.fitInCameraView();
            }
        });
	}

    public VisualFlowMap loadFlowMap(DatasetSpec dataset) {
        logger.info("> Loading flow map \"" + dataset + "\"");
        try {
            VisualAreaMap areaMap = null;
            if (dataset.areaMapFilename != null) {
                areaMap = loadAreaMap(dataset.areaMapFilename);
            }
            return createVisualFlowMap(dataset.getAttrsSpec(), loadGraph(dataset.filename), areaMap);
        } catch (DataIOException e) {
            logger.error("Couldn't load flow map " + dataset, e);
            JOptionPane.showMessageDialog(this,  "Couldn't load flow map: [" + e.getClass().getSimpleName()+ "] " + e.getMessage());
        }
        return null;
    }

    public VisualFlowMap createVisualFlowMap(FlowMapAttrsSpec attrs, Graph graph, VisualAreaMap areaMap) {
        return createVisualFlowMap(attrs.getWeightAttrName(), attrs.getLabelAttrName(),
                attrs.getXNodeAttr(), attrs.getYNodeAttr(), attrs.getWeightFilterMin(), graph, areaMap);
    }

    public VisualFlowMap createVisualFlowMap(String weightAttrName, String nodeLabelAttrName,
            String xNodeAttr, String yNodeAttr, double weightFilterMin, Graph graph, VisualAreaMap areaMap) {
        FlowMapModel params = new FlowMapModel(graph, weightAttrName, xNodeAttr, yNodeAttr, nodeLabelAttrName);
        if (!Double.isNaN(weightFilterMin)) {
            params.setEdgeWeightFilterMin(weightFilterMin);
        }
        VisualFlowMap visualFlowMap = new VisualFlowMap(this, graph, params.getStats(), params);
        if (areaMap != null) {
            visualFlowMap.setAreaMap(areaMap);
        }
        return visualFlowMap;
    }

    private Graph loadGraph(String filename) throws DataIOException {
        logger.info("Loading graph \"" + filename + "\"");
        Graph graph = GraphFileFormats.createReaderFor(filename).readGraph(filename);
        logger.info("Loaded graph \"" + filename + "\": " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");
        return graph;
    }

    private VisualAreaMap loadAreaMap(String areaMapFilename) {
        try {
            return new VisualAreaMap(AreaMap.load(areaMapFilename));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Couldn't load area map " + areaMapFilename + ":\n" + e.getMessage()
            );
            logger.error("Couldn't load area map " + areaMapFilename, e);
        }
        return null;
    }

    public List<DatasetSpec> getDatasetSpecs() {
        return datasetSpecs;
    }

}
