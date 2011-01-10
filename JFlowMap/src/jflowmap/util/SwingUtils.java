package jflowmap.util;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;

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

  public static void center(Window window) {
    final Dimension size = window.getSize();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    final int locX = (screen.width - size.width) / 2;
    final int locY = (screen.height - size.height) / 2;
    window.setLocation(locX, locY);
  }

}
