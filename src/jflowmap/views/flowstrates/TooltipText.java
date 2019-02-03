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

import jflowmap.FlowMapGraph;
import prefuse.data.Edge;
import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
public class TooltipText {

    String header, labels, values;

    public TooltipText(FlowMapGraph fmg, Edge edge, String weightAttr) {
      this.header = header(fmg, edge, weightAttr);
      this.labels = labels(fmg, edge, weightAttr);
      this.values = values(fmg, edge, weightAttr);
    }

    public String getHeader() {
      return header;
    }

    public String getLabels() {
      return labels;
    }

    public String getValues() {
      return values;
    }

    private String header(FlowMapGraph fmg, Edge edge, String weightAttr) {
      Node src = edge.getSourceNode();
      Node target = edge.getTargetNode();
      String nodeLabelAttr = fmg.getNodeLabelAttr();

      String origin = src.getString(nodeLabelAttr);
      String dest = target.getString(nodeLabelAttr);

      if (origin.length() > 75) origin = origin.substring(0, 75) + "...";
      if (dest.length() > 75) dest = dest.substring(0, 75) + "...";

      return
        //FlowMapGraph.getGraphId(edge.getGraph()) + ": " +
          origin + " -> " + dest;
    }

    private String labels(FlowMapGraph fmg, Edge edge, String weightAttr) {
      return
        weightAttr + ":" + "\n" +
        fmg.getAttrSpec().getFlowWeightDiffAttr(weightAttr) + ":"; // + "\n" +
        //flowMapGraph.getAttrSpec().getFlowWeightRelativeDiffAttr(weightAttr) + ":";
    }

    private String values(FlowMapGraph fmg, Edge edge, String weightAttr) {
      double value = edge.getDouble(weightAttr);
      double diff = edge.getDouble(fmg.getAttrSpec().getFlowWeightDiffAttr(weightAttr));
//      double weightRelDiff = edge.getDouble(
//          flowMapGraph.getAttrSpec().getFlowWeightRelativeDiffAttr(weightAttr));
      return
        fmt(value) + "\n" +
        fmt(diff) /* + "\n" +
        fmt(weightRelDiff)*/;
    }

    private String fmt(double weight) {
      if (Double.isNaN(weight)) {
        return "n/a";
      } else {
        return TemporalViewLayer.TOOLTIP_NUMBER_FORMAT.format(weight);
      }
    }
  }