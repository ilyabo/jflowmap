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

package jflowmap.util.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class PBoxLayoutNode extends PNode {

  public enum Axis {
    /**
     * Specifies that children should be laid out left to right.
     */
    X {
      @Override double dx(double w, double h, double sp) { return w + sp; }
      @Override double dy(double w, double h, double sp) { return 0; }
//      @Override double size(PNode node) { return node.getFullBoundsReference().width; }
    },
    /**
     * Specifies that children should be laid out top to bottom.
     */
    Y {
      @Override double dx(double w, double h, double sp) { return 0; }
      @Override double dy(double w, double h, double sp) { return h + sp; }
//      @Override double size(PNode node) { return node.getFullBoundsReference().height; }
    };

    abstract double dx(double w, double h, double sp);
    abstract double dy(double w, double h, double sp);
//    abstract double size(PNode node);
  }

  private final Axis axis;
  private final double spacing;
//  private final boolean equalSize;

  public PBoxLayoutNode(Axis axis, double spacing) {
    this.axis = axis;
    this.spacing = spacing;
//    this.equalSize = equalSize;
  }

  @Override
  protected void layoutChildren() {
//    double maxSize = Double.NaN;
//    if (equalSize) {
//      maxSize = maxSize();
//    }

    PBounds boxb = getBounds();
    double x = boxb.x, y = boxb.y;
    for (PNode child : PNodes.childrenOf(this)) {
      PBounds b = child.getFullBoundsReference();
      double w = b.width;
      double h = b.height;
//      if (equalSize) {
//        w = maxSize;
//      }
      child.setBounds(x, y, w, h);
      x += axis.dx(w, h, spacing);
      y += axis.dy(w, h, spacing);
    }

    super.setBounds(boxb.x, boxb.y, x - boxb.x, y - boxb.y);
  }

//  private double maxSize() {
//    double max = Double.NaN;
//    for (PNode child : PNodes.childrenOf(this)) {
//      double s = axis.size(child);
//      if (Double.isNaN(max)  ||  s > max) {
//        max = s;
//      }
//    }
//    return max;
//  }

  @Override
  public boolean setBounds(double x, double y, double width, double height) {
//    double dx = x - getX();
//    double dy = y - getY();
    if (super.setBounds(x, y, width, height)) {
//      PNodes.moveChildrenBy(this, dx, dy);
      layoutChildren();
      return true;
    }
    return false;
  }

}
