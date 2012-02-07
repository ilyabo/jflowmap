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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import prefuse.data.Graph;
import prefuse.data.Table;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class FlowMapAttrSpec {

  private static final String FlOW_WEIGHT_DIFF_COLUMNS_SUFFIX = ":diff";
  private static final String FLOW_WEIGHT_REL_DIFF_COLUMNS_SUFFIX = ":rdiff";

  private final String flowSrcNodeAttr;
  private final String flowTargetNodeAttr;
  private final String legendCaption;
  private final List<String> flowWeightAttrs;
  private final String nodeIdAttr;
  private final String nodeLabelAttr;
  private final String nodeLonAttr;
  private final String nodeLatAttr;
  private final List<String> requiredNodeAttrs;
  private final List<String> requiredFlowAttrs;

//  public FlowMapAttrSpec(Iterable<String> flowWeightAttrs,
//      String nodeLabelAttr, String nodeLonAttr, String nodeLatAttr) {
//    this(null, null, flowWeightAttrs, null, nodeLabelAttr, nodeLonAttr, nodeLatAttr);
//  }

  public FlowMapAttrSpec(
      String flowSrcNodeAttr,
      String flowTargetNodeAttr,
      String legendCaption,
      Iterable<String> flowWeightAttrs,
      String nodeIdAttr, String nodeLabelAttr,
      String nodeLonAttr, String nodeLatAttr) {

    Preconditions.checkNotNull(nodeLabelAttr);

    this.nodeIdAttr = (nodeIdAttr != null ? nodeIdAttr : FlowMapGraph.GRAPH_NODE_ID_COLUMN);
    this.nodeLabelAttr = nodeLabelAttr;
    this.nodeLonAttr = nodeLonAttr;
    this.nodeLatAttr = nodeLatAttr;

    this.legendCaption = legendCaption;
    this.flowSrcNodeAttr =
      (flowSrcNodeAttr != null ? flowSrcNodeAttr : FlowMapGraph.GRAPH_EDGE_SOURCE_NODE_COLUMN);
    this.flowTargetNodeAttr =
      (flowTargetNodeAttr != null ? flowTargetNodeAttr : FlowMapGraph.GRAPH_EDGE_TARGET_NODE_COLUMN);
    this.flowWeightAttrs = ImmutableList.copyOf(flowWeightAttrs);

    this.requiredNodeAttrs = Arrays.asList(nodeIdAttr, nodeLabelAttr, nodeLonAttr, nodeLatAttr);
    this.requiredFlowAttrs = flowAttrs();

  }

  private ArrayList<String> flowAttrs() {
    ArrayList<String> attrs = Lists.newArrayList(flowSrcNodeAttr, flowTargetNodeAttr);
    Iterables.addAll(attrs, flowWeightAttrs);
    return attrs;
  }

  public boolean isRequiredNodeAttr(String attrName) {
    return requiredNodeAttrs.contains(attrName);
  }

  public boolean isRequiredFlowAttr(String attrName) {
    return requiredFlowAttrs.contains(attrName);
  }

  public boolean isFlowWeightAttr(String attrName) {
    return flowWeightAttrs.contains(attrName);
  }

  public boolean hasNodePositions() {
    return (nodeLonAttr != null  &&  nodeLatAttr != null);
  }

  public String getLegendCaption() {
    return legendCaption;
  }

  public String getNodeIdAttr() {
    return nodeIdAttr;
  }

  public String getFlowSrcNodeAttr() {
    return flowSrcNodeAttr;
  }

  public String getFlowTargetNodeAttr() {
    return flowTargetNodeAttr;
  }

  /**
   * @return An immutable list which can thus be reused without defensive copying.
   */
  public List<String> getFlowWeightAttrs() {
    return flowWeightAttrs;
  }

  public String getFlowWeightDiffAttr(String weightAttr) {
    return weightAttr + FlOW_WEIGHT_DIFF_COLUMNS_SUFFIX;
  }

  public String getFlowWeightRelativeDiffAttr(String weightAttr) {
    return weightAttr + FLOW_WEIGHT_REL_DIFF_COLUMNS_SUFFIX;
  }

  public List<String> getFlowWeightDiffAttrs() {
    return ImmutableList.copyOf(Iterables.transform(flowWeightAttrs,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getFlowWeightDiffAttr(weightAttr);
          }
        }));
  }

  public List<String> getFlowWeightRelativeDiffAttrs() {
    return ImmutableList.copyOf(Iterables.transform(flowWeightAttrs,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getFlowWeightRelativeDiffAttr(weightAttr);
          }
        }));
  }


  public String getNodeLabelAttr() {
    return nodeLabelAttr;
  }

  public String getNodeLonAttr() {
    return nodeLonAttr;
  }

  public String getNodeLatAttr() {
    return nodeLatAttr;
  }

  public void checkValidityFor(Graph graph) {
    if (hasNodePositions()) {
      validateAttr(graph, graph.getNodeTable(), nodeLonAttr, double.class);
      validateAttr(graph, graph.getNodeTable(), nodeLatAttr, double.class);
    }
    validateAttr(graph, graph.getNodeTable(), nodeLabelAttr, String.class);
    for (String attr : flowWeightAttrs) {
      validateAttr(graph, graph.getEdgeTable(), attr, double.class);
    }
  }

  private void validateAttr(Graph graph, Table table, String attr, Class<?> type) {
    if (!table.canGet(attr, type)) {
      throw new IllegalArgumentException("Can't get graph's attr:'" + attr + "', graph id:'" +
          FlowMapGraph.getGraphId(graph) + "'");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((flowWeightAttrs == null) ? 0 : flowWeightAttrs.hashCode());
    result = prime * result + ((nodeIdAttr == null) ? 0 : nodeIdAttr.hashCode());
    result = prime * result + ((nodeLabelAttr == null) ? 0 : nodeLabelAttr.hashCode());
    result = prime * result + ((nodeLatAttr == null) ? 0 : nodeLatAttr.hashCode());
    result = prime * result + ((nodeLonAttr == null) ? 0 : nodeLonAttr.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FlowMapAttrSpec other = (FlowMapAttrSpec) obj;
    if (flowWeightAttrs == null) {
      if (other.flowWeightAttrs != null)
        return false;
    } else if (!flowWeightAttrs.equals(other.flowWeightAttrs))
      return false;
    if (nodeIdAttr == null) {
      if (other.nodeIdAttr != null)
        return false;
    } else if (!nodeIdAttr.equals(other.nodeIdAttr))
      return false;
    if (nodeLabelAttr == null) {
      if (other.nodeLabelAttr != null)
        return false;
    } else if (!nodeLabelAttr.equals(other.nodeLabelAttr))
      return false;
    if (nodeLatAttr == null) {
      if (other.nodeLatAttr != null)
        return false;
    } else if (!nodeLatAttr.equals(other.nodeLatAttr))
      return false;
    if (nodeLonAttr == null) {
      if (other.nodeLonAttr != null)
        return false;
    } else if (!nodeLonAttr.equals(other.nodeLonAttr))
      return false;
    return true;
  }

}
