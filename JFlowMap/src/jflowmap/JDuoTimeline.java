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

import jflowmap.util.piccolo.PanHandler;
import jflowmap.util.piccolo.ZoomHandler;
import jflowmap.visuals.timeline.DefaultDuoTimelineStyle;
import jflowmap.visuals.timeline.DuoTimelineStyle;
import jflowmap.visuals.timeline.VisualDuoTimeline;
import edu.umd.cs.piccolo.PCanvas;


/**
 * @author Ilya Boyandin
 */
public class JDuoTimeline extends JView {

  private final VisualDuoTimeline visualCombTimeline;
  private final PCanvas canvas;
  private final DuoTimelineStyle style = new DefaultDuoTimelineStyle();

  public JDuoTimeline(FlowMapGraphSet flowMapGraphs) {
    visualCombTimeline = new VisualDuoTimeline(flowMapGraphs);
    canvas = new PCanvas();

    canvas.setBackground(style.getBackgroundColor());
    canvas.addInputEventListener(new ZoomHandler());
    canvas.setPanEventHandler(new PanHandler());
    add(canvas, BorderLayout.CENTER);
  }


  @Override
  public void fitInView() {
//    visualCombTimeline.fitInCameraView();
  }


}