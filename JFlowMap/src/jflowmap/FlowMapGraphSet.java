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

import java.util.List;
import java.util.Map;

import jflowmap.data.FlowMapStats;
import prefuse.data.Graph;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
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

    private final List<Graph> graphs;
    private final FlowMapAttrSpec attrSpec;
    private final FlowMapStats stats;

    public FlowMapGraphSet(Iterable<Graph> graphs, FlowMapAttrSpec attrSpec) {
        this.graphs = ImmutableList.copyOf(graphs);
        this.attrSpec = attrSpec;
        this.stats = FlowMapStats.createFor(graphs, attrSpec);
    }

    public FlowMapAttrSpec getAttrSpec() {
        return attrSpec;
    }

    public FlowMapStats getStats() {
        return stats;
    }

    public List<FlowMapGraph> asList() {
        return Lists.transform(graphs, new Function<Graph, FlowMapGraph>() {
           @Override
            public FlowMapGraph apply(Graph from) {
                return new FlowMapGraph(from, attrSpec, stats);
            }
        });
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

}
