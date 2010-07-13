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

import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapGraph;
import jflowmap.FlowMapGraphSet;
import jflowmap.FlowTuple;
import jflowmap.data.MinMax;
import jflowmap.views.VisualCanvas;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.util.ColorLib;
import prefuse.util.ColorMap;

import com.google.common.base.Predicate;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineView extends AbstractCanvasView {

  public static Logger logger = Logger.getLogger(DuoTimelineView.class);

  private static final double cellWidth = 35;
  private static final double cellHeight = 35;
  private static final double cellSpacingX = 0;
  private static final double cellSpacingY = 0;

  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();
  private final FlowMapGraphSet flowMapGraphs;
  private final PScrollPane scrollPane;

  public DuoTimelineView(FlowMapGraphSet flowMapGraphs) {
    this.flowMapGraphs = flowMapGraphs;

    VisualCanvas canvas = getVisualCanvas();
    canvas.setBackground(style.getBackgroundColor());

    canvas.setPanEventHandler(null);

    scrollPane = new PScrollPane(canvas);
    addTupleRows();
  }

  @Override
  public JComponent getViewComponent() {
    return scrollPane;
  }

  @Override
  public String getName() {
    return "DuoTimeline";
  }

  private void addTupleRows() {
    List<FlowTuple> tuples = flowMapGraphs.listFlowTuples(new Predicate<Edge>() {
      @Override
      public boolean apply(Edge e) {
//        String nodeId = FlowMapGraph.getNodeId(e.getTargetNode());
//        return (nodeId.equals("CHE") || nodeId.equals("AUT"));

//        String nodeId = FlowMapGraph.getNodeId(e.getSourceNode());
//        return (nodeId.equals("IRQ") || nodeId.equals("AFG"));

        return true;
      }
    });

    if (logger.isDebugEnabled()) {
      logger.debug("Found tuples: " + tuples.size());
    }
//    for (FlowTuple tuple : tuples) {
//      System.out.println(tuple);
//    }

    ColorMap cm = new ColorMap(new int[] {
        // ColorBrewer OrRd
//        0xFFF7EC, 0xFEE8C8, 0xFDD49E, 0xFDBB84, 0xFC8D59, 0xEF6548, 0xD7301F, 0xB30000, 0x7F0000
        ColorLib.rgb(255, 247, 236), ColorLib.rgb(254, 232, 200),
        ColorLib.rgb(253, 212, 158), ColorLib.rgb(253, 187, 132),
        ColorLib.rgb(252, 141, 89), ColorLib.rgb(239, 101, 72),
        ColorLib.rgb(215, 48, 31), ColorLib.rgb(179, 0, 0),
        ColorLib.rgb(127, 0, 0)
     }, 0, 1);
    List<FlowMapGraph> fmgs = flowMapGraphs.asList();
    PLayer layer = getVisualCanvas().getLayer();
    int row = 0, maxCol = 0;
    for (FlowTuple tuple : tuples) {
      MinMax wstats = flowMapGraphs.getStats().getEdgeWeightStats();
      int col = 0;

      double y = row * cellHeight;

      PText srcLabel = new PText(tuple.getSrcNodeId());
      srcLabel.setX(-srcLabel.getFullBoundsReference().getWidth() - 3);
      srcLabel.setY(y + (cellHeight - srcLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(srcLabel);

      for (FlowMapGraph fmg : fmgs) {
        Edge edge = tuple.getElementFor(fmg.getGraph());

        double x = col * cellWidth;

        if (edge == null) {
          layer.addChild(createEmptyCellNode(x, y));
        } else {
          double nw = wstats.normalize(fmg.getEdgeWeight(edge));

//          row.addChild(new HalfMoonsNode(cellWidth, cellHeight, wstats.normalize(weight), rightNormalizedValue));
//          PPath circle = new PPath(new Ellipse2D.Double(x, y, cellWidth * nw, cellHeight * nw), null);


//          double wh = Math.min(cellWidth, cellHeight);
//          double r = Math.sqrt(Math.abs(nw)) * wh;
//          PPath circle = new PPath(new Ellipse2D.Double(x + (cellWidth - r)/2, y + (cellHeight - r)/2, r, r), null);
//          circle.setPaint(style.getFlowCircleColor());

          PPath rect = new PPath(new Rectangle2D.Double(x, y, cellWidth, cellHeight), null);
          rect.setPaint(ColorLib.getColor(cm.getColor(nw)));

          layer.addChild(rect);
        }
        col++;
        if (col > maxCol) maxCol = col;
      }

      PText targetLabel = new PText(tuple.getTargetNodeId());
      targetLabel.setX(cellWidth * maxCol + 3);
      targetLabel.setY(y + (cellHeight - targetLabel.getFullBoundsReference().getHeight())/ 2);
      layer.addChild(targetLabel);

      row++;
    }

    layer.addChild(new PPath(new Rectangle2D.Double(0, 0, cellWidth * maxCol, cellHeight * row)));
  }

  private PNode createEmptyCellNode(double x, double y) {
    PNode emptyNode = new PNode();
    emptyNode.setBounds(x, y, cellWidth, cellHeight);
    return emptyNode;
  }

  @Override
  public void fitInView() {
    // do nothing
  }

}
