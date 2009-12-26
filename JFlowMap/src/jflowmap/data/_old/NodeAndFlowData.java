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

package jflowmap.data._old;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jflowmap.data.MinMax;

/**
 * @author Ilya Boyandin
 */
public class NodeAndFlowData implements INodeAndFlowData {

	private final INodeData nodeData;
	private final IFlowData flowData;
	private final Map<String, Integer> nodeIdToIndex;
    private final int[] flowStartNodeIdx;
    private final int[] flowEndNodeIdx;

	public NodeAndFlowData(INodeData nodeData, IFlowData flowData) {
		this.nodeData = nodeData;
		this.nodeIdToIndex = new HashMap<String, Integer>(nodeData.numNodes());
		for (int i = 0, numNodes = nodeData.numNodes(); i < numNodes; i++) {
			final String nodeId = nodeData.nodeId(i);
			if (nodeIdToIndex.containsKey(nodeId)) {
				throw new IllegalArgumentException("Duplicate node id " + nodeId);
			}
			nodeIdToIndex.put(nodeId, i);
		}
		this.flowData = flowData;
		this.flowStartNodeIdx = new int[flowData.numFlows()]; 
		this.flowEndNodeIdx = new int[flowData.numFlows()]; 
		for (int i = 0, numFlows = flowData.numFlows(); i < numFlows; i++) {
			String startId = flowData.getFlowStartNodeId(i);
			checkNodeExists(startId);
			String endId = flowData.getFlowEndNodeId(i);
			checkNodeExists(endId);
			
			flowStartNodeIdx[i] = nodeIdToIndex.get(startId);
			flowEndNodeIdx[i] = nodeIdToIndex.get(endId);
		}
	}

	private void checkNodeExists(String nodeId) {
		if (!nodeIdToIndex.containsKey(nodeId)) {
			throw new IllegalArgumentException("Node " + nodeId + " doesn't exist");
		}
	}
	
	public int flowNode1(int flowIdx) {
		return flowStartNodeIdx[flowIdx];
	}

	public int flowNode2(int flowIdx) {
		return flowEndNodeIdx[flowIdx];
	}

	public IFlowData getFlowData() {
		return flowData;
	}

	public INodeData getNodeData() {
		return nodeData;
	}

    public static MinMax getFlowStats(INodeAndFlowData data, final String attrName) {
        final IFlowData flowData = data.getFlowData();
        final int size = flowData.numFlows();

        return MinMax.createFor(new Iterator<Double>() {
            int i = 0;
            public boolean hasNext() {
                return i < size;
            }
            public Double next() {
                return DoubleAttrValue.asDouble(flowData.getAttrValue(i++, attrName));
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        });
    }

	public String getNodeId(int nodeIdx) {
		return nodeData.nodeId(nodeIdx);
	}

	public int getNodeIndex(String nodeId) {
		return nodeIdToIndex.get(nodeId);
	}
	
	public double getOutgoingTotal(String nodeId, String attrName) {
	    final int nodeIdx = nodeIdToIndex.get(nodeId);
	    double total = 0;
	    for (int i = 0, size = flowStartNodeIdx.length; i < size; i++) {
	        if (flowStartNodeIdx[i] == nodeIdx) {
                double v = flowData.getAttrValueAsDouble(i, attrName);
                if (!Double.isNaN(v)) total += v;
	        }
	    }
	    return total;
	}
	
	public double getIncomingTotal(String nodeId, String attrName) {
        final int nodeIdx = nodeIdToIndex.get(nodeId);
        double total = 0;
        for (int i = 0, size = flowEndNodeIdx.length; i < size; i++) {
            if (flowEndNodeIdx[i] == nodeIdx) {
                double v = flowData.getAttrValueAsDouble(i, attrName);
                if (!Double.isNaN(v)) total += v;
            }
        }
        return total;
    }

}
