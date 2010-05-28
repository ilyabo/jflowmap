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

import java.awt.Color;

import jflowmap.data.FlowMapSummaries;
import jflowmap.visuals.ColorScheme;
import jflowmap.visuals.timeline.VisualTimeline;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PCamera;

/**
 * @author Ilya Boyandin
 */
public class JFlowTimeline extends JView {
//  public static final String NODE_COLUMN__REGION = "region";
//  public static final String NODE_COLUMN__REGION_COLOR = "regionColor";

  public static Logger logger = Logger.getLogger(JFlowTimeline.class);

  public static final Color CANVAS_BACKGROUND_COLOR = Color.white;
//    new Color(47, 89, 134);
  private final VisualTimeline visualTimeline;
  private final ColorScheme colorScheme;


//  public JFlowTimeline(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
//    this(graphs, attrSpec, null);
//  }

  public JFlowTimeline(FlowMapGraphSet fmset, String nodeAttrToGroupBy) {
    this.colorScheme = FlowMapColorSchemes.LIGHT_BLUE__COLOR_BREWER.getScheme();

    getVisualCanvas().setBackground(CANVAS_BACKGROUND_COLOR);
//    canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));


    if (nodeAttrToGroupBy != null) {
      FlowMapGraphSet groupedFmset = fmset.groupNodesBy(nodeAttrToGroupBy);

      FlowMapSummaries.supplyNodesWithIntraregSummaries(fmset, nodeAttrToGroupBy);
      FlowMapSummaries.supplyNodesWithIntraregSummaries(groupedFmset, fmset.getAttrSpec().getNodeLabelAttr());

      visualTimeline = new VisualTimeline(this, fmset, groupedFmset, nodeAttrToGroupBy);
    } else {
      visualTimeline = new VisualTimeline(this, fmset, null, null);
    }

    getVisualCanvas().getLayer().addChild(visualTimeline);

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

  public PCamera getCamera() {
    return getVisualCanvas().getCamera();
  }



}
