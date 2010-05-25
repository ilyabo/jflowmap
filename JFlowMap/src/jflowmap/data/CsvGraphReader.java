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

import java.io.IOException;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import prefuse.util.io.IOLib;

/**
 * @author Ilya Boyandin
 */
public class CsvGraphReader {

  public Graph readGraph(String nodesLocation, String edgesLocation) throws IOException {

    Graph graph = new Graph();
//    graph.addColumn(VisualFlowMapModel.DEFAULT_NODE_X_ATTR_NAME, double.class);
//    graph.addColumn(VisualFlowMapModel.DEFAULT_NODE_Y_ATTR_NAME, double.class);
//    graph.addColumn(COLUMN_VALUE, double.class);

    CSVTableReader reader = new CSVTableReader();
    try {
      Table nodeTable = reader.readTable(IOLib.streamFromString(nodesLocation));
      Table edgeTable = reader.readTable(IOLib.streamFromString(edgesLocation));

//      new FlowMapGraphBuilder();

      for (int i = 0, tuples = edgeTable.getTupleCount(); i < tuples; i++) {

      }


    } catch (DataIOException e) {
      throw new IOException(e);
    }

    return null;
  }

}
