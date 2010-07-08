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

package jflowmap.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import jflowmap.views.flowmap.VisualNodeDistance;

class NodeSimilarityDistancesTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private List<VisualNodeDistance> distances;
//    private VisualNode selectedNode;

    public void setDistances(List<VisualNodeDistance> distances) {
      if (distances == null) {
        this.distances = null;
      } else {
        this.distances = new ArrayList<VisualNodeDistance>(distances);
        Collections.sort(this.distances, VisualNodeDistance.FROM_LABEL_COMPARATOR);
      }
      fireTableDataChanged();
      fireTableStructureChanged();
    }

//    public void setSelectedNode(VisualNode node) {
//      selectedNode = node;
//      fireTableDataChanged();
//      //fireTableChanged();
//      fireTableStructureChanged();
//    }

    public void clearData() {
      setDistances(null);
//      selectedNode = null;
    }

    public boolean isCellEditable(int row, int col) {
      return false;
    }

    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "Source node";
        case 1:
          return "Target node";
        case 2:
          return "Distance";
        default:
          return "";
      }
    }

    public Class<?> getColumnClass(int column) {
      switch (column) {
        case 0:
          return String.class;
        case 1:
          return String.class;
        case 2:
          return Double.class;
        default:
          return Object.class;
      }
    }

    public int getRowCount() {
      if (distances == null) return 0;
      return distances.size();
    }

    public Object getValueAt(int row, int column) {
      switch (column) {
        case 0:
          return distances.get(row).getSource().getLabel();
        case 1:
          return distances.get(row).getTarget().getLabel();
        default:
          return distances.get(row).getDistance();
      }
    }
  }