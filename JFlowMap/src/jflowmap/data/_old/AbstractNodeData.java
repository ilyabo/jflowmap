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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractNodeData implements INodeData {

	private List<String> ids;
	private List<String> labels;
	private List<Point2D.Double> positions;

	public AbstractNodeData() {
//		if (ids.size() != labels.size()  ||  ids.size() != positions.size()) {
//			throw new IllegalArgumentException("The input arrays must have the same number of elements");
//		}
		this.ids = new ArrayList<String>();
		this.labels = new ArrayList<String>();
		this.positions = new ArrayList<Point2D.Double>();
	}

	protected void addNode(String id, String label, double x, double y) {
		ids.add(id);
		labels.add(label);
		positions.add(new Point2D.Double(x, y));
	}
	
	public String nodeId(int nodeIdx) {
		return ids.get(nodeIdx);
	}

	public String nodeLabel(int nodeIdx) {
		return labels.get(nodeIdx);
	}

	public double nodeX(int nodeIdx) {
		return positions.get(nodeIdx).x;
	}

	public double nodeY(int nodeIdx) {
		return positions.get(nodeIdx).y;
	}

	public Point2D.Double nodePosition(int nodeIdx) {
		return positions.get(nodeIdx);
	}

	public int numNodes() {
		return ids.size();
	}
}
