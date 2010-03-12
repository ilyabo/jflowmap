/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import jflowmap.FlowMapMain;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.StringUtils;
import at.fhj.utils.swing.JMsgPane;

/**
 * @author Ilya Boyandin
 */
public class OpenFileAction extends AbstractAction {

    private static final ImageIcon ICON = new ImageIcon(
            OpenFileAction.class.getResource("images/Open16-2.gif"));

    private final FlowMapMain app;
    private final As target;

    public OpenFileAction(FlowMapMain app, As target) {
        this.app = app;
        this.target = target;

        String name = target.getName();
        String capitalizedName = StringUtils.firstUpper(name);

        putValue(Action.NAME, "Open As " + capitalizedName + "...");
        putValue(Action.SMALL_ICON, ICON);
        putValue(Action.SHORT_DESCRIPTION, "Open File As " + capitalizedName);
        putValue(Action.LONG_DESCRIPTION, "Open file " + name);
        putValue(Action.ACTION_COMMAND_KEY, "open-file-" + name);
    }

    public enum As {
        MAP("map") {
            @Override
            public void open(FlowMapMain app, String filename) throws Exception {
                app.showFlowMaps(filename);
            }
        },
        TIMELINE("timeline") {
            @Override
            public void open(FlowMapMain app, String filename) throws Exception {
                app.showFlowTimeline(filename);
            }
        };

        private String name;
        private As(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public abstract void open(FlowMapMain app, String filename) throws Exception;
    }

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
            final FileFilter currentFilter = Filters.GRAPHML.getFilter();
            fc.addChoosableFileFilter(currentFilter);
            fc.setFileFilter(currentFilter);
            fc.setAcceptAllFileFilterUsed(false);

            int confirm = fc.showDialog(app, (String) getValue(Action.NAME));
            if (confirm == JFileChooser.APPROVE_OPTION) {
                target.open(app, fc.getSelectedFile().getAbsolutePath());
                AppPreferences.INSTANCE.setFileOpenLastVisitedDir(fc.getSelectedFile().getParent());
            }
        } catch (Throwable th) {
            JMsgPane.showProblemDialog(app, "File couldn't be loaded: " + th.getMessage());
            Logger.getLogger(getClass().getName()).error("Exception: ", th);
        }
    }


    private enum Filters {
        GRAPHML("GraphML", ".xml", ".graphml");

        private final FileFilter filter;
        private final String description;

        private Filters(final String name, final String ... extensions) {
            this.description = name + " files (*" + StringUtils.join(extensions, ",*") + ")";
            this.filter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    for (String ext : extensions) {
                        if (f.getName().endsWith(ext)) return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return description;
                }
            };
        }

        public FileFilter getFilter() {
            return filter;
        }

        public String getDescription() {
            return description;
        }

    }

}
