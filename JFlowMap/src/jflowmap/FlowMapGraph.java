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
import java.util.Set;

import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */

// TODO: finish class FlowMapGraph

public class FlowMapGraph {

    public static final String GRAPH_CLIENT_PROPERTY__ID = "id";
    public static final String GRAPH_NODE_TABLE_COLUMN_NAME__ID = "_node_id";

//    private Graph graph;

    private FlowMapGraph() {
    }

    // TODO: create class FlowMapGraph encapsulating Graph and move these methods there
    public static String getGraphId(Graph graph) {
        return (String) graph.getClientProperty(GRAPH_CLIENT_PROPERTY__ID);
    }

    public static void setGraphId(Graph graph, String name) {
        graph.putClientProperty(GRAPH_CLIENT_PROPERTY__ID, name);
    }

    public static String getNodeId(Node node) {
        return node.getString(GRAPH_NODE_TABLE_COLUMN_NAME__ID);
    }

    public static Node findNodeById(Graph graph, String nodeId) {
        int index = findNodeIndexById(graph, nodeId);
        if (index >= 0) {
            return graph.getNode(index);
        }
        return null;
    }

    public static int findNodeIndexById(Graph graph, String nodeId) {
        for (int i = 0, len = graph.getNodeCount(); i < len; i++) {
            Node node = graph.getNode(i);
            if (nodeId.equals(getNodeId(node))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> Set<T> getNodeAttrValues(Graph graph, String attrName) {
        return getNodeAttrValues(Arrays.asList(graph), attrName);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getNodeAttrValues(Iterable<Graph> graphs, String attrName) {
        Set<T> values = Sets.newLinkedHashSet();
        for (Graph g : graphs) {
            for (int i = 0, len = g.getNodeCount(); i < len; i++) {
                Node node = g.getNode(i);
                T v = (T) node.get(attrName);
                if (v != null) {
                    values.add(v);
                }
            }
        }
        return values;
    }

}
