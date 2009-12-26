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

package jflowmap.aggregation;

import java.util.List;

import at.fhj.utils.misc.ProgressTracker;
import ch.unifr.dmlib.cluster.AbstractDistanceMatrix;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;

/**
 * @author Ilya Boyandin
 */
class EdgeSegmentDistanceMatrix extends AbstractDistanceMatrix<EdgeSegment> {

    public EdgeSegmentDistanceMatrix(List<EdgeSegment> items,
            DistanceMeasure<EdgeSegment> distanceMeasure,
            double maxMergeableDistance) {
        super(items, distanceMeasure, null, maxMergeableDistance);
    }

    @Override
    protected ClusterNode<EdgeSegment> mergeClusterNodes(
            ClusterNode<EdgeSegment> cn1, ClusterNode<EdgeSegment> cn2, double dist) {
        EdgeSegment item1 = cn1.getItem();
        EdgeSegment item2 = cn2.getItem();

//        assert item1.canBeAggregatedWith(item2);

        EdgeSegment aggregate = item1.aggregateWith(item2);

//        System.out.println("Merge item " + System.identityHashCode(item1) + " with " + System.identityHashCode(item2));

        // TODO: first replace item1 and item2 then update adjacent segments
        // (otherwise it can be problematic if item1 and item2 share a parent edge)
        item1.replaceWith(aggregate);
        item2.replaceWith(aggregate);

        assert aggregate.checkParentEdgesSegmentConsecutivity();

        return new ClusterNode<EdgeSegment>(aggregate, cn1, cn2, dist);
    }

    @Override
    public void calc(ProgressTracker progress) {
    }

    @Override
    protected double getDistanceBetweenClusterNodes(int i, int j) {
        return getDistanceMeasure().distance(getNode(i).getItem(), getNode(j).getItem());
    }

    @Override
    protected void updateDistances(int mergedNode1, int mergedNode2,
            ClusterNode<EdgeSegment> newNode) {
    }


}
