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
import prefuse.data.io.DataIOException;

import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class FlowMapLoader {

    private static final String GRAPH_CLIENT_PROPERTY__ID = "id";

    private FlowMapLoader() {
    }

    public static String idOf(Graph graph) {
        return (String) graph.getClientProperty(FlowMapLoader.GRAPH_CLIENT_PROPERTY__ID);
    }

    public static void setGraphId(Graph graph, String name) {
        graph.putClientProperty(FlowMapLoader.GRAPH_CLIENT_PROPERTY__ID, name);
    }

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
