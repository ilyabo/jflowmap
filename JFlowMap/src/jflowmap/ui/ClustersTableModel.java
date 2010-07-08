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

import jflowmap.views.flowmap.ClusterTag;
import jflowmap.views.flowmap.VisualNodeCluster;

/**
 * @author ilya
 */
public class ClustersTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 8418700902193831848L;
  private List<VisualNodeCluster> clusters;

  public void setClusters(List<VisualNodeCluster> clusters) {
    this.clusters = clusters;
    fireTableDataChanged();
    fireTableStructureChanged();
  }

  public void clearData() {
    this.clusters = null;
    fireTableDataChanged();
    fireTableStructureChanged();
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
    case 0:
      return "Cluster";
    case 1:
      return "Nodes";
    default:
      return "";
    }
   }
  
  @Override
  public Class<?> getColumnClass(int column) {
    switch (column) {
    case 0:
      return ClusterIcon.class;
    case 1:
      return String.class;
    default:
      return Object.class;
    }
  }
  
  @Override
  public int getColumnCount() {
    return 2;
  }
  
  @Override
  public int getRowCount() {
    if (clusters == null) return 0;
    return clusters.size();
  }

  public Object getValueAt(final int row, int column) {
    VisualNodeCluster cluster = clusters.get(row);
    switch (column) {
    case 0:
      ClusterTag tag = cluster.getTag();
      return new ClusterIcon(tag.getClusterId(), tag.getClusterPaint());
    case 1:
      return cluster.getNodeListAsString();
    default:
      return null;
    }
  }

}
