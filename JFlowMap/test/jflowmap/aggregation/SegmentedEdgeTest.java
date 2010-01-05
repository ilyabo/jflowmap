package jflowmap.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jflowmap.geom.FPoint;

import org.junit.Test;

import prefuse.data.tuple.TableEdge;

import com.google.common.collect.Iterables;


public class SegmentedEdgeTest {

    private static final double EPS = 1e-7;

    @Test
    public void testReplaceSegment_withAggregate() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment
            seg1_1,seg1_2,seg1_3,
            seg2_1,seg2_2,seg2_3,
            agg;

        seg1_1 = new EdgeSegment(new FPoint(0, 1, false), new FPoint(1, 0, false), 1.0, edge1);
        seg1_2 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 1.0, edge1);
        seg1_3 = new EdgeSegment(new FPoint(2, 0, false), new FPoint(3, 0, false), 1.0, edge1);
        edge1.addConsecutiveSegment(seg1_1);
        edge1.addConsecutiveSegment(seg1_2);
        edge1.addConsecutiveSegment(seg1_3);

        seg2_1 = new EdgeSegment(new FPoint(0.5, 1, false), new FPoint(1, 1, false), 1.0, edge2);
        seg2_2 = new EdgeSegment(new FPoint(1, 1, false), new FPoint(1.5, 1, false), 1.0, edge2);
        seg2_3 = new EdgeSegment(new FPoint(1.5, 1, false), new FPoint(3, 2, false), 1.0, edge2);
        edge2.addConsecutiveSegment(seg2_1);
        edge2.addConsecutiveSegment(seg2_2);
        edge2.addConsecutiveSegment(seg2_3);


        agg = seg1_2.aggregateWith(seg2_2);
        assertEquals(2.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(1, 0.5, false), agg.getA());
        assertEquals(new FPoint(1.75, 0.5, false), agg.getB());


        // replace
