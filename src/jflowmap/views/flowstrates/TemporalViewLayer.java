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

package jflowmap.views.flowstrates;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import jflowmap.FlowEndpoint;
import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public abstract class TemporalViewLayer extends PLayer implements ViewLayer {

  static final NumberFormat TOOLTIP_NUMBER_FORMAT = DecimalFormat.getNumberInstance();

  private final FlowstratesView flowstratesView;
  private final PCamera camera;

  public TemporalViewLayer(FlowstratesView flowstratesView) {
    this.flowstratesView = flowstratesView;
    camera = new PCamera();
    camera.addLayer(this);
  }

  public FlowstratesView getFlowstratesView() {
    return flowstratesView;
  }

  public FlowMapGraph getFlowMapGraph() {
    return getFlowstratesView().getFlowMapGraph();
  }

  public PCamera getCamera() {
    return camera;
  }

  public abstract void renew();

  public abstract void updateColors();

  public abstract void resetWeightAttrTotals();

  public abstract Dimension2D getEdgeLabelBounds(Edge edge, FlowEndpoint ep);

  public abstract Point2D getFlowLineInPoint(int row, FlowEndpoint ep);

  public PBounds getActualViewBounds() {
    return getCamera().getViewBounds();
  }


}
