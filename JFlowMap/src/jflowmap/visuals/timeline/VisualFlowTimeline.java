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

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import jflowmap.FlowMapAttrsSpec;
import jflowmap.data.FlowMapLoader;
import jflowmap.data.FlowMapStats;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowTimeline extends PNode {

    private static final int ROW_CAPTION_TO_CELLS_GAP = 15;
    private static final Font ROW_CAPTION_FONT = new Font("Arial", Font.BOLD, 15);
    private static final int X_OFFSET = 70;
    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpecs;

    private final double cellWidth = 60;
    private final double cellHeight = 35;
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
            double y = cellSpacingY + i * (cellHeight + cellSpacingY);
            PText t = new PText(FlowMapLoader.idOf(g));
            t.setFont(ROW_CAPTION_FONT);
            t.setTextPaint(Color.white);
            t.setJustification(JComponent.RIGHT_ALIGNMENT);
            t.setBounds(
                    X_OFFSET + t.getX() - t.getWidth() - ROW_CAPTION_TO_CELLS_GAP,
                    y + (cellHeight - ROW_CAPTION_FONT.getSize2D())/2,
                    t.getWidth(), t.getHeight());
            addChild(t);

//            int j = 0;
            int j = g.getNodeCount() - 1;  // workaround for the bug in rowsSortedBy
            Iterator<Integer> it = g.getNodeTable().rowsSortedBy(
//                    attrSpecs.nodeLabelAttr, true
                    FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING, true
            );
            while (it.hasNext()) {
                Node n = g.getNode(it.next());
                addChild(new VisualTimelineNodeCell(
                        this, n,
                        X_OFFSET + cellSpacingX + j * (cellWidth + cellSpacingX),
                        y,
                        cellWidth, cellHeight));
//                j++;
                j--;
            }
            i++;
        }
    }

}
