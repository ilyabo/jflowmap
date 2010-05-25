/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import jflowmap.data.FlowMapSummaries;
import jflowmap.util.piccolo.PanHandler;
import jflowmap.util.piccolo.ZoomHandler;
import jflowmap.visuals.ColorScheme;
import jflowmap.visuals.timeline.VisualTimeline;

import org.apache.log4j.Logger;

import prefuse.data.Graph;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Ilya Boyandin
 */
public class JFlowTimeline extends JComponent {
//  public static final String NODE_COLUMN__REGION = "region";
//  public static final String NODE_COLUMN__REGION_COLOR = "regionColor";

  public static Logger logger = Logger.getLogger(JFlowTimeline.class);

  public static final Color CANVAS_BACKGROUND_COLOR =
    Color.white;
//    new Color(47, 89, 134);
  private final PCanvas canvas;
  private final VisualTimeline visualTimeline;
  private final ColorScheme colorScheme;


//  public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
//    this(graphs, attrSpec, null);
//  }

  public JFlowTimeline(FlowMapGraphSet fmset, String nodeAttrToGroupBy) {
    setLayout(new BorderLayout());

    this.colorScheme = ColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme();

    canvas = new PCanvas();
    canvas.setBackground(CANVAS_BACKGROUND_COLOR);
//    canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));
    canvas.addInputEventListener(new ZoomHandler());
    canvas.setPanEventHandler(new PanHandler());
    add(canvas, BorderLayout.CENTER);


    if (nodeAttrToGroupBy != null) {
      visualTimeline = new VisualTimeline(this, fmset, createGraphsWithGroupedNodes(fmset, nodeAttrToGroupBy), nodeAttrToGroupBy);
    } else {
      visualTimeline = new VisualTimeline(this, fmset, null, null);
    }

    canvas.getLayer().addChild(visualTimeline);
  }

  private FlowMapGraphSet createGraphsWithGroupedNodes(FlowMapGraphSet fmset, String nodeAttrToGroupBy) {
    FlowMapAttrSpec attrSpec = fmset.getAttrSpec();
    List<Graph> groupedGraphs = Lists.newArrayList();
    for (FlowMapGraph fmg : fmset.asList()) {

      FlowMapGraph grouped = fmg.groupNodesBy(nodeAttrToGroupBy);

      // TODO: supplyNodesWithIntraregSummaries shouldn't be here
      FlowMapSummaries.supplyNodesWithIntraregSummaries(fmg, nodeAttrToGroupBy);
      FlowMapSummaries.supplyNodesWithIntraregSummaries(grouped, attrSpec.getNodeLabelAttr());

      groupedGraphs.add(grouped.getGraph());
    }
    return new FlowMapGraphSet(groupedGraphs, attrSpec);
  }

  public ColorScheme getColorScheme() {
    return colorScheme;
  }

//  private <T> Map<T, Integer> createColorMapForValues(Set<T> valuesToGroupBy) {
//    int[] palette = ColorLib.getCategoryPalette(valuesToGroupBy.size(), 1.f, 0.4f, 1.f, .15f);
//
//    int colorIdx = 0;
//    Map<T, Integer> valueToColor = Maps.newHashMap();
//    for (T v : valuesToGroupBy) {
//      valueToColor.put(v, palette[colorIdx]);
//      colorIdx++;
//    }
//
//    return valueToColor;
//  }

  public PCanvas getCanvas() {
    return canvas;
  }

  public PCamera getCamera() {
    return canvas.getCamera();
  }


}
