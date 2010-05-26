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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import jflowmap.visuals.flowmap.ClusterTag;
import jflowmap.visuals.flowmap.VisualNode;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
class ClusterNodesTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;
  private List<VisualNode> visualNodes;
  private Map<Integer, ClusterIcon> clusterIcons;
  
  public void setVisualNodes(List<VisualNode> nodes) {
    if (nodes == null) {
      this.visualNodes = null;
      this.clusterIcons = null;
    } else {
      this.visualNodes = Lists.newArrayList(
        Iterators.filter(
            nodes.iterator(),
            new Predicate<VisualNode>() {
              public boolean apply(VisualNode node) {
                return node.getClusterTag() != null;
              }
            }
        )
      );
      Collections.sort(visualNodes, VisualNode.LABEL_COMPARATOR);
      fireTableDataChanged();
      //fireTableChanged();
      fireTableStructureChanged();
      initClusterIcons();
    }
  }

  public void clearData() {
    setVisualNodes(null);
  }

  private void initClusterIcons() {
    clusterIcons = new HashMap<Integer, ClusterIcon>();
    for (VisualNode node : visualNodes) {
      ClusterTag tag = node.getClusterTag();
      int clusterId = tag.getClusterId();
      if (!clusterIcons.containsKey(clusterId)) {
        clusterIcons.put(clusterId, new ClusterIcon(clusterId, tag.getClusterPaint()));
      }
    }
  }

  public int getColumnCount() {
    return 2;
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Node";
      case 1:
        return "Cluster";
      default:
        return "";
    }
  }
  
  public VisualNode getVisualNode(int row) {
    return visualNodes.get(row);
  }
  
  public Class<?> getColumnClass(int column) {
    switch (column) {
      case 0:
        return String.class;
      case 1:
        return ClusterIcon.class;
      default:
        return Object.class;
    }
  }


  public int getRowCount() {
    if (visualNodes == null) return 0;
    return visualNodes.size();
  }

  public Object getValueAt(final int row, int column) {
    final VisualNode node = visualNodes.get(row);
    switch (column) {
      case 0: return node.getLabel();
//      case 1: return node.getClusterId();
      case 1: return clusterIcons.get(node.getClusterTag().getClusterId());
    }
    return null;
  }

  
}
