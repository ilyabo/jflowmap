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
      @Override public double dx(double width, double height) { return width; }
      @Override public double dy(double width, double height) { return 0; }
    },
    /**
     * Specifies that children should be laid out top to bottom.
     */
    Y {
      @Override public double dx(double width, double height) { return 0; }
      @Override public double dy(double width, double height) { return height; }
    };

    public abstract double dx(double width, double height);
    public abstract double dy(double width, double height);
  }

  private final Axis axis;

  public PBoxLayoutNode(Axis axis) {
    this.axis = axis;
  }

  @Override
  protected void layoutChildren() {
    double xOffset = 0, yOffset = 0;
    for (PNode child : PNodes.childrenOf(this)) {
      PBounds fb = child.getFullBoundsReference();
      double width = fb.getWidth();
      double height = fb.getHeight();
      xOffset += axis.dx(width, height);
      yOffset += axis.dy(width, height);
      child.setBounds(xOffset, yOffset, width, height);
    }
  }

}
