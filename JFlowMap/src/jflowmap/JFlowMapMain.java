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

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jflowmap.data.ViewConfig;
import jflowmap.util.SwingUtils;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapMain {

  private static Logger logger = Logger.getLogger(JFlowMapMain.class);

  public static boolean IS_OS_MAC = getOSMatches("Mac");
  public static final String OS_NAME = System.getProperty("os.name");

  public static void main(String[] args) throws IOException {
    logger.info(">>> Starting JFlowMap");

    if (args.length >= 2  &&  args[0].equals("--fullscreen")) {

      final String configLocation = args[1];
      ViewConfig config = ViewConfig.load(configLocation);

      initSystemLF();

      final IView view = config.createView();

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JFrame frame = new JFrame("JFlowMap: " + configLocation);
          SwingUtils.makeFullscreen(frame);
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.add(view.getViewComponent());
          frame.setVisible(true);
        }
      });


    } else {
      if (!IS_OS_MAC) {
        initNimbusLF();
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          new JFlowMapMainFrame().setVisible(true);
        }
      });
    }
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