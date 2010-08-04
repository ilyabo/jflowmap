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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

  private final JTabbedPane tabbedPane;

  public DuoTimelineControlPanel(DuoTimelineView duoTimelineView) {
    this.duoTimelineView = duoTimelineView;

    setLayout(new MigLayout("aligny top,alignx center,gapx 25", "", "grow"));
    tabbedPane = new JTabbedPane();
    add(tabbedPane);

    addPanel(createDataPanel());
    addPanel(createFilterPanel());
    addPanel(createAestheticsPanel());
  }

  private void addPanel(JPanel panel) {
    panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));
//    add(panel);
    tabbedPane.addTab(panel.getName(), panel);
  }


  private JPanel createDataPanel() {
    JPanel panel = new JPanel(new MigLayout("", "", /*"[pref!][grow]",*/ "[]15[]"));
    panel.setName("Data");


//    FlowMapGraphSet fmgs = duoTimelineView.getFlowMapGraph();


    panel.add(new JLabel("Attribute:"), "al right");
    panel.add(new JComboBox(new Object[] { "r", "rity" }), "height min, width min");


    panel.add(new JLabel("Group by:"), "al right, gapleft 15");
    panel.add(new JComboBox(new Object[] { "<None>", "r", "rity" }), "height min, width min, wrap");


//    JButton applyButton = new JButton("Apply");
//    panel.add(applyButton, "gapleft 10, span, al right, wrap");

    // --
    final JCheckBox differencesChk = new JCheckBox("Differences",
        duoTimelineView.getUseWeightDifferences());
    panel.add(differencesChk, "al right, span 2");
    differencesChk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        duoTimelineView.setUseWeightDifferences(differencesChk.isSelected());
      }
    });

    panel.add(new JLabel("Order by:"), "gapleft 10, al right");
    panel.add(new JComboBox(new Object[] {
        "max magnitude in row",
        "Euclidean distance from max" }), "");

    return panel;
  }


  private JPanel createFilterPanel() {
    JPanel panel = new JPanel(new MigLayout("", "", /*"[pref!][grow]",*/ "[]15[]"));
    panel.setName("Filter");

    panel.add(new JLabel("Source:"), "al right, gapleft 10");
    panel.add(new JTextField(), "growx, wmin 150, gapright 5");

    panel.add(new JLabel("Target:"), "al right");
    panel.add(new JTextField(), "growx, wmin 150");

    panel.add(new JLabel("Max rows:"), "gapleft 10, al right");  //
    panel.add(new JComboBox(new Object[] { 50, 100, 250, 500, "\u221e" /*infinity*/ }),
        "height min, width min");

    panel.add(new JButton("Apply"), "gapleft 15, wrap");

    //--

    return panel;
  }


  private JPanel createAestheticsPanel() {
    JPanel panel = new JPanel(new MigLayout("", "", "[]15[]"));
    panel.setName("Aesthetics");
    panel.add(new JLabel("Heatmap colors:"), "al right");
    panel.add(new JComboBox(new Object[] { "RdBu 5" }), "");
    panel.add(new JCheckBox("Interpolate"), "gapleft 15");
    return panel;
  }


  private static JLabel createLabel(String text) {
    return new JLabel(text, SwingConstants.LEADING);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.add(new DuoTimelineControlPanel(null));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1600, 200);
    frame.setLocation(100, 700);
    frame.setVisible(true);
  }
}
