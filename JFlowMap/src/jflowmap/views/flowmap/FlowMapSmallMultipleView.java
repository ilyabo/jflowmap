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

package jflowmap.views.flowmap;

import jflowmap.AbstractCanvasView;
import jflowmap.geo.MapProjections;
import jflowmap.models.map.GeoMap;
import jflowmap.views.IFlowMapColorScheme;

/**
 * @author Ilya Boyandin
 */
public class FlowMapSmallMultipleView extends AbstractCanvasView {

  public FlowMapSmallMultipleView(VisualFlowMapModel model, GeoMap areaMap, MapProjections proj,
      IFlowMapColorScheme colorScheme) {
  }

  @Override
  public String getControlsLayoutConstraint() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

}
