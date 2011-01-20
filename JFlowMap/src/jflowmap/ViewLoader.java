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

package jflowmap;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import jflowmap.data.ViewConfig;
import at.fhj.utils.misc.FileUtils;
import foxtrot.Task;
import foxtrot.Worker;

/**
 * @author Ilya Boyandin
 */
public class ViewLoader {

  public static final ImageIcon LOADING_ICON = new ImageIcon(
      JFlowMapMain.class.getResource("resources/loading.gif"));

  public static void loadView(final String viewConfigLocation, final Container parent)
    throws Exception
  {
    JLabel loadingLabel = new JLabel(" Opening '" +
        FileUtils.getFilename(viewConfigLocation) + "'...", LOADING_ICON, JLabel.CENTER);
    parent.add(loadingLabel);

    IView view = null;
    view = (IView) Worker.post(new Task() {
      @Override
      public Object run() throws Exception {
        ViewConfig config = ViewConfig.load(viewConfigLocation);
        return config.createView();
      }
    });

    parent.remove(loadingLabel);

    boolean isViewEmpty = true;
    if (view != null) {

      JComponent controls = view.getControls();
      if (controls != null) {
        parent.add(controls, view.getControlsLayoutConstraint());
      }

      JComponent viewComp = view.getViewComponent();
      if (viewComp != null) {
        parent.add(viewComp, BorderLayout.CENTER);
        isViewEmpty = false;
      }
    }
    if (isViewEmpty) {
      parent.add(new JLabel("No view", JLabel.CENTER), BorderLayout.CENTER);
    }
    parent.validate();
  }

}
