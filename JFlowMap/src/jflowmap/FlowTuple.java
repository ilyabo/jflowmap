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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class FlowTuple {
  private final String srcNodeId;
  private final String targetNodeId;
  private final List<Edge> tuple;

  public FlowTuple(String srcNodeId, String targetNodeId, Iterable<Edge> edges) {
    this.tuple = ImmutableList.copyOf(edges);  // note this list doesn't permit null elements
    this.srcNodeId = srcNodeId;
    this.targetNodeId = targetNodeId;
  }

  public String getSrcNodeId() {
    return srcNodeId;
  }

  public String getTargetNodeId() {
    return targetNodeId;
  }

  public List<Edge> getTuple() {
    return tuple;
  }

  public Edge getElementFor(Graph graph) {
    for (Edge e : tuple) {
      if (e.getGraph() == graph) {
        return e;
      }
    }
    return null;
  }

  /**
   * Return a list of flow tuples composed of edges having the same src and dest nodes.
   * The method will only include edges whoose src and dest nodes satisfy the given predicates.
   * No empty tuples will be returned.
   */
  public static List<FlowTuple> listFlowTuples(FlowMapGraphSet fmgs, Predicate<Node> srcNodeP, Predicate<Node> targetNodeP) {
    Map<FlowTuple.FlowKey, List<Edge>> edgesByFromTo = Maps.newLinkedHashMap();
    for (Graph g : fmgs.asListOfGraphs()) {
      for (int i = 0, numEdges = g.getEdgeCount(); i < numEdges; i++) {
        Edge e = g.getEdge(i);
        if (doesSatisfy(e, srcNodeP, targetNodeP)) {
          FlowKey key = FlowKey.keyFor(e);
          if (edgesByFromTo.containsKey(key)) {
            edgesByFromTo.get(key).add(e);
          } else {
            edgesByFromTo.put(key, Lists.newArrayList(e));
          }
        }
      }
    }
    return asFlowTuple(edgesByFromTo);
  }

  public static Predicate<Node> nodeIdIsOneOf(String...nodeIds) {
    return nodeIdIsOneOf(Arrays.asList(nodeIds));
  }

  public static Predicate<Node> nodeIdIsOneOf(final Collection<String> nodeIds) {
    return new Predicate<Node>() {
      @Override
      public boolean apply(Node node) {
        return nodeIds.contains(FlowMapGraph.getNodeId(node));
      }
    };
  }

  private static boolean doesSatisfy(Edge e, Predicate<Node> srcNodeP, Predicate<Node> targetNodeP) {
    return (srcNodeP == null  ||  srcNodeP.apply(e.getSourceNode()))  &&
        (targetNodeP == null  ||  targetNodeP.apply(e.getTargetNode()));
  }

  private static List<FlowTuple> asFlowTuple(Map<FlowTuple.FlowKey, List<Edge>> edgesByFromTo) {
    List<FlowTuple> tuples = Lists.newArrayList();
    for (Map.Entry<FlowTuple.FlowKey, List<Edge>> e : edgesByFromTo.entrySet()) {
      tuples.add(new FlowTuple(e.getKey().srcId, e.getKey().targetId, e.getValue()));
    }
    return tuples;
  }

  private static class FlowKey {
    final String srcId, targetId;

    public FlowKey(String srcId, String targetId) {
      this.srcId = srcId;
      this.targetId = targetId;
    }

    public static FlowKey keyFor(Edge e) {
      return new FlowKey(FlowMapGraph.getNodeId(e.getSourceNode()), FlowMapGraph.getNodeId(e.getTargetNode()));
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((srcId == null) ? 0 : srcId.hashCode());
      result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
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
      FlowTuple.FlowKey other = (FlowTuple.FlowKey) obj;
      if (srcId == null) {
        if (other.srcId != null)
          return false;
      } else if (!srcId.equals(other.srcId))
        return false;
      if (targetId == null) {
        if (other.targetId != null)
          return false;
      } else if (!targetId.equals(other.targetId))
        return false;
      return true;
    }
  }

}