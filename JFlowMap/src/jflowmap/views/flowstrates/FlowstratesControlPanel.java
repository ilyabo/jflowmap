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

package jflowmap.views.flowstrates;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jflowmap.ColorSchemes;
import net.miginfocom.swing.MigLayout;

import com.google.common.collect.Iterables;


/**
 * @author Ilya Boyandin
 */
public class FlowstratesControlPanel extends JPanel {

  private final FlowstratesView view;

  private final JTabbedPane tabbedPane;

  public FlowstratesControlPanel(FlowstratesView view) {
    this.view = view;

//    setLayout(new MigLayout("insets 0 0 0 0,btt,nogrid", "", ""));
    setLayout(new FlowLayout());
//    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    tabbedPane = new JTabbedPane();
    add(tabbedPane);

//    addPanel(createFilterPanel());
    addPanel(createDataPanel());
    addPanel(createHeatmapColorsPanel());
//    addPanel(createFlowLinesPanel());
  }

  private void addPanel(JPanel panel) {
    add(panel);
    panel.setBorder(BorderFactory.createTitledBorder(panel.getName()));

    panel.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
    tabbedPane.addTab(panel.getName(), panel);

//    JToolBar tb = new JToolBar(panel.getName());
//    tb.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
//    tb.add(panel);
//    tb.setFloatable(true);
//    //    tb.addSeparator();

//    add(tb);
  }


  private JPanel createPanel() {
    return new JPanel(new MigLayout("insets 0 5 0 5", "", ""));
  }

  private JPanel createDataPanel() {
    JPanel panel = createPanel();
    panel.setName("Data");


//    FlowMapGraphSet fmgs = duoTimelineView.getFlowMapGraph();


//    panel.add(new JLabel("Attribute:"), "al right");
//    panel.add(new JComboBox(new Object[] { "r", "rity" }), "height min, width min");



    final JComboBox differencesCombo = new JComboBox(ValueType.values());
    differencesCombo.setSelectedItem(view.getValueType());
    panel.add(differencesCombo, "al right");
    differencesCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        view.setValueType((ValueType) differencesCombo.getSelectedItem());
      }
    });


    panel.add(new JLabel("Max rows:"), "gapleft 5, al right");  //
    JComboBox maxRowsCombo = new JComboBox(MaxRowNumValues.values());
    maxRowsCombo.setSelectedItem(MaxRowNumValues.valueOf(view.getMaxVisibleTuples()));
    panel.add(maxRowsCombo, "height min, width min");
    maxRowsCombo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        view.setMaxVisibleTuples(((MaxRowNumValues)e.getItem()).num);
      }
    });


    panel.add(new JLabel("Group by:"), "al right, gapleft 10");
    JComboBox groupByCombo = new JComboBox(
        Iterables.toArray(view.getAggLayerNames(), Object.class));
    panel.add(groupByCombo, "height min, width min");
//    groupByCombo.setEnabled(false);
    groupByCombo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        view.clearFilters();
        view.setSelectedAggLayer((String)e.getItem());
      }
    });


//    JButton applyButton = new JButton("Apply");
//    panel.add(applyButton, "gapleft 10, span, al right, wrap");


    panel.add(new JLabel("Order:"), "gapleft 10, al right");
    JComboBox orderByCombo = new JComboBox(RowOrderings.values());
    orderByCombo.setSelectedItem(view.getRowOrdering());
    panel.add(orderByCombo, "");
    orderByCombo.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        view.setRowOrdering((RowOrderings)e.getItem());
      }
    });
