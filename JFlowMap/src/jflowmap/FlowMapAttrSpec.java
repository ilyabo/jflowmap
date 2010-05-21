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

import prefuse.data.Graph;
import prefuse.data.Table;

/**
 * @author Ilya Boyandin
 */
public class FlowMapAttrSpec {

    private final String edgeWeightAttr;
    private final String nodeLabelAttr;
    private final String xNodeAttr, yNodeAttr;
    private final double weightFilterMin;  // TODO: weightFilterMin shouldn't be in FlowMapAttrSpec

    public FlowMapAttrSpec(String edgeWeightAttr, String nodeLabelAttr,
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

    public void checkValidityFor(Graph graph) {
        validateAttr(graph, graph.getNodeTable(), xNodeAttr, double.class);
        validateAttr(graph, graph.getNodeTable(), yNodeAttr, double.class);
        validateAttr(graph, graph.getNodeTable(), nodeLabelAttr, String.class);
        validateAttr(graph, graph.getEdgeTable(), edgeWeightAttr, double.class);
    }

    private void validateAttr(Graph graph, Table table, String attr, Class<?> type) {
        if (!table.canGet(attr, type)) {
            throw new IllegalArgumentException("Can't get graph's attr:'" + attr + "', graph id:'" + FlowMapGraph.getGraphId(graph) + "'");
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((edgeWeightAttr == null) ? 0 : edgeWeightAttr.hashCode());
        result = prime * result + ((nodeLabelAttr == null) ? 0 : nodeLabelAttr.hashCode());
        long temp;
        temp = Double.doubleToLongBits(weightFilterMin);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        if (edgeWeightAttr == null) {
            if (other.edgeWeightAttr != null)
                return false;
        } else if (!edgeWeightAttr.equals(other.edgeWeightAttr))
            return false;
        if (nodeLabelAttr == null) {
            if (other.nodeLabelAttr != null)
                return false;
        } else if (!nodeLabelAttr.equals(other.nodeLabelAttr))
            return false;
        if (Double.doubleToLongBits(weightFilterMin) != Double.doubleToLongBits(other.weightFilterMin))
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
