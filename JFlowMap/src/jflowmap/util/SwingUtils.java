package jflowmap.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import javax.swing.JFrame;

/**
 * @author Ilya Boyandin
 */
public class SwingUtils {

  private SwingUtils() {
  }

  public static void makeFullscreen(JFrame frame) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    frame.setUndecorated(true);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setSize((int)toolkit.getScreenSize().getWidth(), (int)toolkit.getScreenSize().getHeight());
    frame.setResizable(false);
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    gd.setFullScreenWindow(frame);
  }

}
