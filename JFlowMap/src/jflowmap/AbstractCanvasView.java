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

package jflowmap;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JComponent;

import jflowmap.util.piccolo.PNodes;
import jflowmap.views.Tooltip;
import jflowmap.views.VisualCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractCanvasView implements IView {

  private final VisualCanvas visualCanvas;
  private final Tooltip tooltipBox;

  public AbstractCanvasView() {
    this.visualCanvas = createVisualCanvas();

    tooltipBox = new Tooltip();
    tooltipBox.setVisible(false);
    tooltipBox.setPickable(false);

    this.visualCanvas.getCamera().addChild(tooltipBox);
  }

  protected VisualCanvas createVisualCanvas() {
    return new VisualCanvas();
  }

  public JComponent getViewComponent() {
    return getVisualCanvas();
  }

  @Override
  public JComponent getControls() {
    return null;
  }

  public VisualCanvas getVisualCanvas() {
    return visualCanvas;
  }

  public void fitInView() {
    visualCanvas.fitChildrenInCameraView();
  }

  public Frame getParentFrame() {
    Component parent = this.getViewComponent();
    while (parent != null) {
      parent = parent.getParent();
      if (parent instanceof Frame) {
        return (Frame) parent;
      }
    }
    return null;
  }

  private void showTooltip(PNode node, String header, String labels, String values) {
    PBounds bounds = node.getGlobalBounds();
    tooltipBox.setText(header, labels, values);
    tooltipBox.showTooltipAt(bounds.getMaxX(), bounds.getMaxY(), 0, 0);
    tooltipBox.moveToFront();
  }

  private void hideTooltip() {
    tooltipBox.setVisible(false);
  }

  protected String getTooltipHeaderFor(PNode node) {
    return null;
  }

  protected String getTooltipLabelsFor(PNode node) {
    return null;
  }

  protected String getTooltipValuesFor(PNode node) {
    return null;
  }

  public PInputEventListener createTooltipListener(final Class<? extends PNode> nodeClass) {
    return new PBasicInputEventHandler() {
      @Override
      public void mouseEntered(PInputEvent event) {
        PNode node = PNodes.getAncestorOfType(event.getPickedNode(), nodeClass);
        if (node != null) {
          showTooltip(node, getTooltipHeaderFor(node),
              getTooltipLabelsFor(node), getTooltipValuesFor(node));
        }
      }

      @Override
      public void mouseExited(PInputEvent event) {
        PNode node = PNodes.getAncestorOfType(event.getPickedNode(), nodeClass);
        if (node != null) {
          hideTooltip();
        }
      }
    };
  }

}
