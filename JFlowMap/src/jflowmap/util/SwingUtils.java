package jflowmap.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

/**
 * @author Ilya Boyandin
 */
public class SwingUtils {

  private SwingUtils() {
  }

  /**
   * Exclusive mode is only available if <code>isFullScreenSupported</code>
   * returns <code>true</code>.
   *
   * Exclusive mode implies:
   * <ul>
   * <li>Windows cannot overlap the full-screen window.  All other application
   * windows will always appear beneath the full-screen window in the Z-order.
   * <li>There can be only one full-screen window on a device at any time,
   * so calling this method while there is an existing full-screen Window
   * will cause the existing full-screen window to
   * return to windowed mode.
   * <li>Input method windows are disabled.  It is advisable to call
   * <code>Component.enableInputMethods(false)</code> to make a component
   * a non-client of the input method framework.
   * </ul>
   */
  public static void makeFullscreen(Frame frame, boolean attemptExclusiveMode) {
    maximize(frame);
    frame.setUndecorated(true);
    frame.setLocation(0, 0);
    frame.setResizable(false);
    if (attemptExclusiveMode) {
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
      gd.setFullScreenWindow(frame);
    }
  }

  public static void maximize(Frame frame) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    frame.setSize((int)toolkit.getScreenSize().getWidth(), (int)toolkit.getScreenSize().getHeight());
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
  }

  public static void centerOnScreen(Window window) {
    final Dimension size = window.getSize();
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    final int locX = (screen.width - size.width) / 2;
    final int locY = (screen.height - size.height) / 2;
    window.setLocation(locX, locY);
  }

  public static JInternalFrame getInternalFrameFor(Component c) {
    JInternalFrame iframe;
    if (c instanceof JInternalFrame) {
      iframe = (JInternalFrame)c;
    } else {
      iframe = (JInternalFrame) SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
    }
    return iframe;
  }

  public static Window getWindowFor(Component c) {
    Window win;
    if (c instanceof Window) {
      win = (Window)c;
    } else {
      win = SwingUtilities.windowForComponent(c);
    }
    return win;
  }

}
