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

import javax.swing.JComponent;

import jflowmap.data.FlowMapStats;
import jflowmap.util.PanHandler;
import jflowmap.util.ZoomHandler;
import jflowmap.visuals.timeline.VisualFlowTimeline;
import prefuse.data.Graph;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 */
public class JFlowTimeline extends JComponent {

    private static final Color CANVAS_BACKGROUND_COLOR = new Color(47, 89, 134);
    private final PCanvas canvas;
    private final VisualFlowTimeline visualTimeline;
    private final FlowMapStats globalStats;

    public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpec) {
        setLayout(new BorderLayout());

        canvas = new PCanvas();
        canvas.setBackground(CANVAS_BACKGROUND_COLOR);
//        canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));
        canvas.addInputEventListener(new ZoomHandler());
        canvas.setPanEventHandler(new PanHandler());
        add(canvas, BorderLayout.CENTER);



        List<FlowMapGraphWithAttrSpecs> graphsAndSpecs = Lists.newArrayList();
        for (Graph graph : graphs) {
            FlowMapGraphWithAttrSpecs gs = new FlowMapGraphWithAttrSpecs(graph, attrSpec);
            FlowMapStats.supplyNodesWithStats(gs);
            graphsAndSpecs.add(gs);
        }

        FlowMapStats.supplyNodesWithDiffStats(graphs, attrSpec);

        globalStats = FlowMapStats.createFor(graphsAndSpecs);

        visualTimeline = new VisualFlowTimeline(this, graphs, attrSpec);
        canvas.getLayer().addChild(visualTimeline);

    }

    public FlowMapStats getGlobalStats() {
        return globalStats;
    }

}
