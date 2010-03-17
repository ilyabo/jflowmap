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
import jflowmap.JFlowTimeline;
import jflowmap.data.FlowMapLoader;
import jflowmap.data.FlowMapStats;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
public class VisualFlowTimeline extends PNode {

    enum Orientation { HORIZONTAL, VERTICAL };
    private static final Orientation ORIENTATION = Orientation.VERTICAL;

    private static final int ROW_CAPTION_TO_CELLS_X_GAP = 85;
    private static final int ROW_CAPTION_TO_CELLS_Y_GAP = 5;
    private static final Font ROW_CAPTION_FONT = new Font("Arial", Font.BOLD, 15);
    private final List<Graph> graphs;
    private final FlowMapAttrsSpec attrSpecs;

    private final double cellWidth = 60;
    private final double cellHeight = 35;
    private final double cellSpacingX = 2;
    private final double cellSpacingY = 2;
    private final JFlowTimeline jFlowTimeline;

    public VisualFlowTimeline(JFlowTimeline jFlowTimeline, Iterable<Graph> graphs, FlowMapAttrsSpec attrSpecs) {
        this.jFlowTimeline = jFlowTimeline;
        this.graphs = Lists.newArrayList(graphs);
        this.attrSpecs = attrSpecs;
        buildTimeline();
    }

    public FlowMapStats getGlobalStats() {
        return jFlowTimeline.getGlobalStats();
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

        Table selectedGraph = graphs.get(graphs.size() - 1).getNodeTable();

        int i = 0;
        for (Graph g : graphs) {
            PText t = new PText(FlowMapLoader.getGraphId(g));
            t.setFont(ROW_CAPTION_FONT);
            t.setTextPaint(Color.white);
            double x = 0, y = 0;
            switch (ORIENTATION) {
                case HORIZONTAL:
                    x = ROW_CAPTION_TO_CELLS_X_GAP + t.getX() - t.getWidth();
                    y = cellSpacingY + i * (cellHeight + cellSpacingY) + (cellHeight - ROW_CAPTION_FONT.getSize2D())/2;
                    t.setJustification(JComponent.RIGHT_ALIGNMENT);
                    t.setBounds(x , y, t.getWidth(), t.getHeight());
                    break;
                case VERTICAL:
                    x = cellSpacingX + i * (cellWidth + cellSpacingX);
                    y = 0 ;
                    t.setJustification(JComponent.LEFT_ALIGNMENT);
                    t.setBounds(x + (cellWidth - t.getWidth())/2, y, t.getWidth(), t.getHeight());
                break;
            }
            addChild(t);

//            int j = 0;

            int j = g.getNodeCount() - 1;  // workaround for the bug in rowsSortedBy
            @SuppressWarnings("unchecked")
//            Iterator<Integer> it = g.getNodeTable().rowsSortedBy(
            Iterator<Integer> it = selectedGraph.rowsSortedBy(
                    FlowMapStats.NODE_STATS_COLUMN__SUM_OUTGOING, true
////                    attrSpecs.nodeLabelAttr, true
            );
            while (it.hasNext()) {
                Node n = g.getNode(it.next());
                double nx = 0, ny = 0;
                switch (ORIENTATION) {
                    case HORIZONTAL:
                        nx = 10 + ROW_CAPTION_TO_CELLS_X_GAP + cellSpacingX + j * (cellWidth + cellSpacingX);
                        ny = cellSpacingY + i * (cellHeight + cellSpacingY);
                        break;
                    case VERTICAL:
                        nx = cellSpacingX + i * (cellWidth + cellSpacingX);
                        ny = ROW_CAPTION_FONT.getSize2D() + ROW_CAPTION_TO_CELLS_Y_GAP + cellSpacingY + j * (cellHeight + cellSpacingY);
                        break;
                }

//                if (ORIENTATION == Orientation.VERTICAL) {
//                    double _n = nx; nx = ny; ny = _n;   // swap
//                }

                addChild(new VisualTimelineNodeCell(this, n, nx, ny, cellWidth, cellHeight));
//                j++;
                j--;
            }
            i++;
        }
    }

}

