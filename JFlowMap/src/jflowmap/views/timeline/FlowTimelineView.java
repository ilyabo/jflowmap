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

package jflowmap.views.timeline;

import java.awt.Color;

import javax.swing.JComponent;

import jflowmap.AbstractCanvasView;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraphSet;
import jflowmap.data.FlowMapSummaries;
import jflowmap.views.IColorScheme;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PCamera;

/**
 * @author Ilya Boyandin
 */
public class FlowTimelineView extends AbstractCanvasView {
//  public static final String NODE_COLUMN__REGION = "region";
//  public static final String NODE_COLUMN__REGION_COLOR = "regionColor";

  public static Logger logger = Logger.getLogger(FlowTimelineView.class);

  public static final Color CANVAS_BACKGROUND_COLOR = Color.white;
//    new Color(47, 89, 134);
  private final VisualTimeline visualTimeline;
  private final IColorScheme colorScheme;


//  public FlowTimelineView(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
//    this(graphs, attrSpec, null);
//  }

  public FlowTimelineView(FlowMapGraphSet fmset, String nodeAttrToGroupBy) {
    this.colorScheme = FlowMapColorSchemes.LIGHT.getScheme();

    getVisualCanvas().setBackground(CANVAS_BACKGROUND_COLOR);
//    canvas.setBackground(colorScheme.get(ColorCodes.BACKGROUND));


    if (nodeAttrToGroupBy != null) {
      FlowMapGraphSet groupedFmset = fmset.groupNodesBy(nodeAttrToGroupBy);

      String edgeWeightAttr = fmset.getAttrSpec().getFlowWeightAttrs().get(0);
      FlowMapSummaries.supplyNodesWithIntraregSummaries(fmset, nodeAttrToGroupBy,
          edgeWeightAttr);
      FlowMapSummaries.supplyNodesWithIntraregSummaries(groupedFmset,
          fmset.getAttrSpec().getNodeLabelAttr(), edgeWeightAttr);

      visualTimeline = new VisualTimeline(this, fmset, groupedFmset, nodeAttrToGroupBy);
    } else {
      visualTimeline = new VisualTimeline(this, fmset, null, null);
    }

    getVisualCanvas().getLayer().addChild(visualTimeline);
  }


  @Override
  public String getName() {
    return "FlowTimeline";
  }

  public IColorScheme getColorScheme() {
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


  @Override
  public JComponent getControls() {
    return null;
  }




}
