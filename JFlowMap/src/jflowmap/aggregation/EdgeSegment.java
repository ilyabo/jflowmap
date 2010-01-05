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

import java.util.Collections;
import java.util.List;

import jflowmap.geom.FPoint;
import jflowmap.geom.GeomUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * EdgeSegment should be mutable, otherwise it's difficult to propagate
 * to the cluster tree  the changes made to the segments adjacent to
 * those which are merged.
 *
 * @author Ilya Boyandin
 */
public class EdgeSegment {

    private FPoint a;
    private FPoint b;
    private final double weight;
    private final List<SegmentedEdge> parents = Lists.newArrayList();

    public EdgeSegment(FPoint a, FPoint b, double weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    public EdgeSegment(FPoint a, FPoint b, double weight, SegmentedEdge parent) {
        this(a, b, weight);
        this.parents.add(parent);
    }

    public EdgeSegment(FPoint a, FPoint b, double weight, Iterable<SegmentedEdge> parents) {
        this(a, b, weight);
        Iterables.addAll(this.parents, parents);
    }

    public FPoint getA() {
        return a;
    }

    public FPoint getB() {
        return b;
    }

    public void setA(FPoint newA) {
        if (newA.equals(a)) {
            return;
        }
        if (a.isFixed()) {
            throw new IllegalStateException("A is a fixed point");
        }
        this.a = newA;
        for (SegmentedEdge se : parents) {
            EdgeSegment prev = se.getPrev(this);
            if (prev != null) {
                prev.setB(newA);
            }
        }
    }

    public void setB(FPoint newB) {
        if (newB.equals(b)) {
            return;
        }
        if (b.isFixed()) {
            throw new IllegalStateException("B is a fixed point");
        }
        this.b = newB;
        for (SegmentedEdge se : parents) {
            EdgeSegment next = se.getNext(this);
            if (next != null) {
                next.setA(newB);
            }
        }
    }

    public double getWeight() {
        return weight;
    }

    public List<SegmentedEdge> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public void addParent(SegmentedEdge parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public void removeParent(SegmentedEdge parent) {
        parents.remove(parent);
    }

    public boolean isConsecutiveFor(EdgeSegment seg) {
        return seg.getB().equals(getA());
    }

    public boolean canBeReplacedWith(EdgeSegment seg) {
        return
            (!a.isFixed()  ||  a.getPoint().equals(seg.getA().getPoint()))  &&
            (!b.isFixed()  ||  b.getPoint().equals(seg.getB().getPoint()))
        ;
    }

    /**
     * This method usually doesn't change {@code newSegment}, but there is one situation
     * when it does: If a point (a or b) of {@code this} is fixed and the corresponding
     * point of {@code newSegment} isn't, then the {@code newSegment}'s point also becomes fixed.
     */
    public void replaceWith(EdgeSegment newSegment) {
//        logger.debug("Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment));
        if (!canBeReplacedWith(newSegment)) {
            throw new IllegalArgumentException(this + " cannot be replaced with " + newSegment);
        }
        if (a.isFixed()  &&  !newSegment.getA().isFixed()) {
            assert(a.getPoint().equals(newSegment.getA().getPoint()));
            newSegment.setA(a);
        }
        if (b.isFixed()  &&  !newSegment.getB().isFixed()) {
            assert(b.getPoint().equals(newSegment.getB().getPoint()));
            newSegment.setB(b);
        }
        EdgeSegment oldSegment = this;
        // replace segment in all parent edges
        for (SegmentedEdge se : parents) {
//            logger.debug(" >> Replace segment " + System.identityHashCode(this) + " with " + System.identityHashCode(newSegment) +
//                    " in edge " + System.identityHashCode(se));
//            se.replaceSegment(this, newSegment);

            int index = se.indexOf(oldSegment);
            if (index < 0) {
                throw new IllegalArgumentException();
            }
            se.setSegment(index, newSegment);
            newSegment.addParent(se);
        }

        // update prev and next in all parent edges
        for (SegmentedEdge se : parents) {
            int index = se.indexOf(newSegment);

            // update adjacent segments
            // prev
            EdgeSegment prev = se.getPrev(index);
            if (prev != null) {
                prev.setB(newSegment.getA());
            }
            // next
            EdgeSegment next = se.getNext(index);
            if (next != null) {
                next.setA(newSegment.getB());
            }

        }
    }

//    public List<EdgeSegment> getLeftAdjacentSegments() {
//        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
//            // TODO: remove nulls from the list of adjacent segments
//            @Override
//            public EdgeSegment apply(SegmentedEdge se) {
//                return se.getLeftAdjacent(EdgeSegment.this);
//            }
//        }));
//    }
//
//    public List<EdgeSegment> getRightAdjacentSegments() {
//        return ImmutableList.copyOf(Iterables.transform(parents, new Function<SegmentedEdge, EdgeSegment>() {
//            @Override
//            public EdgeSegment apply(SegmentedEdge se) {
//                return se.getRightAdjacent(EdgeSegment.this);
//            }
//        }));
//    }

    public boolean sharesAParentWith(EdgeSegment other) {
        List<SegmentedEdge> otherParents = other.getParents();
        for (SegmentedEdge parent : getParents()) {
            if (otherParents.contains(parent)) {
                return true;
            }
        }
        return false;
    }

    public double length() {
        return a.distanceTo(b);
    }

    /**
     * Vector dot product
     */
    public double dot(EdgeSegment other) {
        return (b.x() - a.x()) * (other.b.x() - other.a.x()) +
               (b.y() - a.y()) * (other.b.y() - other.a.y());
    }

    public static double cosOfAngleBetween(EdgeSegment seg1, EdgeSegment seg2) {
        return (seg1.dot(seg2) / seg1.length()) / seg2.length();
    }

    private static final double COS_PI_4 = Math.cos(Math.PI / 4);

    public boolean canBeAggregatedWith(EdgeSegment other) {
        return
                // segments with a fixed point mustn't be aggregated with anything else
                // because it would mean that in the visualization there would be flows
                // (between fixed points) which didn't exist before
                !a.isFixed()  &&  !other.a.isFixed()  &&  !b.isFixed()  &&  !other.b.isFixed()  &&

                // zero-length segments are irrelevant
                length() != 0  &&  other.length() != 0  &&

                // angle is less or equal to PI/4 = 45 grad
                cosOfAngleBetween(this, other) >= COS_PI_4  &&

                // segments of the same edge mustn't be aggregated as well
                !sharesAParentWith(other);
    }

    public EdgeSegment aggregateWith(EdgeSegment other) {
//        return aggregate(Arrays.asList(this, other));
        if (!canBeAggregatedWith(other)) {
            throw new IllegalArgumentException("Segments cannot be aggregated");
        }

        return new EdgeSegment(
                aggregate(a, other.a), aggregate(b, other.b), weight + other.weight,
//                Iterables.concat(getParents(), other.getParents())
                getParentsOf(this, other)
        );
    }

    private FPoint aggregate(FPoint p1, FPoint p2) {
        if (p1.isFixed()  &&  p2.isFixed()  &&  !p1.equals(p2)) {
            throw new IllegalArgumentException("Both points are fixed; cannot aggregate");
        }
        if (p1.isFixed()) {
            return p1;
        } else if (p2.isFixed()) {
            return p2;
        } else {
            return new FPoint(GeomUtils.midpoint(p1.getPoint(), p2.getPoint()), false);
        }
    }

    private static List<SegmentedEdge> getParentsOf(EdgeSegment ... segs) {
        List<SegmentedEdge> union = Lists.newArrayList();
        for (EdgeSegment seg : segs) {
            for (SegmentedEdge se : seg.getParents()) {
                if (!union.contains(se)) {
                    union.add(se);
                }
            }
        }
        return union;
    }

    public boolean checkParentEdgesSegmentConsecutivity() {
        for (SegmentedEdge se : getParents()) {
            if (!se.checkSegmentConsecutivity()) {
                return false;
            }
        }
        return true;
    }

//    public static EdgeSegment aggregate(List<EdgeSegment> segments) {
//        double sumWeight = 0;
//        for (EdgeSegment seg : segments) {
//            sumWeight += seg.getWeight();
//        }
//        SPoint newA = null;
//        SPoint newB = null;
//        boolean aFixed = false;
//        boolean bFixed = false;
//        for (EdgeSegment seg : segments) {
//            if (seg.isaFixed()) {
//                if (newA != null) {
//                    throw new IllegalArgumentException("More than one segments have a fixed A point");
//                }
//                newA = seg.getA();
//                aFixed = true;
//            }
//            if (seg.isbFixed()) {
//                if (newB != null) {
//                    throw new IllegalArgumentException("More than one segments have a fixed B point");
//                }
//                newB = seg.getB();
//                bFixed = true;
//            }
//        }
//        if (newA == null) {
//            newA = GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_A));
//        }
//        if (newB == null) {
//            newB = GeomUtils.centroid(Iterators.transform(segments.iterator(), EdgeSegment.TRANSFORM_TO_B));
//        }
//        return new EdgeSegment(
//                newA,
//                aFixed,
//                newB,
//                bFixed,
//                sumWeight,
//                Iterables.concat(Iterables.transform(segments, TRANSFORM_TO_PARENTS))
//        );
//    }

    public static final Function<EdgeSegment, FPoint> TRANSFORM_TO_A = new Function<EdgeSegment, FPoint>() {
        @Override
        public FPoint apply(EdgeSegment segment) {
            return segment.getA();
        }
    };

    public static final Function<EdgeSegment, FPoint> TRANSFORM_TO_B = new Function<EdgeSegment, FPoint>() {
        @Override
        public FPoint apply(EdgeSegment segment) {
            return segment.getB();
        }
    };

    public static final Function<EdgeSegment, List<SegmentedEdge>> TRANSFORM_TO_PARENTS =
        new Function<EdgeSegment, List<SegmentedEdge>>() {
        @Override
        public List<SegmentedEdge> apply(EdgeSegment segment) {
            return segment.getParents();
        }
    };

    public static final Function<EdgeSegment, Double> TRANSFORM_TO_WEIGHT = new Function<EdgeSegment, Double>() {
        public Double apply(EdgeSegment seg) {
            return seg.getWeight();
        }
    };


    @Override
    public String toString() {
        return "EdgeSegment [" +
                "a=" + a + ", b=" + b +
                ", parents.size=" + parents.size() + ", weight=" + weight +
                "]";
    }

}
