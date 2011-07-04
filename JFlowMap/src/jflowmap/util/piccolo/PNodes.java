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

package jflowmap.util.piccolo;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import jflowmap.util.MathUtils;

import com.google.common.collect.Iterables;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class PNodes {

  private PNodes() {
  }

  @SuppressWarnings("unchecked")
  public static final <T extends PNode> T getAncestorOfType(PNode node, Class<T> klass) {
    PNode parent = node;
    while (parent != null) {
      if (parent != null  &&  klass.isAssignableFrom(parent.getClass())) {
        return (T) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  public static PNode getRootAncestor(PNode node) {
    PNode parent = node.getParent();
    while (parent.getParent() != null) {
      parent = parent.getParent();
    }
    return parent;
  }
  /*
   * This method will return a non-fail-fast iterator: it won't fail if the collection is modified
   * during the iteration process
   * (see {@link ConcurrentModificationException})
   */
  public static final Iterable<PNode> childrenOf(final PNode node) {
      return new Iterable<PNode>() {
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<PNode> iterator() {
          return node.getChildrenIterator();
//          return new Iterator<PNode>() {      // implement an iterator to avoid ConcurrentModificationException
//            int nextPos = 0;
//            @Override
//            public boolean hasNext() {
//              return (nextPos < node.getChildrenCount());
//            }
//
//            @Override
//            public PNode next() {
//              return node.getChild(nextPos++);
//            }
//
//            @Override
//            public void remove() {
//              throw new UnsupportedOperationException();
//            }
//          };
        }
      };
    }

  public static final <T extends PNode> Iterable<T> childrenOfType(PNode node, Class<T> type) {
    return Iterables.filter(childrenOf(node), type);
  }

  public static final PNode moveTo(PNode node, double x, double y) {
    node.offset(x - node.getX(), y - node.getY());
    return node;
  }

  public static final int indexOfChild(PNode parent, PNode child) {
    for (int i = 0, numChildren = parent.getChildrenCount(); i < numChildren; i++) {
      if (parent.getChild(i) == child) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param halign -1 - stick to left side, 0 - center, 1 - stick to right side
   * @param valign -1 - stick to top side, 0 - middle, 1 - stick to bottom side
   * @param hsizeProportion 0..1 Proportion of the width of the bounds which the node should take
   * @param vsizeProportion 0..1 Proportion of the height of the bounds which the node should take
   */
  public static void alignNodeInBounds_byOffsetAndScale(PNode node, Rectangle2D bounds,
        double halign, double valign, double hsizeProportion, double vsizeProportion) {

      PBounds nodeFullBounds = node.getUnionOfChildrenBounds(null);

      double scale = Math.min(
          hsizeProportion * bounds.getWidth() / nodeFullBounds.width,
          vsizeProportion * bounds.getHeight() / nodeFullBounds.height);
      if (scale <= 0) {
        scale = node.getScale();
      } else {
        node.setScale(scale);
      }
      node.setOffset(
          MathUtils.between(
              (bounds.getMinX() - nodeFullBounds.getMinX() * scale),
              (bounds.getMaxX() - nodeFullBounds.getMaxX() * scale),
              (halign + 1) / 2  // make it between 0 and 1
          ),
          MathUtils.between(
              (bounds.getMinY() - nodeFullBounds.getMinY() * scale),
              (bounds.getMaxY() - nodeFullBounds.getMaxY() * scale),
              (valign + 1) / 2  // make it between 0 and 1
          )
      );
    }


  /**
   *
   * @param bounds Bounds to align the node within.
   * @param halign PCanvas.LEFT_ALIGNMENT or RIGHT_ALIGNMENT
   * @param valign PCanvas.TOP_ALIGNMENT or BOTTOM_ALIGNMENT
   * @param hsizePropoption 0..1 Proportion of the width of the bounds which the node should take
   * @param vsizeProportion 0..1 Proportion of the height of the bounds which the node should take
   */
  public static void alignNodeInBounds_bySetBounds(PNode node, Rectangle2D bounds,
      double halign, double valign, double hsizeProportion, double vsizeProportion) {

    double width = bounds.getWidth() * hsizeProportion;
    double height = bounds.getHeight() * vsizeProportion;

    double x = MathUtils.between(bounds.getMinX(), bounds.getMaxX() - width, halign);
    double y = MathUtils.between(bounds.getMinY(), bounds.getMaxY() - height, valign);

    node.setBounds(x, y, width, height);
  }

  public static void setPosition(PNode node, double x, double y) {
    setPosition(node, x, y, false);
  }

  /**
   * @param deep Offset all children recursively
   */
  public static void setPosition(PNode node, double x, double y, boolean deep) {
    if (deep) {
      double dx = x - node.getX();
      double dy = y - node.getY();
      moveChildrenBy(node, dx, dy);
    }
    node.setBounds(x, y, node.getWidth(), node.getHeight());
  }

  public static void moveChildrenBy(PNode node, double dx, double dy) {
    for (PNode child : PNodes.childrenOf(node)) {
      setPosition(child, child.getX() + dx, child.getY() + dy, true);
    }
  }

  public static void setPosition(PNode node, Point2D pos) {
    node.setBounds(pos.getX(), pos.getY(), node.getWidth(), node.getHeight());
  }

  public static <T extends PNode> Rectangle2D fullBoundsOf(Iterable<T> nodes) {
    Iterator<T> it = nodes.iterator();
    if (!it.hasNext()) {
      return new Rectangle2D.Double();  // empty
    }
    Rectangle2D union = it.next().getFullBoundsReference();
    while (it.hasNext()) {
      union.add(it.next().getFullBoundsReference());
    }
    return union;
  }

  /**
   * @param halign PCanvas.LEFT_ALIGNMENT or RIGHT_ALIGNMENT
   * @param valign PCanvas.TOP_ALIGNMENT or BOTTOM_ALIGNMENT
   */
  public static void anchorNodeToBounds(PNode node, PBounds bounds,
      float halign, float valign, int padx, int pady) {
    PBounds b = node.getFullBoundsReference();
    PNodes.setPosition(node,
        bounds.x + (bounds.width - b.width) * halign - padx * (halign * 2 - 1),
        bounds.y + (bounds.height - b.height) * valign - pady * (valign * 2 - 1)
        );
  }

  /**
   * @param halign PCanvas.LEFT_ALIGNMENT or RIGHT_ALIGNMENT
   * @param valign PCanvas.TOP_ALIGNMENT or BOTTOM_ALIGNMENT
   */
  public static void anchorNodeToBoundsOf(PNode node, PNode anchorToNode,
      float halign, float valign, int padx, int pady) {
    anchorNodeToBounds(node, anchorToNode.getBoundsReference(), halign, valign, padx, pady);
  }

}
