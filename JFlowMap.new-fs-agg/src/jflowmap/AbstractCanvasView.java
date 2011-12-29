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
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import jflowmap.util.piccolo.PTypedBasicInputEventHandler;
import jflowmap.views.PTooltip;
import jflowmap.views.VisualCanvas;
import jflowmap.views.flowstrates.TooltipText;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractCanvasView implements IView {

  private final VisualCanvas visualCanvas;
  private final PTooltip tooltipBox;

  public AbstractCanvasView() {
    this.visualCanvas = createVisualCanvas();

    tooltipBox = new PTooltip();
    tooltipBox.setVisible(false);
    tooltipBox.setPickable(false);

    this.visualCanvas.getCamera().addChild(tooltipBox);
  }

  public PCamera getCamera() {
    return getVisualCanvas().getCamera();
  }

  protected VisualCanvas createVisualCanvas() {
    return new VisualCanvas(this);
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
    Component parent = this.getVisualCanvas();
    while (parent != null) {
      parent = parent.getParent();
      if (parent instanceof Frame) {
        return (Frame) parent;
      }
    }
    return null;
  }

  public PTooltip getTooltipBox() {
    return tooltipBox;
  }

  public void showTooltip(PNode node, TooltipText text) {
    showTooltip(getTooltipPosition(node), text);
  }

  public void showTooltip(Point2D pos, TooltipText text) {
    tooltipBox.setText(text.getHeader(), text.getLabels(), text.getValues());
    tooltipBox.showTooltipAt(pos.getX(), pos.getY(), 0, 0);
    tooltipBox.moveToFront();
  }

  protected Point2D getTooltipPosition(PNode node) {
    PBounds bounds = node.getGlobalBounds();
    return new Point2D.Double(bounds.getMaxX(), bounds.getMaxY());
  }

  public void hideTooltip() {
    tooltipBox.setVisible(false);
  }

  protected TooltipText getTooltipTextFor(PNode node) {
    return null;
  }

  public <T extends PNode> PTypedBasicInputEventHandler<T> createTooltipListener(
      final Class<T> nodeClass) {
    return new PTypedBasicInputEventHandler<T>(nodeClass) {
      @Override
      public void mouseEntered(PInputEvent event) {
        T node = node(event);
        TooltipText tt = getTooltipTextFor(node);
        showTooltip(node, tt);
      }

      @Override
      public void mouseExited(PInputEvent event) {
        hideTooltip();
      }
    };
  }

}
