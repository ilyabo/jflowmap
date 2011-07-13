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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import jflowmap.AppPreferences;
import jflowmap.FlowMapColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.IView;
import jflowmap.JFlowMapMainFrame;
import jflowmap.data.StaxGraphMLReader;
import jflowmap.geo.MapProjections;
import jflowmap.ui.PropertiesDialog;
import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import at.fhj.utils.misc.StringUtils;
import at.fhj.utils.swing.JMsgPane;

import com.google.common.collect.Iterables;

/**
 * @author Ilya Boyandin
 */
public class OpenFlowMapGraphAction extends AbstractAction {

  private static final Logger logger = Logger.getLogger(OpenFlowMapGraphAction.class);

  private static final ImageIcon ICON = new ImageIcon(
      OpenFlowMapGraphAction.class.getResource("images/Open16-2.gif"));

  private final JFlowMapMainFrame app;
  private final As target;

  public OpenFlowMapGraphAction(JFlowMapMainFrame app, As target) {
    this.app = app;
    this.target = target;

    String name = target.getName();
    String capitalizedName = StringUtils.firstUpper(name);

    putValue(Action.NAME, "Open in " + capitalizedName + " View...");
    putValue(Action.SMALL_ICON, ICON);
    putValue(Action.SHORT_DESCRIPTION, "Open File As " + capitalizedName);
    putValue(Action.LONG_DESCRIPTION, "Open file " + name);
    putValue(Action.ACTION_COMMAND_KEY, "open-file-" + name);
  }

  public enum As {
    FLOWMAP("flowmap") {
      @Override
      public JComponent createPropertiesPanel(String fileName) throws IOException {

        Graph graph = StaxGraphMLReader.readFirstGraph(fileName);
        String[] doubleNodeAttrs = Iterables.toArray(FlowMapGraph.nodeAttrsOf(graph, double.class), String.class);
        String[] doubleEdgeAttrs = Iterables.toArray(FlowMapGraph.edgeAttrsOf(graph, double.class), String.class);
        String[] stringNodeAttrs = Iterables.toArray(FlowMapGraph.nodeAttrsOf(graph, String.class), String.class);
        String[] stringEdgeAttrs = Iterables.toArray(FlowMapGraph.edgeAttrsOf(graph, String.class), String.class);


        JPanel panel = new JPanel(new MigLayout("","[para]0[][100lp, fill][60lp][95lp, fill]", ""));

        //panel.add(new JLabel("Properties for \"" + new File(fileName).getName() + "\""), "wrap");

        addSeparator(panel, "Node attributes");

        panel.add(new JLabel("Label"), "skip");
        panel.add(new JComboBox(stringNodeAttrs), "wrap para");

        panel.add(new JLabel("Latitude"), "skip");
        panel.add(new JComboBox(doubleNodeAttrs), "wrap para");

        panel.add(new JLabel("Longitude"), "skip");
        panel.add(new JComboBox(doubleNodeAttrs), "wrap para");


        addSeparator(panel, "Edge attributes");

        panel.add(new JLabel("Weight"), "skip");
        panel.add(new JComboBox(doubleEdgeAttrs), "wrap para");


        addSeparator(panel, "Map properties");

        panel.add(new JLabel("Map shapefile"), "skip");
        panel.add(new JTextField(), "span, growx");


        panel.add(new JLabel("Map projection"), "skip");
        panel.add(new JComboBox(MapProjections.values()), "wrap para");

        addSeparator(panel, "Aesthetics");

        panel.add(new JLabel("Color scheme"), "skip");
        panel.add(new JComboBox(FlowMapColorSchemes.values()), "wrap para");

        return panel;
      }

      @Override
      public IView createView(JComponent propertiesPanel) {

        return null;
      }
    },
    FLOWSTRATES("flowstrates") {
      @Override
      public JComponent createPropertiesPanel(String fileName) {
        return new JLabel("Hello flowstrates");
      }
      @Override
      public IView createView(JComponent propertiesPanel) {
        return null;
      }
    },
    TIMELINE("timeline") {
      @Override
      public JComponent createPropertiesPanel(String fileName) {
        return null;
      }
      @Override
      public IView createView(JComponent propertiesPanel) {
        return null;
      }
    };

    private String name;
    private As(String name) {
      this.name = name;
    }
    public String getName() {
      return name;
    }

    public abstract JComponent createPropertiesPanel(String fileName) throws IOException;

    public abstract IView createView(JComponent propertiesPanel);

//    public abstract void open(JFlowMapMain app, String filename) throws Exception;
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
        AppPreferences.INSTANCE.setFileOpenLastVisitedDir(fc.getSelectedFile().getParent());

        JComponent propertiesPanel = target.createPropertiesPanel(
            fc.getSelectedFile().getAbsolutePath());
        if (propertiesPanel != null) {
          if (PropertiesDialog.showFor(app,
              StringUtils.firstUpper(target.getName()) + " properties", propertiesPanel)) {

          }
        }

//        app.showFlowMaps
//        target.open(app, fc.getSelectedFile().getAbsolutePath());
      }

    } catch (Exception ex) {
      JMsgPane.showProblemDialog(app, "File couldn't be loaded: " + ex.getMessage());
      logger.error("Exception: ", ex);
    }
  }


  private enum Filters {
    GRAPHML("GraphML", ".graphml", ".graphml.gz", ".xml", ".xml.gz");

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

  private static final Color LABEL_COLOR = new Color(0, 70, 213);

  private static void addSeparator(JPanel panel, String text) {
    JLabel l = new JLabel(text);
    l.setForeground(LABEL_COLOR);
    panel.add(l, "gapbottom 1, span, split 2, aligny center");
    panel.add(new JSeparator(), "gapleft rel, growx");
  }

}
