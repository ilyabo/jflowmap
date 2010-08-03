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

import java.util.Arrays;

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
    FlowMapView jFlowMap = new FlowMapView(Arrays.asList(getDatasetSpec()), true);

    String colorScheme = getParameter("colorScheme");
    if (colorScheme != null  &&  !colorScheme.isEmpty()) {
      jFlowMap.setColorScheme(FlowMapColorSchemes.findByName(colorScheme));
    }
    return jFlowMap;
  }

}
