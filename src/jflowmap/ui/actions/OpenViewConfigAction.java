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

package jflowmap.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import jflowmap.AppPreferences;
import jflowmap.JFlowMapMainFrame;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;

/**
 * @author Ilya Boyandin
 */
public class OpenViewConfigAction extends AbstractAction {

  private static final Logger logger = Logger.getLogger(OpenViewConfigAction.class);

  private static final ImageIcon ICON = new ImageIcon(
      OpenViewConfigAction.class.getResource("images/Open16-2.gif"));

  private final JFlowMapMainFrame app;

  public OpenViewConfigAction(JFlowMapMainFrame app) {
    this.app = app;

    putValue(Action.NAME, "Open view...");
    putValue(Action.SMALL_ICON, ICON);
    putValue(Action.SHORT_DESCRIPTION, "Open View Config");
    putValue(Action.LONG_DESCRIPTION, "Open view config");
    putValue(Action.ACTION_COMMAND_KEY, "open-view-config");
  }

  private final FileFilter filter = new FileFilter() {
    @Override
    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }
      if (f.getName().toLowerCase().endsWith(".jfmv")) return true;
      return false;
    }

    @Override
    public String getDescription() {
      return "JFlowMap view config files (*.jfmv)";
    }
  };


  @Override
  public void actionPerformed(ActionEvent e) {
    try {

      final JFileChooser fc = new JFileChooser();
      fc.setAcceptAllFileFilterUsed(false);
      fc.setMultiSelectionEnabled(false);
      final String lastVisitedDir = AppPreferences.INSTANCE.getFileOpenLastVisitedDir();
      final String dir;
      if (lastVisitedDir != null) {
        dir = lastVisitedDir;
      } else {
        dir = System.getProperty("user.dir");
      }
      if (dir != null) {
        fc.setCurrentDirectory(new File(dir));
      }
      fc.addChoosableFileFilter(filter);
      fc.setFileFilter(filter);
      fc.setAcceptAllFileFilterUsed(false);

      int confirm = fc.showDialog(app, (String) getValue(Action.NAME));

      if (confirm == JFileChooser.APPROVE_OPTION) {
        AppPreferences.INSTANCE.setFileOpenLastVisitedDir(fc.getSelectedFile().getParent());
        app.loadView(fc.getSelectedFile().getAbsolutePath());
      }

    } catch (Exception ex) {
      JMsgPane.showProblemDialog(app, "File couldn't be loaded: " + ex.getMessage());
      logger.error("Exception: ", ex);
    }

  }

}
