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

import jflowmap.data.FlowMapLoader;
import prefuse.data.Graph;
import prefuse.data.Table;

/**
 * @author Ilya Boyandin
 */
public class FlowMapAttrsSpec {

    public final String edgeWeightAttr;
    public final String nodeLabelAttr;
    public final String xNodeAttr, yNodeAttr;
    public final double weightFilterMin;

    public FlowMapAttrsSpec(String edgeWeightAttr, String nodeLabelAttr,
            String xNodeAttr, String yNodeAttr, double weightFilterMin) {
        this.edgeWeightAttr = edgeWeightAttr;
        this.nodeLabelAttr = nodeLabelAttr;
        this.xNodeAttr = xNodeAttr;
        this.yNodeAttr = yNodeAttr;
        this.weightFilterMin = weightFilterMin;
    }

    public String getEdgeWeightAttr() {
        return edgeWeightAttr;
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

    public double getWeightFilterMin() {
        return weightFilterMin;
    }

    public void validateFor(Graph graph) {
        validateAttr(graph, graph.getNodeTable(), xNodeAttr, double.class);
        validateAttr(graph, graph.getNodeTable(), yNodeAttr, double.class);
        validateAttr(graph, graph.getNodeTable(), nodeLabelAttr, String.class);
        validateAttr(graph, graph.getEdgeTable(), edgeWeightAttr, double.class);
    }

    private void validateAttr(Graph graph, Table table, String attr, Class<?> type) {
        if (!table.canGet(attr, type)) {
            throw new IllegalArgumentException("Can't get graph's attr:'" + attr + "', graph id:'" + FlowMapLoader.idOf(graph) + "'");
        }
    }
}