//    orderByCombo.setEnabled(false);


    return panel;
  }



  private JPanel createFilterPanel() {
    JPanel panel = createPanel();
    panel.setName("Filter by name");

    panel.add(new JLabel("Source:"), "al right");
    final JTextField srcField = new JTextField();
    panel.add(srcField, "growx, wmin 150, gapright 5");

    panel.add(new JLabel("Target:"), "al right");
    final JTextField targetField = new JTextField();
    panel.add(targetField, "growx, wmin 150");

    final DocumentListener docListener = new DocumentListener() {
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

    view.addPropertyChangeListener(FlowstratesView.Properties.NODE_SELECTION,
        new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
//        flowstratesView.setCustomEdgeFilter(null);
        srcField.getDocument().removeDocumentListener(docListener);
        targetField.getDocument().removeDocumentListener(docListener);

        srcField.setText("");
        targetField.setText("");

        srcField.getDocument().addDocumentListener(docListener);
        targetField.getDocument().addDocumentListener(docListener);
      }
    });

    JButton clearBut = new JButton("Clear");
    panel.add(clearBut, "gapleft 5");
    clearBut.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        view.setCustomEdgeFilter(null);
        srcField.setText("");
        targetField.setText("");
      }
    });


    //--

    return panel;
  }

  private void doFilterBySrcDest(JTextField srcField, JTextField targetField) {
    view.setCustomEdgeFilter(FlowstratesHeatmapRowFilters.createEdgeFilter_bySrcTargetNamesAsBagOfWords(
        view.getFlowMapGraph(), srcField.getText(), targetField.getText()
        ));
  }


  private JPanel createHeatmapColorsPanel() {
    JPanel panel = createPanel();
    panel.setName("Heatmap colors");

    panel.add(new JLabel("Sequential:"), "al right");
    final JComboBox sequentialCombo =
      new JComboBox(ColorSchemes.ofType(ColorSchemes.Type.SEQUENTIAL).toArray());
    sequentialCombo.setSelectedItem(view.getSequentialColorScheme());
    sequentialCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        view.setSequentialColorScheme((ColorSchemes)sequentialCombo.getSelectedItem());
      }
    });
    panel.add(sequentialCombo, "");

    panel.add(new JLabel("Diverging:"), "gapleft 15, al right");
    final JComboBox divergingCombo =
      new JComboBox(ColorSchemes.ofType(ColorSchemes.Type.DIVERGING).toArray());
    divergingCombo.setSelectedItem(view.getDivergingColorScheme());
    panel.add(divergingCombo, "");
    divergingCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        view.setDivergingColorScheme((ColorSchemes) divergingCombo.getSelectedItem());
      }
    });

    final JCheckBox interpolateChk = new JCheckBox("Interpolate",
        view.getInterpolateColors());
    panel.add(interpolateChk, "gapleft 15");
    interpolateChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        view.setInterpolateColors(interpolateChk.isSelected());
      }
    });


//    final JCheckBox focusChk = new JCheckBox("Focus on visible rows",
//        view.getFocusOnVisibleRows());
//    panel.add(focusChk, "gapleft 15");
//    focusChk.setEnabled(true);
//    focusChk.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        view.setFocusOnVisibleRows(focusChk.isSelected());
//      }
//    });

    return panel;
  }



  private JPanel createFlowLinesPanel() {
    JPanel panel = createPanel();
    panel.setName("Flow lines");

    panel.add(new JLabel("Coloring:"), "al right");
    final JComboBox coloringCombo = new JComboBox(FlowLinesColoringMode.values());
    coloringCombo.setSelectedItem(view.getFlowLinesLayerNode().getFlowLinesColoringMode());
    coloringCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        view.getFlowLinesLayerNode().setFlowLinesColoringMode((FlowLinesColoringMode)coloringCombo.getSelectedItem());
      }
    });
    panel.add(coloringCombo, "");

    return panel;
  }

//  private static JLabel createLabel(String text) {
//    return new JLabel(text, SwingConstants.LEADING);
//  }

//  public static void main(String[] args) {
//    JFrame frame = new JFrame();
//    frame.add(new DuoTimelineControlPanel(new DuoTimelineView(null, null)));
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.setSize(1600, 200);
//    frame.setLocation(100, 700);
//    frame.setVisible(true);
//  }

  private enum MaxRowNumValues {
    _3(3), _5(5), _25(25), _50(50), _100(100), _200(200), _250(250), _500(500), _1000(1000), INFINITY(-1, "\u221e");
    final int num;
    final String str;

    private MaxRowNumValues(int num) {
      this(num, Integer.toString(num));
    }
    private MaxRowNumValues(int num, String str) {
      this.num = num;
      this.str = str;
    }
    @Override
    public String toString() {
      return str;
    }
    public static MaxRowNumValues valueOf(int num) {
      for (MaxRowNumValues n : values()) {
        if (n.num == num) return n;
      }
      return null;
    }
  }

}
