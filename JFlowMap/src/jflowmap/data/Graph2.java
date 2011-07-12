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

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.tuple.TableTuple;

/**
 * Graph2 has more detailed error messages compared to Graph when accessing
 * table fields which do not exist.
 *
 * @author Ilya Boyandin
 */
public class Graph2 extends Graph {

  private Graph2(Table2 nodes, Table2 edges) {
    super(nodes, edges, true);
  }

  public static Graph2 create() {
    Table2 nodes = new Table2();
    Table2 edges = new Table2();
    nodes.addColumn(Graph.DEFAULT_NODE_KEY, int.class, -1);
    edges.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class, -1);
    edges.addColumn(Graph.DEFAULT_TARGET_KEY, int.class, -1);
    return new Graph2(nodes, edges);
  }

  public static class Table2 extends Table {
    public Table2() {
      this(0, 0);
    }

    public Table2(int nrows, int ncols) {
      super(nrows, ncols, TableTuple.class);
    }

    @Override
    public Object get(int row, String field) {
      int col = getColumnNumber(field);
      if (col < 0) {
        throw new IllegalArgumentException("No such field: '" + field + "'");
      }
      row = getColumnRow(row, col);
      return getColumn(col).get(row);
    }
  }


  public static class Schema2 extends Schema {
    @Override
    public Table instantiate(int nrows) {
      int size = getColumnCount();
      Table2 t = new Table2(nrows, size);
      for (int i=0; i < size; i++) {
        t.addColumn(getColumnName(i), getColumnType(i), getDefault(i));
      }
      return t;
    }
  }

}
