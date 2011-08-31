package jflowmap.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JApplet;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.log4j.Logger;

/**
 * @author Ilya Boyandin
 */
public class SwingUtils {

  private static Logger logger = Logger.getLogger(SwingUtils.class);

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

  public static JApplet getAppletFor(Component c) {
    JApplet iframe;
    if (c instanceof JApplet) {
      iframe = (JApplet)c;
    } else {
      iframe = (JApplet) SwingUtilities.getAncestorOfClass(JApplet.class, c);
    }
    return iframe;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Component> T getChildOfType(Container c, Class<T> klass) {
    for (int i = 0, size = c.getComponentCount(); i < size; i++) {
      Component comp = c.getComponent(i);
      if (comp.getClass().isAssignableFrom(klass)) {
        return (T) comp;
      }
    }
    return null;
  }

  public static void initNimbusLF() {
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

}
