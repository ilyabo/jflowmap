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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import jflowmap.data.XmlDatasetSpecsReader;
import jflowmap.views.flowmap.FlowMapView;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class FlowMapPreloadedDataMain extends JFrame {

  private static Logger logger = Logger.getLogger(FlowMapPreloadedDataMain.class);

  private static final long serialVersionUID = 1L;
  public static final String OS_NAME = System.getProperty("os.name");
  public static boolean IS_OS_MAC = getOSMatches("Mac");

  private static boolean getOSMatches(String osNamePrefix) {
    if (OS_NAME == null) {
      return false;
    }
    return OS_NAME.startsWith(osNamePrefix);
  }

  private final FlowMapView flowMap;

  public FlowMapPreloadedDataMain(List<DatasetSpec> datasetSpecs) {
    setTitle("FlowMapView");
    flowMap = new FlowMapView(datasetSpecs, true);

    setLayout(new BorderLayout());
    JComponent viewComp = flowMap.getViewComponent();
    JComponent controls = flowMap.getControls();
    if (controls == null) {
      add(viewComp, BorderLayout.CENTER);
      controls = null;
    } else {
      add(createHorizontalSplitPane(viewComp, controls));
    }

//    JPanel statusPanel = new JPanel(new BorderLayout());
//    add(statusPanel, BorderLayout.SOUTH);

//    JMemoryIndicator mi = new JMemoryIndicator(3000);
//    statusPanel.add(mi, BorderLayout.EAST);
//    mi.startUpdater();

    setExtendedState(MAXIMIZED_BOTH);
    setMinimumSize(new Dimension(800, 600));

    final Dimension size = getSize();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    final int locX = (screen.width - size.width) / 2;
    final int locY = (screen.height - size.height) / 2;
    setLocation(locX, locY);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        flowMap.fitInView();
      }
      @Override
      public void windowClosing(WindowEvent e) {
        dispose();
        shutdown();
      }
    });
  }



  private JSplitPane createHorizontalSplitPane(JComponent top, JComponent bottom) {
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
    BasicSplitPaneUI ui = new BasicSplitPaneUI() {
      @Override
      public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
          @Override
          protected JButton createLeftOneTouchButton() {
            JButton but = super.createLeftOneTouchButton();
            but.setEnabled(false);
            but.setVisible(false);
            return but;
          }
        };
      }
    };
    splitPane.setUI(ui);
    splitPane.setOneTouchExpandable(true);
    splitPane.setResizeWeight(1.0);
    return splitPane;
  }


  public void shutdown() {
    logger.info("Exiting application");
    System.exit(0);
  }

  public static void main(String[] args) throws IOException {
    logger.info(">>> Starting FlowMapView");
    final List<DatasetSpec> datasetSpecs =
      XmlDatasetSpecsReader.readDatasetSpecs("/data/datasets.xml");
    initLookAndFeel();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new FlowMapPreloadedDataMain(datasetSpecs).setVisible(true);
      }
    });
  }

  private static void initLookAndFeel() {
//    try {
//      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//        if ("Nimbus".equals(info.getName())) {
//          UIManager.setLookAndFeel(info.getClassName());
//          break;
//        }
//      }
////      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }
}
