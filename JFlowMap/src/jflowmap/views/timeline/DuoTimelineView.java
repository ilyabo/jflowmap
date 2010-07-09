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

package jflowmap.views.timeline;

import java.util.List;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraphSet;
import jflowmap.FlowTuple;
import jflowmap.util.piccolo.PCollapsableItemsContainer;
import jflowmap.views.VisualCanvas;


/**
 * @author Ilya Boyandin
 */
public class DuoTimelineView extends AbstractCanvasView {

  private static final double cellWidth = 35;
  private static final double cellHeight = 35;
  private static final double cellSpacingX = 0;
  private static final double cellSpacingY = 0;

  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();
  private final FlowMapGraphSet flowMapGraphs;

  public DuoTimelineView(FlowMapGraphSet flowMapGraphs) {
    this.flowMapGraphs = flowMapGraphs;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setBackground(style.getBackgroundColor());

    List<FlowTuple> tuples =
      flowMapGraphs.listFlowTuples(null, FlowTuple.nodeIdIsOneOf("AUT", "CHE"));


    PCollapsableItemsContainer container = new PCollapsableItemsContainer();

    for (FlowTuple tuple : tuples) {
//      container.addNewItem(tuple.getSrcNodeId() + "->" + tuple.getTargetNodeId(), head, null);

    }
  }

  @Override
  public String getName() {
    return "DuoTimeline";
  }

//  class VisualDuoTimeline extends PNode {
//
//    @Override
//    protected void layoutChildren() {
//      super.layoutChildren();
//    }
//  }
//

//  private PNode createRow(FlowTuple tuple) {
//    final PNode row = new PNode();
//
//    int graphIndex = 0;
//    for (Graph g : flowMapGraphs.asListOfGraphs()) {
//      Node node = FlowMapGraph.findNodeById(g, nodeId);
//      double x = cellSpacingX + graphIndex * (cellWidth + cellSpacingX);
//      double y = cellSpacingY + nodeIndex * (cellHeight + cellSpacingY);
//
////      row.addChild(new VisualTimelineNodeCell(this, node, x, y, cellWidth, cellHeight));
//
//
//
//      row.setBounds(row.getFullBoundsReference());
//      graphIndex++;
//    }
//    return row;
//  }

}
