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
import java.util.Arrays;

import javax.swing.JComponent;

import jflowmap.views.flowmap.FlowMapView;

/**
 * TODO: finish FlowMapApplet
 *
 * @author Ilya Boyandin
 */
public class FlowMapApplet extends BaseApplet {

  public FlowMapApplet() {
    super("FlowMapApplet");
  }

  @Override
  protected IView createView() {
    FlowMapView view = new FlowMapView(Arrays.asList(getDatasetSpec()), true);

    String minw = getParameter("weightFilterMin");
    if (minw != null) {
      view.getVisualFlowMap().getModel().setEdgeWeightFilterMin(Double.parseDouble(minw));
    }

    String colorScheme = getParameter("colorScheme");
    if (colorScheme != null  &&  !colorScheme.isEmpty()) {
      view.setColorScheme(FlowMapColorSchemes.findByName(colorScheme));
    }
    return view;
  }

  @Override
  protected void initControls() {
    JComponent controls = view.getControls();
    if (controls != null) {
        add(controls, BorderLayout.SOUTH);
    }
  }
}
