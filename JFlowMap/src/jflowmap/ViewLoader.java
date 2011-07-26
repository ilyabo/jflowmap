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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import jflowmap.data.ViewConfig;
import jflowmap.util.SwingUtils;
import jflowmap.util.piccolo.PBoxLayoutNode;
import jflowmap.util.piccolo.PButton;
import jflowmap.util.piccolo.PNodes;
import jflowmap.views.VisualCanvas;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.FileUtils;
import at.fhj.utils.swing.JMsgPane;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class ViewLoader {

  private static Logger logger = Logger.getLogger(ViewLoader.class);

  public static final String CLIENT_PROPERTY_CONTAINER_IVIEW = "_iview";

  public static final ImageIcon LOADING_ICON = JFlowMapMain.createImageIcon("resources/loading.gif");

  private static final Font LOADING_TEXT_FONT = new Font("Sans Serif", Font.PLAIN, 11);

  public static void loadView(final String viewConfigLocation, final Container parent)
    throws Exception
  {
    final JLabel loadingLabel = new JLabel(" Opening '" +
        FileUtils.getFilename(viewConfigLocation) + "'...", LOADING_ICON, JLabel.CENTER);
    loadingLabel.setFont(LOADING_TEXT_FONT);
    parent.add(loadingLabel);
    parent.setBackground(Color.white);

//    view = (IView) Worker.post(new Task() {
//      @Override
//      public Object run() throws Exception {
//        ViewConfig config = ViewConfig.load(viewConfigLocation);
//        return config.createView();
//      }
//    });


    SwingWorker<IView, Object> worker = new SwingWorker<IView, Object>() {

      ViewConfig config;

      @Override
      public IView doInBackground() throws IOException {
        config = ViewConfig.load(viewConfigLocation);

        adjustWindowSize(parent);

        return config.createView();
      }

      private void adjustWindowSize(final Container parent) {
        String size = config.getString(ViewConfig.PROP_WINDOW_SIZE);
        if (size != null) {
          Matcher m = Pattern.compile("(\\d+)x(\\d+)").matcher(size);
          if (m.matches()) {
            Dimension dim = new Dimension(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));

            JInternalFrame iframe = SwingUtils.getInternalFrameFor(parent);
            if (iframe != null) {
              iframe.setSize(dim);
            } else {
              Window win = SwingUtils.getWindowFor(parent);
              if (win != null) {
                win.setSize(dim);
              }
            }
          }
        }
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

              JInternalFrame iframe = SwingUtils.getInternalFrameFor(parent);
              if (iframe != null) {
                iframe.setTitle(view.getName());
                iframe.putClientProperty(CLIENT_PROPERTY_CONTAINER_IVIEW, view);
              } else {
                Window w = SwingUtils.getWindowFor(parent);
                w.setName(view.getName());
              }

              final JComponent controls = view.getControls();
              if (controls != null) {
                initControls(parent, canvas, controls, config);
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

  private static void initControls(final Container parent, VisualCanvas canvas,
      final JComponent controls, ViewConfig config) {


    String visibleTabs = config.getString(ViewConfig.PROP_WINDOW_SETTINGS_SHOW_TABS);
    if (visibleTabs != null) {
      setVisibileSettingsTabs(controls, visibleTabs.split(","));
    }

    boolean embedSettings = config.getBoolOrElse(ViewConfig.PROP_WINDOW_SETTINGS_EMBED, false);
    if (embedSettings) {
      parent.add(controls, BorderLayout.SOUTH);
    } else {
      final PBoxLayoutNode buttonPanel = new PBoxLayoutNode(PBoxLayoutNode.Axis.X, 5);
      buttonPanel.addChild(createSettingsButton(parent, controls, config));
      buttonPanel.addChild(createPaintToSvgButton(parent, canvas));

  //    final PButton helpBut = new PButton(" ? ", true);
  //    buttonPanel.addChild(helpBut);


      final PCamera ccam = canvas.getCamera();
      ccam.addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          PBounds b = ccam.getBoundsReference();
          PNodes.setPosition(buttonPanel, b.getMaxX() - buttonPanel.getFullBoundsReference().width - 4, 4);
        }
      });
      ccam.addChild(buttonPanel);
    }
  }

  private static PNode createPaintToSvgButton(final Container parent, final VisualCanvas canvas) {
    PButton but = new PButton("SVG");
    but.addInputEventListener(new PBasicInputEventHandler() {
      @Override
      public void mouseClicked(PInputEvent event) {
        canvas.tryToPaintToSvg();
      }
    });
    return but;
  }

  private static PButton createSettingsButton(final Container parent, final JComponent controls,
      ViewConfig config) {
    final PButton settingsBut = new PButton("Settings", true);

    Window win = SwingUtils.getWindowFor(parent);
    final JDialog dialog = new JDialog(win, "Settings");
//    try {
//      dialog.setAlwaysOnTop(true);
//    } catch (SecurityException se) {
//      // ignore
//    }
    dialog.setContentPane(controls);
    dialog.pack();
    Rectangle b = win.getBounds();
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    dialog.setLocation(
        (int)b.getMaxX() - dialog.getWidth(),
        (int)Math.min(b.getMaxY(), screen.getHeight() - dialog.getHeight()));
    dialog.setResizable(false);


    String activeTab = config.getString(ViewConfig.PROP_WINDOW_SETTINGS_ACTIVE_TAB);
    if (activeTab != null) {
      setActiveControlTab(dialog.getContentPane(), activeTab);
    }

    boolean visible = config.getBoolOrElse(ViewConfig.PROP_WINDOW_SETTINGS_SHOW, false);
    dialog.setVisible(visible);
    settingsBut.setPressed(visible);

    settingsBut.addInputEventListener(new PBasicInputEventHandler() {
       @Override
       public void mouseClicked(PInputEvent event) {
         if (!dialog.isVisible()) {
           dialog.setVisible(true);
           dialog.toFront();
           dialog.requestFocus();
           dialog.addWindowListener(new WindowAdapter() {
             @Override
            public void windowDeactivated(WindowEvent e) {
               if (!settingsBut.isArmed()) {
                 settingsBut.setPressed(false);
               }
             }
             @Override
             public void windowClosed(WindowEvent e) {
             }
           });
         } else {
           dialog.setVisible(false);
         }
       }
     });

    return settingsBut;
  }

  private static JTabbedPane getTabbedPane(Container contentPane) {
    JTabbedPane tp = null;
    for (Component c : contentPane.getComponents()) {
      if (c instanceof JTabbedPane) {
        tp = (JTabbedPane)c;
        break;
      }
    }
    return tp;
  }

  private static void setActiveControlTab(Container contentPane, String activeTab) {
    JTabbedPane tp = getTabbedPane(contentPane);
    if (tp != null) {
      int index = tp.indexOfTab(activeTab);
      if (index >= 0) {
        tp.setSelectedIndex(index);
      }
    }
  }

  private static int setVisibileSettingsTabs(Container contentPane, String[] visibleTabs) {
    int numVisible = 0;
    List<String> tabList = ImmutableList.copyOf(visibleTabs);
    JTabbedPane tp = getTabbedPane(contentPane);
    if (tp != null) {
      for (int i = tp.getTabCount() - 1; i >= 0; i--) {
        String title = tp.getTitleAt(i);
        boolean visible = tabList.contains(title);
        if (!visible) {
          tp.removeTabAt(i);
//          tp.getComponentAt(i).setVisible(false);
        } else {
          numVisible++;
        }
      }
    }
    return numVisible;
  }
}
