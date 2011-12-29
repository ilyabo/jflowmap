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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jflowmap.data.FlowMapStats;
import jflowmap.data.StaxGraphMLReader;
import jflowmap.data.MultiFlowMapStats;

import org.apache.log4j.Logger;

import prefuse.data.Graph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * A set of FlowMapGraphs with common global attrSpec and stats.
 *
 * @author Ilya Boyandin
 */
public class FlowMapGraphSet {

  public static Logger logger = Logger.getLogger(FlowMapGraphSet.class);

  private final List<Graph> graphs;
  private final FlowMapAttrSpec attrSpec;
  private final FlowMapStats stats;

  public FlowMapGraphSet(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
    this.graphs = ImmutableList.copyOf(graphs);
    this.attrSpec = attrSpec;
    this.stats = MultiFlowMapStats.createFor(graphs, attrSpec);
  }

  public FlowMapAttrSpec getAttrSpec() {
    return attrSpec;
  }

  public FlowMapStats getStats() {
    return stats;
  }

  public List<FlowMapGraph> asList() {
    return Lists.transform(graphs, FlowMapGraph.funcGraphToFlowMapGraph(attrSpec, stats));
  }

  public List<Graph> asListOfGraphs() {
    return graphs;
  }

  public int size() {
    return graphs.size();
  }

  /**
   * Finds the graph with the given {@code id} in this set and
   * returns a FlowMapGraph with the global stats for the graphs in the set.
   * Returns null in no such graph found.
   */
  public FlowMapGraph findFlowMapGraphById(String id) {
    for (Graph graph : graphs) {
      if (id.equals(FlowMapGraph.getGraphId(graph))) {
        return new FlowMapGraph(graph, attrSpec, stats);
      }
    }
    return null;
  }

  /**
   * If the given graph is in this set, returns the corresponding FlowMapGraph
   */
  public FlowMapGraph findFlowMapGraphFor(Graph graph) {
    return findFlowMapGraphById(FlowMapGraph.getGraphId(graph));
  }

  public Map<String, String> mapOfNodeIdsToAttrValues(String nodeAttr) {
    Map<String, String> nodeIdsToLabels = Maps.newLinkedHashMap();
    for (FlowMapGraph fmg : asList()) {
      nodeIdsToLabels.putAll(fmg.mapOfNodeIdsToAttrValues(nodeAttr));
    }
    return nodeIdsToLabels;
  }

  /**
   * Builds a multimap between the values of the given {@nodeAttr} and the
   * node ids having these values. The multimap is built over the whole set
   * of flow map graphs.
   */
  public Multimap<Object, String> multimapOfNodeAttrValuesToNodeIds(String nodeAttr) {
    Multimap<Object, String> nodeAttrValuesToNodeIds = LinkedHashMultimap.create();
    for (FlowMapGraph fmg : asList()) {
      nodeAttrValuesToNodeIds.putAll(fmg.multimapOfNodeAttrValuesToNodeIds(nodeAttr));
    }
    return nodeAttrValuesToNodeIds;
  }

  public static FlowMapGraphSet loadGraphML(String filename, FlowMapAttrSpec attrSpec)
      throws IOException {
    return new FlowMapGraphSet(StaxGraphMLReader.readGraphs(filename), attrSpec);
  }

  /**
   * @param stats Should normally be null, except in case if the stats have to be induced
   * and not calculated (e.g. when a global mapping over a number of flow maps for small
   * multiples must be used).
   */
  public static List<FlowMapGraph> loadGraphMLAsList(String filename, FlowMapAttrSpec attrSpec,
      FlowMapStats stats)
      throws IOException {
    return ImmutableList.copyOf(
        Iterables.transform(
            StaxGraphMLReader.readGraphs(filename), FlowMapGraph.funcGraphToFlowMapGraph(attrSpec, stats)));
  }

  /**
   * Nodes having the same value of nodeAttrToGroupBy are grouped into one. Flows are
   * also updated, so that they connect the grouped nodes.
   * <p>
   * NOTE: If nodes of different graphs in this set have different values of nodeAttrToGroupBy,
   * the grouped nodes of these graphs will not be the same.
   */
  public FlowMapGraphSet groupNodesBy(String nodeAttrToGroupBy) {
    List<Graph> groupedGraphs = Lists.newArrayList();

    for (FlowMapGraph fmg : asList()) {
      FlowMapGraph grouped = fmg.groupNodesBy(nodeAttrToGroupBy);
      groupedGraphs.add(grouped.getGraph());
    }

    return new FlowMapGraphSet(groupedGraphs, attrSpec);
  }

}
