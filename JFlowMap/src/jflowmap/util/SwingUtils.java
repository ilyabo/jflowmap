package jflowmap.util;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * @author Ilya Boyandin
 */
public class SwingUtils {

  private SwingUtils() {
  }

  public static void makeFullscreen(Frame frame) {
    maximize(frame);
    frame.setUndecorated(true);
    frame.setResizable(false);
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    gd.setFullScreenWindow(frame);
  }

  public static void maximize(Frame frame) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    frame.setSize((int)toolkit.getScreenSize().getWidth(), (int)toolkit.getScreenSize().getHeight());
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
  }

  public static void center(Window window) {
    final Dimension size = window.getSize();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    final int locX = (screen.width - size.width) / 2;
    final int locY = (screen.height - size.height) / 2;
    window.setLocation(locX, locY);
  }

}