//        edge1.replaceSegment(seg1_2, agg);
//        edge2.replaceSegment(seg2_2, agg);
        seg1_2.replaceWith(agg);
        seg2_2.replaceWith(agg);


        // test that adjacent segments were properly changed
        assertEquals(new FPoint(1, 0.5, false), seg1_1.getB());
        assertEquals(new FPoint(1, 0.5, false), seg2_1.getB());

        assertEquals(new FPoint(1.75, 0.5, false), seg1_3.getA());
        assertEquals(new FPoint(1.75, 0.5, false), seg2_3.getA());
    }


    @Test
    public void testReplaceSegment_replaceOneOfSubflows() {

        /*
                      edge1
                        *
                       /
                      1
                     /
  edge1,2,3 *===0===*--2---* edge2            replacing #2 with #5
                     \
                      3
                       \
                        *
                        edge3

         */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge3 = new SegmentedEdge(new TableEdge());

        EdgeSegment seg0 = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 3.0);
        edge1.addConsecutiveSegment(seg0);
        edge2.addConsecutiveSegment(seg0);
        edge3.addConsecutiveSegment(seg0);
        assertEquals(3, seg0.getParents().size());

        EdgeSegment seg1 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, -1, false), 1.0);
        edge1.addConsecutiveSegment(seg1);

        EdgeSegment seg2 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 1.0);
        edge2.addConsecutiveSegment(seg2);

        EdgeSegment seg3 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, +1, false), 1.0);
        edge3.addConsecutiveSegment(seg3);


        // check prev/next
        assertEquals(seg0, edge1.getPrev(seg1)); assertEquals(seg1, edge1.getNext(seg0));
        assertEquals(seg0, edge2.getPrev(seg2)); assertEquals(seg2, edge2.getNext(seg0));
        assertEquals(seg0, edge3.getPrev(seg3)); assertEquals(seg3, edge3.getNext(seg0));

        // check parents
        assertEquals(edge1, Iterables.getOnlyElement(seg1.getParents()));
        assertEquals(edge2, Iterables.getOnlyElement(seg2.getParents()));
        assertEquals(edge3, Iterables.getOnlyElement(seg3.getParents()));


        // REPLACE seg2 with seg5
        EdgeSegment seg5 = new EdgeSegment(new FPoint(1.1, 0.1, false), new FPoint(2.1, 0.1, false), 2.5);
        seg2.replaceWith(seg5);


        // check prev/next
        try {
            edge2.getPrev(seg2);
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // this is expected
        }

        assertEquals(seg0, edge1.getPrev(seg1)); assertEquals(seg1, edge1.getNext(seg0));
        assertEquals(seg0, edge2.getPrev(seg5)); assertEquals(seg5, edge2.getNext(seg0));
        assertEquals(seg0, edge3.getPrev(seg3)); assertEquals(seg3, edge3.getNext(seg0));


        // check parents
        assertEquals(edge1, Iterables.getOnlyElement(seg1.getParents()));
        assertEquals(edge2, Iterables.getOnlyElement(seg5.getParents()));
        assertEquals(edge3, Iterables.getOnlyElement(seg3.getParents()));


        // check that all segments were updated properly
        assertEquals(seg5.getA(),  seg0.getB());
        assertEquals(seg5.getA(),  seg1.getA());
        assertEquals(seg5.getA(),  seg3.getA());

    }




    @Test
    public void testReplaceSegment_replaceBigFlow() {

        /*
                      edge1
                        *
                       /
                      1
                     /
    edge1,2 *===0===*         replacing #0 with #5
                     \
                      2
                       \
                        *
                        edge2

         */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment seg0 = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 3.0);
        edge1.addConsecutiveSegment(seg0);
        edge2.addConsecutiveSegment(seg0);
        assertEquals(2, seg0.getParents().size());

        EdgeSegment seg1 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, -1, false), 1.0);
        edge1.addConsecutiveSegment(seg1);

        EdgeSegment seg2 = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, +1, false), 1.0);
        edge2.addConsecutiveSegment(seg2);


        // REPLACE seg2 with seg5
        EdgeSegment seg5 = new EdgeSegment(new FPoint(0.1, 0.1, false), new FPoint(1.1, 0.1, false), 4.0);
        seg0.replaceWith(seg5);


        assertEquals(seg5.getB(),  seg1.getA());
        assertEquals(seg5.getB(),  seg2.getA());

        assertEquals(seg5, edge1.getPrev(seg1)); assertEquals(seg1, edge1.getNext(seg5));
        assertEquals(seg5, edge2.getPrev(seg2)); assertEquals(seg2, edge2.getNext(seg5));
    }




    @Test
    public void testReplaceSegment_replaceOneOfCommonSeq() {
        /*


    edge1,2 *===A===*===B===*       replacing B with D


         */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment segA = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 2.0);
        EdgeSegment segB = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 2.0);

        edge1.addConsecutiveSegment(segA);
        edge1.addConsecutiveSegment(segB);

        edge2.addConsecutiveSegment(segA);
        edge2.addConsecutiveSegment(segB);


        EdgeSegment segD = new EdgeSegment(new FPoint(1.1, 0.1, false), new FPoint(1.9, -0.1, false), 2.1);
        segB.replaceWith(segD);

        assertEquals(segA.getB(), segD.getA());
        assertEquals(segD, edge1.getNext(segA));
        assertEquals(segA, edge1.getPrev(segD));

        assertEquals(segD, edge2.getNext(segA));
        assertEquals(segA, edge2.getPrev(segD));
    }

    @Test
    public void testReplaceSegment_replaceOneOfCommonSeq_2sided() {
        /*


    edge1,2 *===A===*===B===*===C===*       replacing B with D


        */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment segA = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 2.0);
        EdgeSegment segB = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 2.0);
        EdgeSegment segC = new EdgeSegment(new FPoint(2, 0, false), new FPoint(3, 0, false), 2.0);

        edge1.addConsecutiveSegment(segA);
        edge1.addConsecutiveSegment(segB);
        edge1.addConsecutiveSegment(segC);

        edge2.addConsecutiveSegment(segA);
        edge2.addConsecutiveSegment(segB);
        edge2.addConsecutiveSegment(segC);


        EdgeSegment segD = new EdgeSegment(new FPoint(1.1, 0.1, false), new FPoint(1.9, -0.1, false), 2.1);
        segB.replaceWith(segD);


        assertEquals(segA.getB(), segD.getA());
        assertEquals(segD.getB(), segC.getA());

        assertEquals(2, segD.getParents().size());
        assertTrue(segD.getParents().contains(edge1));
        assertTrue(segD.getParents().contains(edge2));

        assertEquals(segD, edge1.getNext(segA));
        assertEquals(segA, edge1.getPrev(segD));
        assertEquals(segD, edge1.getPrev(segC));
        assertEquals(segC, edge1.getNext(segD));

        assertEquals(segD, edge2.getNext(segA));
        assertEquals(segA, edge2.getPrev(segD));
        assertEquals(segD, edge2.getPrev(segC));
        assertEquals(segC, edge2.getNext(segD));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testReplaceSegment_addNonConsecutive() {
        SegmentedEdge edge = new SegmentedEdge(new TableEdge());
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 1.0));
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(2, 0, false), new FPoint(2, 0, false), 1.0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testReplaceSegment_addFixedSegmentAfterNonFixed() {
        SegmentedEdge edge = new SegmentedEdge(new TableEdge());
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 1.0));
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(1, 0, true), new FPoint(2, 0, false), 1.0));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testReplaceSegment_addNonFixedSegmentAfterFixed() {
        SegmentedEdge edge = new SegmentedEdge(new TableEdge());
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, true), 1.0));
        edge.addConsecutiveSegment(new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 1.0));
    }


    @Test
    public void testReplaceSegment_replaceWithFixed() {
        /*


           edge1,2 *===A===*===B===*===C===*       replacing B with D where both points of D are fixed


        */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment segA = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, false), 2.0);
        EdgeSegment segB = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 2.0);
        EdgeSegment segC = new EdgeSegment(new FPoint(2, 0, false), new FPoint(3, 0, false), 2.0);

        edge1.addConsecutiveSegment(segA);
        edge1.addConsecutiveSegment(segB);
        edge1.addConsecutiveSegment(segC);

        edge2.addConsecutiveSegment(segA);
        edge2.addConsecutiveSegment(segB);
        edge2.addConsecutiveSegment(segC);

        assertTrue(!segA.getB().isFixed());

        EdgeSegment segD = new EdgeSegment(new FPoint(1.1, 0.1, true), new FPoint(1.9, -0.1, true), 2.1);
        segB.replaceWith(segD);

        assertTrue(segA.getB().isFixed());
        assertTrue(segC.getA().isFixed());
    }

    @Test
    public void testReplaceSegment_replaceFixed() {
        /*

            edge1,2 *===A===*===B===*===C===*       replacing B with D where both points of B are fixed


        */
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment segA = new EdgeSegment(new FPoint(0, 0, false), new FPoint(1, 0, true), 2.0);
        EdgeSegment segB = new EdgeSegment(new FPoint(1, 0, true), new FPoint(2, 0, true), 2.0);
        EdgeSegment segC = new EdgeSegment(new FPoint(2, 0, true), new FPoint(3, 0, false), 2.0);

        edge1.addConsecutiveSegment(segA);
        edge1.addConsecutiveSegment(segB);
        edge1.addConsecutiveSegment(segC);

        edge2.addConsecutiveSegment(segA);
        edge2.addConsecutiveSegment(segB);
        edge2.addConsecutiveSegment(segC);

        try {
            EdgeSegment segD = new EdgeSegment(new FPoint(1.1, 0.1, false), new FPoint(1.9, -0.1, false), 2.1);
            segB.replaceWith(segD);
            fail("IllegalArgumentException was expected");
        } catch (IllegalArgumentException iae) {
            // ok
        }

        EdgeSegment segD = new EdgeSegment(new FPoint(1, 0, false), new FPoint(2, 0, false), 2.1);  // a and b equal to segB's
        segB.replaceWith(segD);

        assertTrue(segD.getA().isFixed());
        assertTrue(segD.getB().isFixed());

        assertTrue(segA.getB().isFixed());
        assertTrue(segC.getA().isFixed());
    }
}
