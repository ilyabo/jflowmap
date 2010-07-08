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

import jflowmap.views.VisualCanvas;

/**
 * @author Ilya Boyandin
 */
public abstract class AbstractCanvasView implements IView {

  private final VisualCanvas visualCanvas;

  public AbstractCanvasView() {
    this.visualCanvas = createVisualCanvas();
  }

  protected VisualCanvas createVisualCanvas() {
    return new VisualCanvas();
  }

  public VisualCanvas getViewComponent() {
    return visualCanvas;
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

}
