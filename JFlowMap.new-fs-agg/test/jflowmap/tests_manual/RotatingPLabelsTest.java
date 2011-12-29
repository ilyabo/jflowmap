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

package jflowmap.tests_manual;

import java.util.List;

import jflowmap.util.piccolo.PLabel;
import jflowmap.util.piccolo.PNodes;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDebug;
import edu.umd.cs.piccolox.PFrame;

/**
 * @author Ilya Boyandin
 */
public class RotatingPLabelsTest extends PFrame {


  public static void main(String[] args) {
    new RotatingPLabelsTest();
  }

  @Override
  public void initialize() {

    PDebug.debugFullBounds = true;

    final Parent parent = new Parent();
    getCanvas().getLayer().addChild(parent);

    List<PLabel> labels = Lists.newArrayList();
    for (int i = 1; i < 10; i++) {
      PLabel label = new PLabel("Hi " + i);

      parent.addChild(label);
      label.rotateInPlace(-Math.PI * .65 / 2);
      labels.add(label);
      label.addInputEventListener(new PBasicInputEventHandler() {
        @Override
        public void mouseClicked(PInputEvent event) {
          parent.layoutChildren();
        }
      });
    }
    parent.layoutChildren();
  }

  private static class Parent extends PNode {
    @Override
    protected void layoutChildren() {
      PBounds boxb = getBounds();
      double x = boxb.x, y = boxb.y;
      for (PLabel label : PNodes.childrenOfType(this, PLabel.class)) {
        PBounds b = label.getFullBoundsReference();

        double w = b.width;

        label.setOffset(x, y);  // Here setBounds does not work, because the labels
                                // were rotated and invoking setBounds will set the position
                                // without taking the transformation into account.
                                // So in order to move the labels we have
                                // to update the transformation instead of setting the
                                // bounds directly and setOffset does this.

        x += w;
        y += 0;
      }
      super.setBounds(boxb.x, boxb.y, x - boxb.x, y - boxb.y);
    }
  }
}
