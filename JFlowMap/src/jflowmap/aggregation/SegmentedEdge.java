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

package jflowmap.aggregation;

import java.util.Collections;
import java.util.List;

import jflowmap.util.Pair;
import prefuse.data.Edge;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class SegmentedEdge {

  private final List<EdgeSegment> segments;
  private final Edge edge;

  public SegmentedEdge(Edge edge) {
    this.edge = edge;
    this.segments = Lists.newArrayList();
  }

  public Edge getEdge() {
    return edge;
  }

  public List<EdgeSegment> getSegments() {
    return Collections.unmodifiableList(segments);
  }

  /**
   * Adds {@code segment} to the edge. The {@code segment} must be
   * consecutive to the last segment of the edge (its start point
   * must be the same as the end point of the last segment of the edge).
   *
   * @throws IllegalArgumentException If the segment is not consecutive
   *     to the last segment of the edge.
   */
  public void addConsecutiveSegment(EdgeSegment segment) {
    if (segments.size() > 0) {
      if (!segment.isConsecutiveFor(Iterables.getLast(segments))) {
        throw new IllegalArgumentException("Segments are not consecutive");
      }
    }
//    System.out.println("Add segment " + System.identityHashCode(segment) + " to edge " + System.identityHashCode(this));
//    if (!segment.getParents().contains(this)) {
//      throw new IllegalArgumentException();
//    }
    segments.add(segment);
    segment.addParent(this);
  }

  void setSegment(int index, EdgeSegment newSegment) {
    segments.set(index, newSegment);
  }

  /**
   * Don't call this method directly. Use {@link EdgeSegment#replaceWith} instead.
   */
  /*
  void replaceSegment(EdgeSegment oldSegment, EdgeSegment newSegment) {
    int index = indexOf(oldSegment);
    if (index < 0) {
      throw new IllegalArgumentException();
    }
    segments.set(index, newSegment);
    newSegment.addParent(this);

    // update adjacent segments
    // prev
    EdgeSegment prev = getPrev(index);
    if (prev != null) {
      FPoint newB = newSegment.getA();
      if (!prev.getB().equals(newB)) {
        prev.setB(newB);
      }
    }
    // next
    EdgeSegment next = getNext(index);
    if (next != null) {
      FPoint newA = newSegment.getB();
      if (!next.getA().equals(newA)) {
        next.setA(newA);
      }
    }
  }
  */

  /**
   * Will throw IllegalArgumentException if edge is not a part of the edge.
   */
  public Pair<EdgeSegment, EdgeSegment> getPrevAndNext(EdgeSegment seg) {
    return getPrevAndNext(indexOf(seg));
  }

  /**
   * Will throw IllegalArgumentException if edge is not a part of the edge.
   */
  public EdgeSegment getPrev(EdgeSegment seg) {
    return getPrev(indexOf(seg));
  }

  /**
   * Will throw IllegalArgumentException if edge is not a part of the edge.
   */
  public EdgeSegment getNext(EdgeSegment seg) {
    return getNext(indexOf(seg));
  }

  Pair<EdgeSegment, EdgeSegment> getPrevAndNext(int index) {
    return Pair.of(getPrev(index), getNext(index));
  }

  EdgeSegment getPrev(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Segment not found in the edge: " + index);
    }
    EdgeSegment prev;
    if (index > 0) {
      prev = segments.get(index - 1);
    } else {
      prev = null;
    }
    return prev;
  }

  EdgeSegment getNext(int index) {
    if (index < 0) {
      throw new IllegalArgumentException("Segment not found in the edge: " + index);
    }
    int size = segments.size();
    EdgeSegment next;
    if (index < size - 1) {
      next = segments.get(index + 1);
    } else {
      next = null;
    }
    return next;
  }

  int indexOf(EdgeSegment segment) {
    int index = -1;
    for (int i = 0, size = segments.size(); i < size; i++) {
      if (segments.get(i) == segment) {
        index = i;
      }
    }
    return index;
  }

  public boolean checkSegmentConsecutivity() {
    if (segments.size() == 0) {
      return true;
    }
    EdgeSegment prev = segments.get(0);
    for (int i = 1, size = segments.size(); i < size; i++) {
      EdgeSegment seg = segments.get(i);
      if (!seg.isConsecutiveFor(prev)) {
        return false;
      }
      prev = seg;
    }
    return true;
  }

//  public EdgeSegment getLeftAdjacent(EdgeSegment segment) {
//    EdgeSegment prev = null;
//    for (EdgeSegment seg : segments) {
//      if (seg.equals(segment)) {
//        return prev;
//      }
//    }
//    return null;
//  }
//
//  public EdgeSegment getRightAdjacent(EdgeSegment segment) {
//    for (Iterator<EdgeSegment> it = segments.iterator(); it.hasNext(); ) {
//      EdgeSegment seg = it.next();
//      if (seg.equals(segment)) {
//        if (it.hasNext()) {
//          return it.next();
//        } else {
//          return null;
//        }
//      }
//    }
//    return null;
//  }

  @Override
  public String toString() {
    return "SegmentedEdge [edge=" + edge + ", segments.size=" + segments.size() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((edge == null) ? 0 : edge.hashCode());
    result = prime * result + ((segments == null) ? 0 : segments.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SegmentedEdge other = (SegmentedEdge) obj;
    if (edge == null) {
      if (other.edge != null)
        return false;
    } else if (!edge.equals(other.edge))
      return false;
    if (segments == null) {
      if (other.segments != null)
        return false;
    } else if (!segments.equals(other.segments))
      return false;
    return true;
  }

}