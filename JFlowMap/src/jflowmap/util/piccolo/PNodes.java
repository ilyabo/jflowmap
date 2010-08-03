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

import java.util.Iterator;

import jflowmap.util.MathUtils;

import com.google.common.collect.Iterables;

import edu.umd.cs.piccolo.PCamera;
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

  public static final Iterable<PNode> childrenOf(final PNode node) {
      return new Iterable<PNode>() {
        @Override
  //      @SuppressWarnings("unchecked")
        public Iterator<PNode> iterator() {
  //        return node.getChildrenIterator();
          return new Iterator<PNode>() {      // implement an iterator to avoid ConcurrentModificationException
            int nextPos = 0;
            @Override
            public boolean hasNext() {
              return (nextPos < node.getChildrenCount());
            }

            @Override
            public PNode next() {
              return node.getChild(nextPos++);
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };
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
   * @param hsizeProp 0..1 Proportion of the width of the camera bounds which the node should take
   * @param vsizeProp 0..1 Proportion of the height of the camera bounds which the node should take
   */
  public static void adjustStickyNodeToCameraSize(PCamera camera, PNode node,
        double halign, double valign, double hsizeProp, double vsizeProp) {
      PBounds cameraBounds = camera.getGlobalBounds();
      PBounds nodeFullBounds = node.getUnionOfChildrenBounds(null);

      double scale = Math.min(
          hsizeProp * camera.getWidth() / nodeFullBounds.width,
          vsizeProp * camera.getHeight() / nodeFullBounds.height);
      if (scale <= 0) {
        scale = node.getScale();
      } else {
        node.setScale(scale);
      }
      node.setOffset(
          MathUtils.between(
              (cameraBounds.getMinX() - nodeFullBounds.getMinX() * scale),
              (cameraBounds.getMaxX() - nodeFullBounds.getMaxX() * scale),
              (halign + 1) / 2  // make it between 0 and 1
          ),
          MathUtils.between(
              (cameraBounds.getMinY() - nodeFullBounds.getMinY() * scale),
              (cameraBounds.getMaxY() - nodeFullBounds.getMaxY() * scale),
              (valign + 1) / 2  // make it between 0 and 1
          )
      );
    }



}
