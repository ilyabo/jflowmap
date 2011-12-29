package jflowmap.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

/**
 * Based on ProgressGlassPane by Romain Guy from the book "Filthy Rich Clients".
 */
public class BlockingGlassPane extends JComponent {

  public BlockingGlassPane() {
    // blocks all user input
    addMouseListener(new MouseAdapter() { });
    addMouseMotionListener(new MouseMotionAdapter() { });
    addKeyListener(new KeyAdapter() { });

    setFocusTraversalKeysEnabled(false);
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent evt) {
        requestFocusInWindow();
      }
    });

    setBackground(Color.WHITE);
  }

  @Override
  protected void paintComponent(Graphics g) {
    // enables anti-aliasing
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // gets the current clipping area
    Rectangle clip = g.getClipBounds();

    // sets a 65% translucent composite
    AlphaComposite alpha = AlphaComposite.SrcOver.derive(0.65f);
    Composite composite = g2.getComposite();
    g2.setComposite(alpha);

    // fills the background
    g2.setColor(getBackground());
    g2.fillRect(clip.x, clip.y, clip.width, clip.height);

    g2.setComposite(composite);
  }
}
