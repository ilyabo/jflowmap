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

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import jflowmap.data.SeqStat;
import jflowmap.views.Legend;
import jflowmap.views.flowmap.AbstractLegendItemProducer;
import jflowmap.views.flowmap.FlowMapView;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * @author Ilya Boyandin
 */
public class FlowstratesLegend extends Legend {

  public FlowstratesLegend(final FlowstratesView flowstratesView) {
    super(
      flowstratesView.getFlowMapGraph().getAttrSpec().getLegendCaption(),
      new Color(220, 220, 220, 225),
      new Color(60, 60, 60, 200),

      new AbstractLegendItemProducer(4) {
        @Override
        public PNode createItem(double value) {
          PPath item = new PPath(new Rectangle2D.Double(0, 0, 10, 10));
          item.setPaint(flowstratesView.getColorFor(value));
          String name = FlowMapView.NUMBER_FORMAT.format(value);
          if (flowstratesView.getValueStat().getMin() < 0   &&   value > 0) {
            name = "+" + name;
          }
          item.setName(name);
          item.setStroke(null);
          return item;
        }

        @Override
        public PNode createHeader() {
          return null;
        }

        @Override
        public SeqStat getValueStat() {
          return flowstratesView.getValueStat();
        }

      });

    setOffset(5, 12);
    setScale(1.2);
  }

}
