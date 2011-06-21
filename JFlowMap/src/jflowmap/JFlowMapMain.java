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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jflowmap.util.SwingUtils;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;
import edu.umd.cs.piccolo.util.PDebug;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapMain {

  private static Logger logger = Logger.getLogger(JFlowMapMain.class);

  public static boolean IS_OS_MAC = getOSMatches("Mac");
  public static final String OS_NAME = System.getProperty("os.name");

  private static void enableDebugging() {
    PDebug.debugThreads = true;
//    PDebug.debugBounds = true;
//    PDebug.debugFullBounds = true;
    PDebug.debugPaintCalls = true;
//    PDebug.debugPrintFrameRate = true;
//    PDebug.debugPrintUsedMemory = true;
//    PDebug.debugRegionManagement = true;
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: java -jar jflowmap.jar [-fullscreen] <view-config.jfmv>");
      System.exit(0);
    }

//    enableDebugging();


    final String configLocation;
    final boolean fullscreenMode;
    if (args[0].equals("-fullscreen")) {
      fullscreenMode = true;
      configLocation = args[1];
    } else {
      fullscreenMode = false;
      configLocation = args[0];
    }


    logger.info(">>> Starting JFlowMap");

//    initFonts();

//    initSystemLF();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        final JFrame frame = createFrame(configLocation);

//        JComponent cp = (JComponent)frame.getContentPane();
//        cp.getInputMap().put(KeyStroke.getKeyStroke("F11"), "fullScreen");
//        cp.getActionMap().put("fullScreen", new AbstractAction() {
//          @Override
//          public void actionPerformed(ActionEvent e) {
//            SwingUtilities.invokeLater(new Runnable() {
//              public void run() {
//                frame.dispose();
//                SwingUtils.makeFullscreen(frame, false);
//                frame.setVisible(true);
//              }
//            });
//          }
//        });

        if (fullscreenMode) {
          SwingUtils.makeFullscreen(frame, false);
        } else {
          SwingUtils.maximize(frame);
        }

        frame.setVisible(true);

        try {
          ViewLoader.loadView(configLocation, frame.getContentPane());
        } catch (Exception ex) {
          logger.error("Cannot open view", ex);
          JMsgPane.showProblemDialog(frame, ex);
          System.exit(0);
        }

      }

      private JFrame createFrame(final String configLocation) {
        final JFrame frame = new JFrame("JFlowMap: " + configLocation);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.white);
        return frame;
      }
    });


//    showMainFrame();
  }

  /*
  private static void initFonts() {
    final float scale = .9f;
    UIDefaults defaults = UIManager.getDefaults();
    @SuppressWarnings("rawtypes") Enumeration keys = defaults.keys();
    while(keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = defaults.get(key);
      if(value != null && value instanceof Font) {
         UIManager.put(key, null);
         Font font = UIManager.getFont(key);
         if(font != null) {
            float size = font.getSize2D();
            UIManager.put(key, new FontUIResource(
                font.deriveFont(size * scale)
//                new Font("Arial", font.isBold() ? Font.BOLD : Font.PLAIN, Math.round(size * scale))
                ));
         } } }
  }
  */

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

  public static ImageIcon createImageIcon(String path) {
    URL url = JFlowMapMain.class.getResource(path);
    if (url != null) {
      return new ImageIcon(url);
    } else {
      logger.error("Couldn't find file: " + path);
      return null;
    }
  }

}