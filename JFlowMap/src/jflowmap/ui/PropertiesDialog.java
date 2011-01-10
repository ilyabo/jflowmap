package jflowmap.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * @author Ilya Boyandin
 */
public class PropertiesDialog extends JDialog {

  public enum ReturnValue { OK, CANCEL, ERROR };

  private ReturnValue returnValue;

  private PropertiesDialog(JFrame owner, String title, JComponent propertiesPanel) {
    super(owner, title, true);

    Container cp = getContentPane();
//    dialog.setContentPane(cp);
//    cp.setLayout(new BorderLayout());
    cp.add(propertiesPanel, BorderLayout.CENTER);
    cp.add(createButtons(), BorderLayout.SOUTH);

    propertiesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        returnValue = ReturnValue.CANCEL;
      }
    });

    pack();
    setLocationRelativeTo(owner);
  }

  public static boolean showFor(JFrame owner, String title, JComponent propertiesPanel) {
    PropertiesDialog dialog = new PropertiesDialog(owner, title, propertiesPanel);
    dialog.setVisible(true);
    return dialog.getReturnValue() == ReturnValue.OK;
  }

  public ReturnValue getReturnValue() {
    return returnValue;
  }

  private JPanel createButtons() {
    JPanel p = new JPanel(new MigLayout("nogrid, fillx", "", ""));

    JButton okBut = new JButton("Apply");
    okBut.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        returnValue = ReturnValue.OK;
        dispose();
      }
    });

    JButton cancelBut = new JButton("Cancel");
    cancelBut.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        returnValue = ReturnValue.CANCEL;
        dispose();
      }
    });

    p.add(okBut, "tag Apply");
    p.add(cancelBut, "tag Cancel");

    return p;
  }

}
