package jflowmap.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jflowmap.geom.FPoint;

import org.junit.Test;

import prefuse.data.tuple.TableEdge;

public class EdgeSegmentTest {

    private static final double EPS = 1e-7;

    @Test
    public void testCanBeAggregatedWith() {
        SegmentedEdge edge = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment seg1, seg2;

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, false) , 2.0, edge);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // seg1 and seg2 share the same parent edge


        seg1 = new EdgeSegment(new FPoint(0, 1, true), new FPoint(1, 0, false) , 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, true), new FPoint(1.5, 0, false) , 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // both A points are fixed


        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, true), 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // both B points are fixed


        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1.5, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, true), 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // both B points are fixed; but also equal

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1.5, 0, true), 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, true), new FPoint(1.5, 0, false) , 2.0, edge2);
        assertTrue(!seg1.canBeAggregatedWith(seg2));  // cross-points fixed

    }

    @Test
    public void testAggregateWith() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment seg1, seg2, agg;

        seg1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge1);
        seg2 = new EdgeSegment(new FPoint(0, 1.5, false) , new FPoint(1.5, 0, false) , 2.0, edge2);
        agg = seg1.aggregateWith(seg2);
        assertEquals(3.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(0, 1.25, false), agg.getA());
        assertEquals(new FPoint(1.25, 0, false), agg.getB());
    }

    @Test
    public void testReplaceWithAggregate() {
        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());
        EdgeSegment
        seg1_1,seg1_2,seg1_3,
        seg2_1,seg2_2,seg2_3,
        agg;

        seg1_1 = new EdgeSegment(new FPoint(0, 1, false) , new FPoint(1, 0, false) , 1.0, edge1);
        seg1_2 = new EdgeSegment(new FPoint(1, 0, false) , new FPoint(2, 0, false) , 1.0, edge1);
        seg1_3 = new EdgeSegment(new FPoint(2, 0, false) , new FPoint(3, 0, false) , 1.0, edge1);
        edge1.addConsecutiveSegment(seg1_1);
        edge1.addConsecutiveSegment(seg1_2);
        edge1.addConsecutiveSegment(seg1_3);

        seg2_1 = new EdgeSegment(new FPoint(0.5, 1, false) , new FPoint(1, 1, false) , 1.0, edge2);
        seg2_2 = new EdgeSegment(new FPoint(1, 1, false) , new FPoint(1.5, 1, false) , 2.0, edge2);
        seg2_3 = new EdgeSegment(new FPoint(1.5, 1, false) , new FPoint(3, 2, false) , 1.0, edge2);
        edge2.addConsecutiveSegment(seg2_1);
        edge2.addConsecutiveSegment(seg2_2);
        edge2.addConsecutiveSegment(seg2_3);


        // aggregate middle segments
        agg = seg1_2.aggregateWith(seg2_2);
        assertEquals(3.0, agg.getWeight(), EPS);
        assertEquals(new FPoint(1, 0.5, false), agg.getA());
        assertEquals(new FPoint(1.75, 0.5, false), agg.getB());


        // check parents of the aggregate
        assertEquals(2, agg.getParents().size());
        assertTrue(agg.getParents().contains(edge1));
        assertTrue(agg.getParents().contains(edge2));


        // replace middle segments with the aggregate
        seg1_2.replaceWith(agg);
        seg2_2.replaceWith(agg);


        // test that adjacent segments were properly changed
        assertEquals(new FPoint(1, 0.5, false), seg1_1.getB());
        assertEquals(new FPoint(1, 0.5, false), seg2_1.getB());

        assertEquals(new FPoint(1.75, 0.5, false), seg1_3.getA());
        assertEquals(new FPoint(1.75, 0.5, false), seg2_3.getA());


        // check prev/next
        assertEquals(agg, edge1.getNext(seg1_1));
        assertEquals(agg, edge1.getPrev(seg1_3));

        assertEquals(agg, edge2.getNext(seg2_1));
        assertEquals(agg, edge2.getPrev(seg2_3));

        assertEquals(3.0, edge2.getNext(seg2_1).getWeight(), EPS);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testAggregateWith_zeroLength() {

        SegmentedEdge edge1 = new SegmentedEdge(new TableEdge());
        SegmentedEdge edge2 = new SegmentedEdge(new TableEdge());

        EdgeSegment seg1_1 = new EdgeSegment(
                new FPoint(13.377183611771455, 52.517694721775015, false),
                new FPoint(13.377922829504366, 52.517377533672196, false), 2.0);
        edge1.addConsecutiveSegment(seg1_1);

        EdgeSegment seg2_1 = new EdgeSegment(
                // zero-length
                new FPoint(13.3760237867868, 52.5185965900901, false),
                new FPoint(13.3760237867868, 52.5185965900901, false), 2.0);
        edge2.addConsecutiveSegment(seg2_1);

        seg1_1.aggregateWith(seg2_1);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWith_impossibleToReplace() {

        SegmentedEdge edge = new SegmentedEdge(new TableEdge());

        EdgeSegment seg1 = new EdgeSegment(
                new FPoint(13.377183611771455, 52.517694721775015, true),  // fixed -> cannot replace
                new FPoint(13.377922829504366, 52.517377533672196, false), 2.0);
        edge.addConsecutiveSegment(seg1);

        EdgeSegment seg2 = new EdgeSegment(
                new FPoint(13.3760237867868, 52.5185965900901, false),
                new FPoint(13.3760237867868, 52.5185965900901, false), 2.0);

        seg1.replaceWith(seg2);
    }


    @Test
    public void testReplaceWith_newSegmentsPointBecomesFixed() {

        SegmentedEdge edge = new SegmentedEdge(new TableEdge());

        EdgeSegment oldSegment = new EdgeSegment(
                new FPoint(13.377183611771455, 52.517694721775015, true),  // fixed, but ...
                new FPoint(13.377922829504366, 52.517377533672196, true), 2.0);
        edge.addConsecutiveSegment(oldSegment);

        EdgeSegment newSegment = new EdgeSegment(
                new FPoint(13.377183611771455, 52.517694721775015, false),  // ... this point is the same -> possible to replace, and ...
                new FPoint(13.377922829504366, 52.517377533672196, false), 2.0);

        oldSegment.replaceWith(newSegment);

        assertTrue(newSegment.getA().isFixed());      // ... becomes fixed
        assertTrue(newSegment.getB().isFixed());
    }


    @Test
    public void testDot() {
        assertEquals(0,
                    new EdgeSegment(new FPoint(0, 0, false) , new FPoint(2, 0, false) , 1.0)
                    .dot(new EdgeSegment(new FPoint(0, 0, false) , new FPoint(0, 1, false) , 1.0)),
        EPS);
        assertEquals(1.0,
                    new EdgeSegment(new FPoint(0, 0, false) , new FPoint(0, 1, false) , 1.0)
                    .dot(new EdgeSegment(new FPoint(0, 0, false) , new FPoint(0, 1, false) , 1.0)),
        EPS);
    }

    @Test
    public void test_cosOfAngleBetween() {
        assertEquals(Math.cos(Math.PI/4),
                    EdgeSegment.cosOfAngleBetween(
                        new EdgeSegment(new FPoint(1, 1, false) , new FPoint(2, 2, false) , 1.0),
                        new EdgeSegment(new FPoint(1, 2, false) , new FPoint(1, 3, false) , 1.0)
                    ),
        EPS);
        assertEquals(0,
                EdgeSegment.cosOfAngleBetween(
                    new EdgeSegment(new FPoint(1, 1, false) , new FPoint(3, 1, false) , 1.0),
                    new EdgeSegment(new FPoint(1, 2, false) , new FPoint(1, 3, false) , 1.0)
                ),
        EPS);

        assertEquals(1.0,
                EdgeSegment.cosOfAngleBetween(
                    new EdgeSegment(new FPoint(1, 1, false) , new FPoint(1, 2, false) , 1.0),
                    new EdgeSegment(new FPoint(1, 2, false) , new FPoint(1, 3, false) , 1.0)
                ),
        EPS);

        assertEquals(-1.0,
                EdgeSegment.cosOfAngleBetween(
                    new EdgeSegment(new FPoint(0, 0, false) , new FPoint(2, 0, false) , 1.0),
                    new EdgeSegment(new FPoint(0, 0, false) , new FPoint(-2, 0, false) , 1.0)
                ),
        EPS);

    }

}
