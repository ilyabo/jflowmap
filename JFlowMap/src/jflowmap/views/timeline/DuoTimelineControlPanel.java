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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jflowmap.ColorSchemes;
import jflowmap.FlowMapGraph;
import jflowmap.util.BagOfWordsFilter;
import net.miginfocom.swing.MigLayout;
import prefuse.data.Edge;
import prefuse.data.Node;

import com.google.common.base.Predicate;

/**
 * @author Ilya Boyandin
 */
public class DuoTimelineControlPanel extends JPanel {

  private final DuoTimelineView duoTimelineView;

  private final JTabbedPane tabbedPane;

  public DuoTimelineControlPanel(DuoTimelineView duoTimelineView) {
    this.duoTimelineView = duoTimelineView;

    setLayout(new MigLayout("aligny top,alignx center,gapx 25", "", "grow"));
    tabbedPane = new JTabbedPane();
    add(tabbedPane);

    addPanel(createDataPanel());
    addPanel(createFilterPanel());
    addPanel(createHeatmapColorsPanel());
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


//    panel.add(new JLabel("Attribute:"), "al right");
//    panel.add(new JComboBox(new Object[] { "r", "rity" }), "height min, width min");


    final JCheckBox differencesChk = new JCheckBox("Differences",
        duoTimelineView.getUseWeightDifferences());
    panel.add(differencesChk, "al right");
    differencesChk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        duoTimelineView.setUseWeightDifferences(differencesChk.isSelected());
      }
    });


    panel.add(new JLabel("Group by:"), "al right, gapleft 15");
    JComboBox groupByCombo = new JComboBox(new Object[] { "<None>", "r", "rity" });
    panel.add(groupByCombo, "height min, width min");
    groupByCombo.setEnabled(false);


//    JButton applyButton = new JButton("Apply");
//    panel.add(applyButton, "gapleft 10, span, al right, wrap");


    panel.add(new JLabel("Order by:"), "gapleft 10, al right");
    JComboBox orderByCombo = new JComboBox(new Object[] {
        "max magnitude in row",
        "Euclidean distance from max" });
    panel.add(orderByCombo, "");
    orderByCombo.setEnabled(false);

    return panel;
  }


  private JPanel createFilterPanel() {
    JPanel panel = new JPanel(new MigLayout("", "", /*"[pref!][grow]",*/ "[]15[]"));
    panel.setName("Filter");

    panel.add(new JLabel("Source:"), "al right, gapleft 10");
    final JTextField srcField = new JTextField();
    panel.add(srcField, "growx, wmin 150, gapright 5");

    panel.add(new JLabel("Target:"), "al right");
    final JTextField targetField = new JTextField();
    panel.add(targetField, "growx, wmin 150");

    panel.add(new JLabel("Max rows:"), "gapleft 10, al right");  //
    panel.add(new JComboBox(new Object[] { 50, 100, 250, 500, "\u221e" /*infinity*/ }),
        "height min, width min");

//    JButton applyButton = new JButton("Apply");
//    panel.add(applyButton, "gapleft 15, wrap");

    DocumentListener docListener = new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        doFilterBySrcDest(srcField, targetField);
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        doFilterBySrcDest(srcField, targetField);
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        doFilterBySrcDest(srcField, targetField);
      }
    };
    srcField.getDocument().addDocumentListener(docListener);
    targetField.getDocument().addDocumentListener(docListener);

    //--

    return panel;
  }

  private void doFilterBySrcDest(final JTextField srcField, final JTextField targetField) {
    duoTimelineView.setEdgeFilter(new Predicate<Edge>() {

      FlowMapGraph fmg = duoTimelineView.getFlowMapGraph();

      String srcQuery = srcField.getText().toLowerCase();
      String targetQuery = targetField.getText().toLowerCase();

      String[] srcQueryWords = BagOfWordsFilter.words(srcQuery);
      String[] targetQueryWords = BagOfWordsFilter.words(targetQuery);

      @Override
      public boolean apply(Edge edge) {
        Node srcNode = edge.getSourceNode();
        Node targetNode = edge.getTargetNode();

        String srcNames = fmg.getNodeLabel(srcNode);
        String targetNames = fmg.getNodeLabel(targetNode);

        return BagOfWordsFilter.ALL.apply(srcNames, srcQueryWords)  &&
               BagOfWordsFilter.ALL.apply(targetNames, targetQueryWords);
      }
    });
  }

  private JPanel createHeatmapColorsPanel() {
    JPanel panel = new JPanel(new MigLayout("", "", "[]15[]"));
    panel.setName("Heatmap colors");
    panel.add(new JLabel("Diverging:"), "al right");

    final JComboBox divergingCombo =
      new JComboBox(ColorSchemes.ofType(ColorSchemes.Type.DIVERGING).toArray());
    divergingCombo.setSelectedItem(duoTimelineView.getDivergingColorScheme());
    panel.add(divergingCombo, "");
    divergingCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        duoTimelineView.setDivergingColorScheme((ColorSchemes) divergingCombo.getSelectedItem());
      }
    });

    panel.add(new JLabel("Sequential:"), "gapleft 15, al right");
    final JComboBox sequentialCombo =
      new JComboBox(ColorSchemes.ofType(ColorSchemes.Type.SEQUENTIAL).toArray());
    sequentialCombo.setSelectedItem(duoTimelineView.getSequentialColorScheme());
    sequentialCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        duoTimelineView.setSequentialColorScheme((ColorSchemes)sequentialCombo.getSelectedItem());
      }
    });
    panel.add(sequentialCombo, "");
    final JCheckBox interpolateChk = new JCheckBox("Interpolate",
        duoTimelineView.getInterpolateColors());
    panel.add(interpolateChk, "gapleft 15");
    interpolateChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        duoTimelineView.setInterpolateColors(interpolateChk.isSelected());
      }
    });
    return panel;
  }


  private static JLabel createLabel(String text) {
    return new JLabel(text, SwingConstants.LEADING);
  }

//  public static void main(String[] args) {
//    JFrame frame = new JFrame();
//    frame.add(new DuoTimelineControlPanel(new DuoTimelineView(null, null)));
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setSize(1600, 200);
//    frame.setLocation(100, 700);
//    frame.setVisible(true);
//  }
}
