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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import jflowmap.data.FlowMapSummaries;
import jflowmap.geom.Point;
import jflowmap.models.FlowMapGraphBuilder;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.timeline.VisualTimeline;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 */
public class JFlowTimeline extends JComponent {
//    public static final String NODE_COLUMN__REGION = "region";
//    public static final String NODE_COLUMN__REGION_COLOR = "regionColor";

    public static Logger logger = Logger.getLogger(JFlowTimeline.class);

    public static final Color CANVAS_BACKGROUND_COLOR =
        Color.white;
//        new Color(47, 89, 134);
    private final PCanvas canvas;
    private final VisualTimeline visualTimeline;

    public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec) {
        this(graphs, attrSpec, null);
    }

    public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec, String columnToGroupNodesBy) {
        setLayout(new BorderLayout());

        canvas = new PCanvas();
        canvas.setBackground(CANVAS_BACKGROUND_COLOR);
//        canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);


        if (columnToGroupNodesBy != null) {
            List<Graph> groupedGraphs = createGraphsWithGroupedNodes(graphs, attrSpec, columnToGroupNodesBy);
            visualTimeline = new VisualTimeline(this, graphs, attrSpec, groupedGraphs, columnToGroupNodesBy);
        } else {
            visualTimeline = new VisualTimeline(this, graphs, attrSpec, null, null);
        }
        canvas.getLayer().addChild(visualTimeline);
    }


    private List<Graph> createGraphsWithGroupedNodes(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec,
            String columnToGroupNodesBy) {
        Set<Object> valuesToGroupBy = FlowMap.getNodeAttrValues(graphs, columnToGroupNodesBy);
//            Map<Object, Integer> valueToColor = createColorMapForValues(valuesToGroupBy);

        List<Graph> groupedGraphs = Lists.newArrayList();
        for (Graph g : graphs) {

            FlowMapGraphBuilder builder = new FlowMapGraphBuilder(FlowMap.getGraphId(g))
//                    .withCumulativeEdges()            // TODO: why isn't it working?
                .withNodeXAttr(attrSpec.getXNodeAttr())
                .withNodeYAttr(attrSpec.getYNodeAttr())
                .withEdgeWeightAttr(attrSpec.getEdgeWeightAttr())
                .withNodeLabelAttr(attrSpec.getNodeLabelAttr())
                ;

            Map<Object, Node> valueToNode = Maps.newHashMap();
            for (Object v : valuesToGroupBy) {
                String strv = v.toString();
                Node node = builder.addNode(strv, new Point(0, 0), strv);
                valueToNode.put(v, node);
            }

            for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
                Edge e = g.getEdge(i);
                Node src = e.getSourceNode();
                Node trg = e.getTargetNode();
                String srcV = src.getString(columnToGroupNodesBy);
                String trgV = trg.getString(columnToGroupNodesBy);
                if (srcV == null) {
                    throw new IllegalArgumentException("No " + columnToGroupNodesBy + " value for " + src);
                }
                if (trgV == null) {
                    throw new IllegalArgumentException("No " + columnToGroupNodesBy + " value for " + trg);
                }
                builder.addEdge(
                        valueToNode.get(srcV),
                        valueToNode.get(trgV),
                        e.getDouble(attrSpec.getEdgeWeightAttr()));
            }



            Graph grouped = builder.build();

            FlowMapSummaries.supplyNodesWithIntraregSummaries(
                    new FlowMapGraphWithAttrSpecs(g, attrSpec), columnToGroupNodesBy);
            FlowMapSummaries.supplyNodesWithIntraregSummaries(
                    new FlowMapGraphWithAttrSpecs(grouped, attrSpec), attrSpec.getNodeLabelAttr());

            groupedGraphs.add(grouped);
        }
        return groupedGraphs;
    }

    private <T> Map<T, Integer> createColorMapForValues(Set<T> valuesToGroupBy) {
        int[] palette = ColorLib.getCategoryPalette(valuesToGroupBy.size(), 1.f, 0.4f, 1.f, .15f);

        int colorIdx = 0;
        Map<T, Integer> valueToColor = Maps.newHashMap();
        for (T v : valuesToGroupBy) {
            valueToColor.put(v, palette[colorIdx]);
            colorIdx++;
        }

        return valueToColor;
    }

    public PCanvas getCanvas() {
        return canvas;
    }

    public PCamera getCamera() {
        return canvas.getCamera();
    }


}
