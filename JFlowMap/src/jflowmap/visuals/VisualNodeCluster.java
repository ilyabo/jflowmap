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

package jflowmap.visuals;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jflowmap.geom.GeomUtils;
import jflowmap.geom.Point;
import jflowmap.models.FlowMapGraphBuilder;
import jflowmap.util.ColorUtils;
import jflowmap.util.Pair;
import prefuse.data.Graph;
import prefuse.data.Node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Ilya Boyandin
 */
public class VisualNodeCluster implements Iterable<VisualNode> {

    private static final String JOINED_FLOW_MAP_CLUSTER_ATTR = "visualNodeCluster";
    private static final String LABEL_SEPARATOR = "; ";
//    private static final String LABEL_SEPARATOR = "\n ";
    private ClusterTag tag;
    private final List<VisualNode> nodes;

    private VisualNodeCluster(int id, Paint color, Iterator<VisualNode> it) {
        this.nodes = new ArrayList<VisualNode>();
        this.tag = ClusterTag.createFor(id, color);
        Iterators.addAll(nodes, it);
        updateClusterTags();
    }

    public void setVisible(boolean visible) {
        if (visible == tag.isVisible()) {
            return;     // no change
        }
        tag = tag.withVisible(visible);
        updateClusterTags();
    }

    private void updateClusterTags() {
        for (VisualNode node : nodes) {
            node.setClusterTag(tag);
        }
    }

    public ClusterTag getTag() {
        return tag;
    }

    private transient String cachedNodeListAsString;

    public String getNodeListAsString() {
        if (cachedNodeListAsString == null) {
            cachedNodeListAsString = makeCumulatedLabel(this);
        }
        return cachedNodeListAsString;
    }

    public List<VisualEdge> getIncomingEdges() {
        List<VisualEdge> edges = new ArrayList<VisualEdge>();
        for (VisualNode node : nodes) {
            edges.addAll(node.getIncomingEdges());
        }
        return edges;
    }

    public List<VisualEdge> getOutgoingEdges() {
        List<VisualEdge> edges = new ArrayList<VisualEdge>();
        for (VisualNode node : nodes) {
            edges.addAll(node.getIncomingEdges());
        }
        return edges;
    }

    public Iterator<VisualNode> iterator() {
        return nodes.iterator();
    }

    public int size() {
        return nodes.size();
    }

    public static VisualNodeCluster createFor(int id, Paint color, Iterator<VisualNode> it) {
        return new VisualNodeCluster(id, color, it);
    }

    public static List<VisualNodeCluster> createClusters(List<List<VisualNode>> nodeClusterLists, double clusterDistanceThreshold) {
//        List<List<VisualNode>> clusters = Lists.newArrayList(Iterators.filter(
//                ClusterSetBuilder.getClusters(rootCluster, clusterDistanceThreshold).iterator(),
//                new Predicate<List<VisualNode>>() {
//                    public boolean apply(List<VisualNode> nodes) {
//                        return (nodes.size() > 1);
//                    }
//                }
//        ));
        final int numClusters = nodeClusterLists.size();

        Color[] colors = ColorUtils.createCategoryColors(numClusters);
        List<VisualNodeCluster> nodeClusters = Lists.newArrayListWithExpectedSize(numClusters);
        int cnt = 0;
        for (Collection<VisualNode> nodes : nodeClusterLists) {
            nodeClusters.add(VisualNodeCluster.createFor(cnt + 1, colors[cnt], nodes.iterator()));
            cnt++;
        }
        return nodeClusters;
    }

    public static List<List<VisualNode>> combineClusters(List<List<VisualNode>> clusters1, List<List<VisualNode>> clusters2) {
        Map<VisualNode, Integer> map1 = createNodeToClusterIndexMap(clusters1);
        Map<VisualNode, Integer> map2 = createNodeToClusterIndexMap(clusters2);

        Multimap<Pair<Integer, Integer>, VisualNode> newClusters = LinkedHashMultimap.create();
        for (List<VisualNode> cluster : clusters1) {
            for (VisualNode node : cluster) {
                newClusters.put(Pair.of(map1.get(node), map2.get(node)), node);
            }
        }

        List<List<VisualNode>> newClustersList = Lists.newArrayList();
        for (Pair<Integer, Integer> key : newClusters.asMap().keySet()) {
            newClustersList.add(ImmutableList.copyOf(newClusters.get(key)));
        }

        return newClustersList;
    }

    private static Map<VisualNode, Integer> createNodeToClusterIndexMap(
            List<List<VisualNode>> nodeClusterLists) {
        Map<VisualNode, Integer> map = Maps.newHashMap();
        for (int clusterIndex = 0, size = nodeClusterLists.size(); clusterIndex < size; clusterIndex++) {
            for (VisualNode node : nodeClusterLists.get(clusterIndex)) {
                map.put(node, clusterIndex);
            }
        }
        return map;
    }


    public static Graph createClusteredFlowMap(List<VisualNodeCluster> clusters) {
        FlowMapGraphBuilder builder =
            new FlowMapGraphBuilder(null).withCumulativeEdges().addNodeAttr(
                    JOINED_FLOW_MAP_CLUSTER_ATTR, VisualNodeCluster.class);

        // Create (visualNode->cluster node) mapping
        Map<VisualNode, Node> visualToNode = Maps.newHashMap();
        for (VisualNodeCluster cluster : clusters) {
            Point centroid = GeomUtils.centroid(
                    Iterators.transform(
                        cluster.iterator(), VisualNode.TRANSFORM_NODE_TO_POSITION
                    )
            );
            Node node = builder.addNode(centroid, makeCumulatedLabel(cluster));
            node.set(JOINED_FLOW_MAP_CLUSTER_ATTR, cluster);
            for (VisualNode visualNode : cluster) {
                visualToNode.put(visualNode, node);
            }
        }

        // Edges between clusters
        for (VisualNodeCluster cluster : clusters) {
            for (VisualNode node : cluster) {
                for (VisualEdge visualEdge : node.getEdges()) {
                    builder.addEdge(
                            visualToNode.get(visualEdge.getSourceNode()),
                            visualToNode.get(visualEdge.getTargetNode()),
                            visualEdge.getEdgeWeight());
                }
            }
        }

        return builder.build();
    }

    private static String makeCumulatedLabel(VisualNodeCluster cluster) {
        ArrayList<VisualNode> nodes = Lists.newArrayList(cluster);
        Collections.sort(nodes, VisualNode.LABEL_COMPARATOR);
        StringBuilder label = new StringBuilder();
        label.append("[");
        for (VisualNode visualNode : nodes) {
            label.append(visualNode.getLabel()).append(LABEL_SEPARATOR);
        }
        if (label.length() > 0) {
            label.setLength(label.length() - LABEL_SEPARATOR.length()); // remove the last separator
        }
        label.append("]");
        return label.toString();
    }

    public static VisualNodeCluster getJoinedFlowMapNodeCluster(Node node) {
        if (!node.canGet(JOINED_FLOW_MAP_CLUSTER_ATTR, VisualNodeCluster.class)) {
//            throw new IllegalArgumentException("Node " + node + " doesn't have a cluster attr");
            return null;
        }
        return (VisualNodeCluster) node.get(JOINED_FLOW_MAP_CLUSTER_ATTR);
    }

}
