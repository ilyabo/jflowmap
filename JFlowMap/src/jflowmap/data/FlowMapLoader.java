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

package jflowmap.data;

import javax.swing.JOptionPane;

import jflowmap.DatasetSpec;
import jflowmap.FlowMapGraphWithAttrSpecs;
import jflowmap.JFlowMap;
import jflowmap.models.map.AreaMap;
import jflowmap.visuals.VisualAreaMap;
import jflowmap.visuals.VisualFlowMap;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.DataIOException;

import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class FlowMapLoader {

    private static final String GRAPH_CLIENT_PROPERTY__ID = "id";
    static final String GRAPH_NODE_TABLE_COLUMN_NAME__ID = "_node_id";

    private FlowMapLoader() {
    }


    // TODO: create class FlowMap encapsulating Graph and move these methods there
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

//    public static void setNodeId(Node node, String id) {
//        Table table = node.getGraph().getNodeTable();
//        if (table.getColumn(GRAPH_NODE_TABLE_COLUMN_NAME__ID) == null) {
//            table.addColumn(GRAPH_NODE_TABLE_COLUMN_NAME__ID, String.class);
//        }
//        node.setString(GRAPH_NODE_TABLE_COLUMN_NAME__ID, id);
//    }




    public static Graph loadGraph(String filename) throws DataIOException {
        JFlowMap.logger.info("Loading \"" + filename + "\"");
//        Graph graph = GraphFileFormats.createReaderFor(filename).readGraph(filename);
//        logger.info("Graph loaded: " + graph.getNodeCount() + " nodes, " + graph.getEdgeCount() + " edges");

        GraphMLReader2 reader = new GraphMLReader2();
        Iterable<Graph> graphs = reader.readFromFile(filename);
        JFlowMap.logger.info("Graphs loaded: " + Iterables.size(graphs));
        for (Graph g : graphs) {
            JFlowMap.logger.info(
                    "Graph '" + g.getClientProperty(FlowMapLoader.GRAPH_CLIENT_PROPERTY__ID) + "': " +
                    g.getNodeCount() + " nodes, " + g.getEdgeCount() + " edges");
        }
        Graph graph = graphs.iterator().next();

        return graph;
    }

    public static void loadFlowMap(JFlowMap jFlowMap, DatasetSpec dataset, FlowMapStats stats) {
        JFlowMap.logger.info("> Loading flow map \"" + dataset + "\"");
        try {
            Graph graph = loadGraph(dataset.getFilename());

            FlowMapGraphWithAttrSpecs graphAndSpecs = new FlowMapGraphWithAttrSpecs(graph, dataset.getAttrsSpec());

            VisualFlowMap visualFlowMap = jFlowMap.createVisualFlowMap(graphAndSpecs, stats);
            if (dataset.getAreaMapFilename() != null) {
                AreaMap areaMap = AreaMap.load(dataset.getAreaMapFilename());
                visualFlowMap.setAreaMap(new VisualAreaMap(visualFlowMap, areaMap));
            }
            jFlowMap.setVisualFlowMap(visualFlowMap);

        } catch (Exception e) {
            JFlowMap.logger.error("Couldn't load flow map " + dataset, e);
            JOptionPane.showMessageDialog(jFlowMap,
                    "Couldn't load flow map: [" + e.getClass().getSimpleName()+ "] " + e.getMessage());
        }
    }


}
