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
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import jflowmap.data.ViewConfig;
import jflowmap.util.piccolo.PButton;
import jflowmap.views.VisualCanvas;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.FileUtils;
import at.fhj.utils.swing.JMsgPane;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class ViewLoader {

  private static Logger logger = Logger.getLogger(ViewLoader.class);

  public static final ImageIcon LOADING_ICON = new ImageIcon(
      JFlowMapMain.class.getResource("resources/loading.gif"));

  public static void loadView(final String viewConfigLocation, final Container parent)
    throws Exception
  {
    final JLabel loadingLabel = new JLabel(" Opening '" +
        FileUtils.getFilename(viewConfigLocation) + "'...", LOADING_ICON, JLabel.CENTER);
    parent.add(loadingLabel);

//    view = (IView) Worker.post(new Task() {
//      @Override
//      public Object run() throws Exception {
//        ViewConfig config = ViewConfig.load(viewConfigLocation);
//        return config.createView();
//      }
//    });


    SwingWorker<IView, Object> worker = new SwingWorker<IView, Object>() {
      @Override
      public IView doInBackground() throws IOException {
        ViewConfig config = ViewConfig.load(viewConfigLocation);
        return config.createView();
      }

      @Override
      protected void done() {
        boolean isViewEmpty = true;
        try {
          IView view = get();
          if (view != null) {
            /*
            JComponent controls = view.getControls();
            if (controls != null) {
              parent.add(controls, view.getControlsLayoutConstraint());
            }
            */

            VisualCanvas canvas = view.getVisualCanvas();
            if (canvas != null) {
              parent.add(canvas, BorderLayout.CENTER);
              isViewEmpty = false;

              final JComponent controls = view.getControls();
              if (controls != null) {
                createControls(parent, canvas, controls);
              }

            }
          }
        } catch (Exception ex) {
          logger.error("Cannot open view", ex);
          JMsgPane.showProblemDialog(parent, ex);
          isViewEmpty = true;
        } finally {
          try {
            parent.remove(loadingLabel);
            if (isViewEmpty) {
              parent.add(new JLabel("No view", JLabel.CENTER), BorderLayout.CENTER);
            }
            parent.validate();
          } catch (Exception ex) {
            // ignore
          }
        }
      }
    };

    worker.execute();
  }

  private static void createControls(final Container parent, VisualCanvas canvas,
      final JComponent controls) {
    final PButton button = new PButton("Settings");
    final PCamera ccam = canvas.getCamera();
    ccam.addChild(button);
    ccam.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        PBounds b = ccam.getBoundsReference();
        button.setPosition(b.getMaxX() - button.getWidth() - 2, 2);
      }
    });

    Window win = SwingUtilities.windowForComponent(parent);
    final JDialog dialog = new JDialog(win, "Settings");
    dialog.setContentPane(controls);
    dialog.pack();
    Rectangle b = win.getBounds();
    dialog.setLocation((int)b.getMaxX() - dialog.getWidth(), (int)b.getMaxY() - dialog.getHeight());
    dialog.setResizable(false);

    button.addInputEventListener(new PBasicInputEventHandler() {
       @Override
       public void mouseClicked(PInputEvent event) {
        dialog.setVisible(true);
       }
     });
  }

}
