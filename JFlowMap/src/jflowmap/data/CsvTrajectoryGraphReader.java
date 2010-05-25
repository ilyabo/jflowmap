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

package jflowmap.data;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jflowmap.visuals.VisualFlowMapModel;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.AbstractGraphReader;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

/**
 * @author Ilya Boyandin
 */
public class CsvTrajectoryGraphReader extends AbstractGraphReader {

  private static final String COLUMN_X = VisualFlowMapModel.DEFAULT_NODE_X_ATTR_NAME;
  private static final String COLUMN_Y = VisualFlowMapModel.DEFAULT_NODE_Y_ATTR_NAME;
  private static final String COLUMN_VALUE = VisualFlowMapModel.DEFAULT_EDGE_WEIGHT_ATTR_NAME;

  private Map<Point2D, Node> nodes;
  private Graph graph;

  @Override
  public Graph readGraph(InputStream is) throws DataIOException {
    CSVTableReader reader = new CSVTableReader();
    Table table = reader.readTable(is);

    nodes = new HashMap<Point2D, Node>();
    graph = new Graph();
    graph.addColumn(COLUMN_X, double.class);
    graph.addColumn(COLUMN_Y, double.class);
    graph.addColumn(COLUMN_VALUE, double.class);

    for (int i = 0, tuples = table.getTupleCount(); i < tuples; i++) {
      Tuple tuple = table.getTuple(i);
      if (tuple.getColumnCount() < 4) {
        throw new DataIOException("Not enough data columns in line " + (i + 1) + ". Must be at least 4.");
      }
      Point2D from = new Point2D.Double(tuple.getDouble(0), tuple.getDouble(1));
      Point2D to = new Point2D.Double(tuple.getDouble(2), tuple.getDouble(3));
      Edge edge = graph.addEdge(getNode(from), getNode(to));

      if (tuple.getColumnCount() >= 4) {
        edge.set(COLUMN_VALUE, tuple.getDouble(4));
      }
    }

    return graph;
  }

  private Node getNode(Point2D point) {
    Node node = nodes.get(point);
    if (node == null) {
      node = graph.addNode();
      node.set(COLUMN_X, point.getX());
      node.set(COLUMN_Y, point.getY());
      nodes.put(point, node);
    }
    return node;
  }

}
