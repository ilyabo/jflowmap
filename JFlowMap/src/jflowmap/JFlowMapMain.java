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
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jflowmap.data.ViewConfig;
import jflowmap.util.SwingUtils;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.FileUtils;
import at.fhj.utils.swing.JMsgPane;
import foxtrot.Task;
import foxtrot.Worker;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapMain {

  private static Logger logger = Logger.getLogger(JFlowMapMain.class);

  public static boolean IS_OS_MAC = getOSMatches("Mac");
  public static final String OS_NAME = System.getProperty("os.name");
  public static final ImageIcon LOADING_ICON = new ImageIcon(
      JFlowMapMain.class.getResource("resources/loading.gif"));

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: java -jar jflowmap.jar [--fullscreen] <view-config.jfmv>");
      System.exit(0);
    }

    final String configLocation;
    final boolean fullscreenMode;
    if (args[0].equals("--fullscreen")) {
      fullscreenMode = true;
      configLocation = args[1];
    } else {
      fullscreenMode = false;
      configLocation = args[0];
    }


    logger.info(">>> Starting JFlowMap");
    initSystemLF();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final JFrame frame = new JFrame("JFlowMap: " + configLocation);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.white);

        if (fullscreenMode) {
          SwingUtils.makeFullscreen(frame);
        } else {
          SwingUtils.maximize(frame);
        }


        JLabel loadingLabel = new JLabel(" Opening '" +
            FileUtils.getFilename(configLocation) + "'...", LOADING_ICON, JLabel.CENTER);
        frame.add(loadingLabel);

        frame.setVisible(true);

        IView view = null;
        try {
          view = (IView) Worker.post(new Task() {
            @Override
            public Object run() throws Exception {
              ViewConfig config = ViewConfig.load(configLocation);
              return config.createView();
            }
          });
        } catch (Exception ex) {
          logger.error("Cannot open view", ex);
          JMsgPane.showProblemDialog(frame, ex);
          System.exit(0);
        }

        frame.remove(loadingLabel);
        frame.add(view.getViewComponent(), BorderLayout.CENTER);
        JComponent controls = view.getControls();
        if (controls != null) {
          frame.add(controls, BorderLayout.NORTH);
        }
      }
    });


//    showMainFrame();
  }

  private static void showMainFrame() {
    if (!IS_OS_MAC) {
      initNimbusLF();
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new JFlowMapMainFrame().setVisible(true);
      }
    });
  }

  private static void initSystemLF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      logger.error("Cannot init systemL&F");
    }
  }

  private static void initNimbusLF() {
    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Nimbus".equals(info.getName())) {
        try {
          UIManager.setLookAndFeel(info.getClassName());
        } catch (Exception e) {
          logger.error("Cannot init Nimbus L&F");
        }
        break;
      }
    }
  }

  private static boolean getOSMatches(String osNamePrefix) {
    if (OS_NAME == null) {
      return false;
    }
    return OS_NAME.startsWith(osNamePrefix);
  }

}