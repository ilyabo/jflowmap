package jflowmap;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jflowmap.util.SwingUtils;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapAppletFrame extends JFrame {

  private final JFlowMapApplet applet;

  public JFlowMapAppletFrame(JFlowMapApplet applet) {
    super("JFlowMap");
    this.applet = applet;
  }

  public JFlowMapApplet getApplet() {
    return applet;
  }

  public static JFlowMapApplet getApplet(Component c) {
    JFlowMapAppletFrame frame = (JFlowMapAppletFrame)
        SwingUtilities.getAncestorOfClass(JFlowMapAppletFrame.class, c);
    if (frame != null)
      return frame.getApplet();
    else
      return (JFlowMapApplet)SwingUtils.getAppletFor(c);
  }
}