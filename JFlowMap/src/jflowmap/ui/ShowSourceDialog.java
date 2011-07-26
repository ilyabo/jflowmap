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

package jflowmap.ui;

import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.text.DefaultEditorKit;

import jflowmap.util.SwingUtils;

/**
 * @author Ilya Boyandin
 */
public class ShowSourceDialog extends JDialog {

  final static String COPY = "Copy";
  final static String CUT = "Cut";
  final static String PASTE = "Paste";
  final static String SELECTALL = "Select All";


  public ShowSourceDialog(Window owner, String title, String text, boolean editable) {
    super(owner, title, ModalityType.APPLICATION_MODAL);
    final JTextPane textPane = new JTextPane();
    final JScrollPane scrollPane = new JScrollPane(textPane);
    scrollPane.setAutoscrolls(false);
    add(scrollPane);
    textPane.setText(text);
    textPane.setEditable(editable);
    setSize(800, 600);
    SwingUtils.centerOnScreen(this);

    addCopyPasteContextMenu(textPane, editable);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent e) {
        textPane.setCaretPosition(textPane.getDocument().getLength());
        textPane.moveCaretPosition(0);
      }
    });
  }

  private static void addCopyPasteContextMenu(final JComponent c, boolean editable) {
    final JPopupMenu menu = new JPopupMenu();
    final JMenuItem copyItem = new JMenuItem();
    copyItem.setAction(c.getActionMap().get(DefaultEditorKit.copyAction));
    copyItem.setText(COPY);

    final JMenuItem cutItem = new JMenuItem();
    cutItem.setAction(c.getActionMap().get(DefaultEditorKit.cutAction));
    cutItem.setText(CUT);

    final JMenuItem pasteItem = new JMenuItem(PASTE);
    pasteItem.setAction(c.getActionMap().get(DefaultEditorKit.pasteAction));
    pasteItem.setText(PASTE);

    final JMenuItem selectAllItem = new JMenuItem(SELECTALL);
    selectAllItem.setAction(c.getActionMap().get(DefaultEditorKit.selectAllAction));
    selectAllItem.setText(SELECTALL);

    menu.add(copyItem);
    if (editable) {
      menu.add(cutItem);
      menu.add(pasteItem);
      menu.add(new JSeparator());
    }
    menu.add(selectAllItem);

    c.add(menu);
    c.addMouseListener(new MouseAdapter() {
      private void showMenuIfPopupTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
          menu.show(c, e.getX() + 3, e.getY() + 3);
        }
      }
      @Override
      public void mousePressed(MouseEvent e) {
        showMenuIfPopupTrigger(e);
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        showMenuIfPopupTrigger(e);
      }
    });
  }

}