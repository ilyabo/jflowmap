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

package jflowmap.visuals.timeline;

import java.util.List;

import jflowmap.FlowMapAttrsSpec;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowTimeline extends PNode {

    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpecs;

    private final double cellWidth = 50;
    private final double cellHeight = 25;
    private final double cellSpacingX = 2;
    private final double cellSpacingY = 2;

    public VisualFlowTimeline(Iterable<Graph> graphs, FlowMapAttrsSpec attrSpecs) {
        this.graphs = Lists.newArrayList(graphs);
        this.attrSpecs = attrSpecs;
        buildTimeline();
    }

    public FlowMapAttrsSpec getAttrSpecs() {
        return attrSpecs;
    }

    private void buildTimeline() {
        if (graphs.size() == 0) {
            return;
        }

        // the graphs are supposed to share the same nodes: TODO: build a joint nodeset
//        Graph firstOne = graphs.get(0);
//        final int numNodes = firstOne.getNodeCount();
//        for (int i = 0; i < numNodes; i++) {
//            Node node = firstOne.getNode(i);
//            addChild(new VisualTimelineNodeCell(node));
//        }

        int i = 0;
        for (Graph g : graphs) {
            for (int j = 0, numNodes = g.getNodeCount(); j < numNodes; j++) {
                Node n = g.getNode(j);
                addChild(new VisualTimelineNodeCell(
                        this, n,
                        cellSpacingX + i * (cellWidth + cellSpacingX),
                        cellSpacingY + j * (cellHeight + cellSpacingY),
                        cellWidth, cellHeight));
            }
            i++;
        }
    }

}
