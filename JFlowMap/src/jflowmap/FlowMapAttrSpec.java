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
import java.util.List;

import prefuse.data.Graph;
import prefuse.data.Table;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class FlowMapAttrSpec {

  private static final String EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX = ":diff";
  private static final String EDGE_WEIGHT_REL_DIFF_COLUMNS_SUFFIX = ":rdiff";

  private final List<String> edgeWeightAttrs;
  private final String nodeLabelAttr;
  private final String xNodeAttr, yNodeAttr;

  public FlowMapAttrSpec(List<String> edgeWeightAttrs, String nodeLabelAttr,
      String xNodeAttr, String yNodeAttr) {
    this.edgeWeightAttrs = ImmutableList.copyOf(edgeWeightAttrs);
    this.nodeLabelAttr = nodeLabelAttr;
    this.xNodeAttr = xNodeAttr;
    this.yNodeAttr = yNodeAttr;
  }

  public FlowMapAttrSpec(List<String> edgeWeightAttrs, String nodeLabelAttr) {
    this(edgeWeightAttrs, nodeLabelAttr, null, null);
  }

  public FlowMapAttrSpec(String edgeWeightAttr, String nodeLabelAttr,
      String xNodeAttr, String yNodeAttr) {
    this(Arrays.asList(edgeWeightAttr), nodeLabelAttr, xNodeAttr, yNodeAttr);
  }

  public FlowMapAttrSpec(String edgeWeightAttr, String nodeLabelAttr) {
    this(Arrays.asList(edgeWeightAttr), nodeLabelAttr, null, null);
  }

  public boolean hasNodePositions() {
    return (xNodeAttr != null  &&  yNodeAttr != null);
  }

  /**
   * @return An immutable list which can thus be reused without defensive copying.
   */
  public List<String> getEdgeWeightAttrs() {
    return edgeWeightAttrs;
  }

  public String getEdgeWeightDiffAttr(String weightAttr) {
    return weightAttr + EDGE_WEIGHT_DIFF_COLUMNS_SUFFIX;
  }

  public String getEdgeWeightRelativeDiffAttr(String weightAttr) {
    return weightAttr + EDGE_WEIGHT_REL_DIFF_COLUMNS_SUFFIX;
  }

  public List<String> getEdgeWeightDiffAttrs() {
    return ImmutableList.copyOf(Iterables.transform(edgeWeightAttrs,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getEdgeWeightDiffAttr(weightAttr);
          }
        }));
  }

  public List<String> getEdgeWeightRelativeDiffAttrs() {
    return ImmutableList.copyOf(Iterables.transform(edgeWeightAttrs,
        new Function<String, String>() {
          public String apply(String weightAttr) {
            return getEdgeWeightRelativeDiffAttr(weightAttr);
          }
        }));
  }


  public String getNodeLabelAttr() {
    return nodeLabelAttr;
  }

  public String getXNodeAttr() {
    return xNodeAttr;
  }

  public String getYNodeAttr() {
    return yNodeAttr;
  }

  public void checkValidityFor(Graph graph) {
    if (hasNodePositions()) {
      validateAttr(graph, graph.getNodeTable(), xNodeAttr, double.class);
      validateAttr(graph, graph.getNodeTable(), yNodeAttr, double.class);
    }
    validateAttr(graph, graph.getNodeTable(), nodeLabelAttr, String.class);
    for (String attr : edgeWeightAttrs) {
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
    result = prime * result + ((edgeWeightAttrs == null) ? 0 : edgeWeightAttrs.hashCode());
    result = prime * result + ((nodeLabelAttr == null) ? 0 : nodeLabelAttr.hashCode());
    result = prime * result + ((xNodeAttr == null) ? 0 : xNodeAttr.hashCode());
    result = prime * result + ((yNodeAttr == null) ? 0 : yNodeAttr.hashCode());
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
    if (edgeWeightAttrs == null) {
      if (other.edgeWeightAttrs != null)
        return false;
    } else if (!edgeWeightAttrs.equals(other.edgeWeightAttrs))
      return false;
    if (nodeLabelAttr == null) {
      if (other.nodeLabelAttr != null)
        return false;
    } else if (!nodeLabelAttr.equals(other.nodeLabelAttr))
      return false;
    if (xNodeAttr == null) {
      if (other.xNodeAttr != null)
        return false;
    } else if (!xNodeAttr.equals(other.xNodeAttr))
      return false;
    if (yNodeAttr == null) {
      if (other.yNodeAttr != null)
        return false;
    } else if (!yNodeAttr.equals(other.yNodeAttr))
      return false;
    return true;
  }


}
