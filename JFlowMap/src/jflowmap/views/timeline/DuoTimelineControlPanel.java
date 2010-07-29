/*
 * This file is part of JFlowMap.
 *
 * Copyright 2009 Ilya Boyandin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jflowmap.views.timeline;

import java.awt.Color;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineControlPanel extends JPanel {

  private static final Color LABEL_COLOR = new Color(0, 70, 213);

  private final DuoTimelineView duoTimelineView;

  public DuoTimelineControlPanel(DuoTimelineView duoTimelineView) {
    this.duoTimelineView = duoTimelineView;

    JTabbedPane tabbedPane = new JTabbedPane();
    add(tabbedPane);

    JPanel queryPanel = createTabPanel(new MigLayout("", "", /*"[pref!][grow]",*/ "[]15[]"));
    tabbedPane.addTab("Query", queryPanel);
    queryPanel.add(new JLabel("Source:"), "al right");
    queryPanel.add(new JTextField(), "growx, wmin 150, gapright .15cm");

    queryPanel.add(new JLabel("Target:"), "al right");
    queryPanel.add(new JTextField(), "growx, wmin 150");

    queryPanel.add(new JButton("Update"), "gapleft .25cm, wrap");

    queryPanel.add(new JLabel("Max rows:"), "al right, split 4, span 4");
    queryPanel.add(new JComboBox(new Object[] { 50, 100, 250, 500 }), "height min, width min");

    queryPanel.add(new JLabel("Order by:"), "gapleft .35cm, al right");
    queryPanel.add(new JComboBox(new Object[] { "max flow", "total" }), "");



    JPanel aestheticsPanel = createTabPanel(new MigLayout("", "", "[]15[]"));
    tabbedPane.addTab("Aesthetics", aestheticsPanel);
    aestheticsPanel.add(new JLabel("Heatmap colors:"), "al right");
    aestheticsPanel.add(new JComboBox(new Object[] { "RdBu 5" }), "");
    aestheticsPanel.add(new JCheckBox("Interpolate"), "gapleft .25cm");

  }

  private static void addSeparator(JPanel panel, String text) {
    JLabel l = createLabel(text);
    l.setForeground(LABEL_COLOR);

    panel.add(l, "gapbottom 1, span, split 2, aligny center");
    panel.add(new JSeparator(), "gapleft rel, growx");
  }

  private static JLabel createLabel(String text) {
    return new JLabel(text, SwingConstants.LEADING);
  }

  private static JPanel createTabPanel(LayoutManager lm) {
    JPanel panel = new JPanel(lm);
    return panel;
  }

}
