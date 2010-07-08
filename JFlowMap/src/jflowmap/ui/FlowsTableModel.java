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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import at.fhj.utils.swing.TableSorter;

import jflowmap.views.flowmap.VisualEdge;
import jflowmap.views.flowmap.VisualFlowMap;

/**
 * @author Ilya Boyandin
 */
class FlowsTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;
  
  private List<VisualEdge> visualEdges;

  private VisualFlowMap visualFlowMap;
  
  public void setVisualEdges(List<VisualEdge> visualEdges) {
    this.visualEdges = visualEdges;
    fireTableDataChanged();
    fireTableStructureChanged();
  }

  public void setVisualFlowMap(VisualFlowMap visualFlowMap) {
    this.visualFlowMap = visualFlowMap;
    setVisualEdges(visualFlowMap.getVisualEdges());
  }

  public void showAllVisualEdges() {
    if (visualFlowMap == null) {
      setVisualEdges(null);
    } else {
      setVisualEdges(visualFlowMap.getVisualEdges());
    }
  }

  public int getColumnCount() {
    return 3;
  }

  public int getRowCount() {
    if (visualEdges == null) {
      return 0;
    }
    return visualEdges.size();
  }
  
  public String getColumnName(int column) {
    switch (column) {
      case 0: return "Source node";
      case 1: return "Target node";
      case 2: return "Weight";
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int column) {
    switch (column) {
      case 0: return String.class;
      case 1: return String.class;
      case 2: return Double.class;
    }
    return Object.class;
  }
  
  public Object getValueAt(int row, int column) {
    VisualEdge visualEdge = visualEdges.get(row);
    switch (column) {
      case 0: return visualEdge.getSourceNode().getLabel();
      case 1: return visualEdge.getTargetNode().getLabel();
      case 2: return visualEdge.getEdgeWeight();
    }
    return "";
  }

}
