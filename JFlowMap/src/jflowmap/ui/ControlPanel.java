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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapColorSchemes;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.data.FlowMapStats;
import jflowmap.data.SeqStat;
import jflowmap.util.MathUtils;
import jflowmap.util.Pair;
import jflowmap.views.flowmap.FlowMapView;
import jflowmap.views.flowmap.VisualFlowMap;
import jflowmap.views.flowmap.VisualFlowMapModel;
import jflowmap.views.flowmap.VisualNode;
import net.miginfocom.swing.MigLayout;
import at.fhj.utils.graphics.AxisMarks;
import at.fhj.utils.swing.FancyTable;
import at.fhj.utils.swing.TableSorter;
import at.fhj.utils.swing.TableSorter.Directive;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ControlPanel {

    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JCheckBox useLogColorScaleCheckbox;
    private JSlider maxEdgeWidthSlider;
    private JSpinner maxEdgeWidthSpinner;
    private JSpinner minWeightFilterSpinner;
//    private JSlider minLengthFilterSlider;
//    private JSpinner minLengthFilterSpinner;
    private JSlider maxWeightFilterSlider;
    private JSpinner maxWeightFilterSpinner;
    private JCheckBox autoAdjustColorScaleCheckBox;
//    private JSlider maxLengthFilterSlider;
//    private JSpinner maxLengthFilterSpinner;
    private JSlider minWeightFilterSlider;
    private JCheckBox useLogWidthScaleCheckbox;
    private JComboBox datasetCombo;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox colorSchemeCombo;
    private JComboBox comboBox6;
    private JSlider edgeOpacitySlider;
    private JSpinner edgeOpacitySpinner;
    private JCheckBox mapEdgeValueToCheckBox;
    private JCheckBox mapEdgeValueToCheckBox1;
    private JSlider edgeMarkerOpacitySlider;
    private JSpinner edgeMarkerOpacitySpinner;
    private JSpinner edgeStiffnessSpinner;
    private JSpinner stepSizeSpinner;
    private JSpinner edgeCompatibilityThresholdSpinner;
    private JSpinner stepDampingFactorSpinner;
    private JSpinner stepsInCycleSpinner;
    private JButton bundleButton;
    private JButton resetBundlingButton;
    private JSpinner numberOfCyclesSpinner;
    private JButton defaultValuesButton;
    private JCheckBox directionAffectsCompatibilityCheckBox;
    private JCheckBox binaryCompatibilityCheckBox;
    private JCheckBox inverseQuadraticModelCheckBox;
    private JCheckBox repulsiveEdgesCheckBox;
    private JCheckBox simpleCompatibilityMeasureCheckBox;
    private JCheckBox showNodesCheckBox;
    private JSpinner repulsionSpinner;
    private JCheckBox edgeValueAffectsAttractionCheckBox;
    private JCheckBox fillEdgesWithGradientCheckBox;
    private JCheckBox showDirectionMarkersCheckBox;
    private JSlider edgeMarkerSizeSlider;
    private JSpinner edgeMarkerSizeSpinner;
    private JCheckBox proportionalDirectionMarkersCheckBox;
    private JLabel edgeMarkerSizeLabel;
    private JLabel edgeMarkerOpacityLabel;
    private JTable flowsTable;
//    private JButton aggregateEdgesButton;
    private final FlowMapView flowMapView;
    private boolean initializing;
    private ForceDirectedBundlerParameters fdBundlingParams;
    private boolean modelsInitialized;
    private FlowsTableModel flowsTableModel;
    private TableSorter flowsTableSorter;
    private VisualFlowMap visualFlowMap;
    private final FlowMapAttrSpec attrSpec;


    public ControlPanel(FlowMapView flowMap, FlowMapAttrSpec attrSpec) {
        this.flowMapView = flowMap;
        this.attrSpec = attrSpec;
        $$$setupUI$$$();
        setupUI();

        loadVisualFlowMap(flowMap.getVisualFlowMap());
        initUIChangeListeners();

        updateDirectionAffectsCompatibilityCheckBox();
        updateDirectionAffectsCompatibilityCheckBox();
        updateRepulsionSpinner();
        updateMarkersInputs();
    }

    private void setSelectedFlowWeightAttr(String selAttr) {
      flowMapView.setSelectedFlowWeightAttr(selAttr);
      updateFlowsTable();
    }

    private void updateFlowsTable() {
      List<Directive> sort = flowsTableSorter.getSortingColumns();
      flowsTableModel.setVisualFlowMap(flowMapView.getVisualFlowMap());
      flowsTableSorter.setSortingColumns(sort); // prevent the column sortings being reset
    }

    private void setupUI() {
      tabbedPane1.addTab("Animation", createAnimationTab());
    }

    private JPanel createAnimationTab() {
      JPanel panel = new JPanel(new MigLayout(
          "insets 20,alignx center", "[grow 0][grow 0,center,65][grow 0][800, grow 0]", ""));


      final List<String> attrs = attrSpec.getFlowWeightAttrs();
      int selIndex = attrs.indexOf(flowMapView.getVisualFlowMap().getFlowWeightAttr());
//      final JLabel selAttrLabel = new JLabel(attrs.get(selIndex));
//      selAttrLabel.setFont(new Font("Arial", Font.BOLD, 42));

      final JSlider attrSlider = new JSlider(0, attrs.size() - 1, selIndex);
//      attrSlider.getModel().set

      Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
      int size = attrs.size();
      labels.put(0, new JLabel(attrs.get(0)));
      labels.put(size / 2, new JLabel(attrs.get(size / 2)));
      labels.put(size - 1, new JLabel(attrs.get(size - 1)));
      attrSlider.setLabelTable(labels);
      attrSlider.setMajorTickSpacing(attrs.size() < 50  ?  1  : (int)Math.ceil(attrs.size() / 50));
      attrSlider.setPaintTicks(true);
      attrSlider.setPaintLabels(true);
      attrSlider.setSnapToTicks(true);
      flowMapView.getVisualFlowMap().addPropertyChangeListener(
          VisualFlowMap.PROPERTY_FLOW_WEIGHT_ATTR,
          new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              String sel = (String)evt.getNewValue();
              attrSlider.setValue(attrs.indexOf(sel));
              datasetCombo.setSelectedItem(sel);
            }
          });

      final JSlider speedSlider = new JSlider(0, 10, 5);
      Hashtable<Integer, JComponent> speedLabels = new Hashtable<Integer, JComponent>();
      speedLabels.put(speedSlider.getMaximum(), createTinyLabel("Fast "));
      speedLabels.put(speedSlider.getMinimum(), createTinyLabel("Slow "));
      speedSlider.setLabelTable(speedLabels);
      speedSlider.setOrientation(JSlider.VERTICAL);
      speedSlider.setPaintTicks(true);
      speedSlider.setPaintLabels(true);

      final JButton rewindBut = new JButton("|<<");
      rewindBut.setFont(rewindBut.getFont().deriveFont(rewindBut.getFont().getSize2D()*0.7f));
      rewindBut.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          flowMapView.setSelectedFlowWeightAttr(attrs.get(0));
        }
      });

      final JButton playStopBut = new JButton("Play");
      final Runnable runWhenFinished = new Runnable() {
        @Override
        public void run() {
          rewindBut.setEnabled(true);
          attrSlider.setEnabled(true);
          speedSlider.setEnabled(true);
          playStopBut.setText("Play");
          updateFlowsTable();
        }
      };


      playStopBut.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          VisualFlowMap vfm = flowMapView.getVisualFlowMap();
          if (playStopBut.getText().equals("Play")) {
            if (attrSlider.getValue() == attrSlider.getMaximum()) {
              attrSlider.setValue(attrSlider.getMinimum());
            }
            rewindBut.setEnabled(false);
            attrSlider.setEnabled(false);
            speedSlider.setEnabled(false);
            playStopBut.setText("Stop");
            vfm.startValueAnimation(runWhenFinished,
                attrSlider.getValue(),
                0.5 + speedSlider.getValue() / 4.0);
          } else {
            vfm.stopValueAnimation();
            runWhenFinished.run();
          }
        }
      });


//      JButton fffBut = new JButton(">>|");
//      fffBut.setFont(fffBut.getFont().deriveFont(fffBut.getFont().getSize2D()*0.7f));
//      fffBut.addActionListener(new ActionListener() {
//        @Override
//        public void actionPerformed(ActionEvent e) {
//          flowMapView.setSelectedFlowWeightAttr(attrs.get(attrs.size()-1));
//        }
//      });

      attrSlider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          final JSlider slider = (JSlider)e.getSource();
          final String attr = attrs.get(slider.getValue());
          flowMapView.setSelectedFlowWeightAttr(attr);
          flowMapView.getVisualFlowMap().setFlowWeightAttrLabelVisibile(true);
        }
      });

      panel.add(rewindBut, "aligny center");
      panel.add(playStopBut, "aligny center");
      panel.add(speedSlider, "hmax 60, growx 0");
//      panel.add(fffBut, "aligny center");

      panel.add(attrSlider, "gapx 20, growx 100, wmax 800");
//      panel.add(selAttrLabel, "gap 20, aligny top");

      return panel;
    }

    private JLabel createTinyLabel(String text) {
      JLabel label = new JLabel(text);
      label.setFont(new Font(label.getFont().getFamily(), Font.PLAIN, (int) (label.getFont().getSize()*.75)));
      label.setHorizontalTextPosition(JLabel.LEFT);
      return label;
    }

    public void loadVisualFlowMap(VisualFlowMap newVisualFlowMap) {
        // attach listeners
        if (visualFlowMap != null) {
            removeVisualFlowMapListeners(visualFlowMap);
        }
        visualFlowMap = newVisualFlowMap;
        attachVisualFlowMapListeners(newVisualFlowMap);

        // load data
        fdBundlingParams = visualFlowMap.createForceDirectedBundlerParameters();
        if (!modelsInitialized) {
            initModelsOnce();
            modelsInitialized = true;
        }
        initModels();
        setData(visualFlowMap.getModel());
    }

    private void updateRepulsionSpinner() {
        repulsionSpinner.setEnabled(repulsiveEdgesCheckBox.isSelected());
    }

    private void updateSimpleCompatibilityMeasureCheckBox() {
        if (repulsiveEdgesCheckBox.isSelected()) {
            simpleCompatibilityMeasureCheckBox.setSelected(false);
            simpleCompatibilityMeasureCheckBox.setEnabled(false);
        } else {
            simpleCompatibilityMeasureCheckBox.setEnabled(true);
        }
    }

    private void updateDirectionAffectsCompatibilityCheckBox() {
        if (simpleCompatibilityMeasureCheckBox.isSelected() || repulsiveEdgesCheckBox.isSelected()) {
            directionAffectsCompatibilityCheckBox.setSelected(false);
            directionAffectsCompatibilityCheckBox.setEnabled(false);
        } else {
            directionAffectsCompatibilityCheckBox.setEnabled(true);
        }
    }

    private void updateMarkersInputs() {
        boolean showMarkers = showDirectionMarkersCheckBox.isSelected();
        proportionalDirectionMarkersCheckBox.setEnabled(showMarkers);
        edgeMarkerSizeSlider.setEnabled(showMarkers);
        edgeMarkerSizeSpinner.setEnabled(showMarkers);
        edgeMarkerSizeLabel.setEnabled(showMarkers);
        edgeMarkerOpacitySlider.setEnabled(showMarkers);
        edgeMarkerOpacitySpinner.setEnabled(showMarkers);
        edgeMarkerOpacityLabel.setEnabled(showMarkers);
    }

    private void initModelsOnce() {
        if (attrSpec != null) {
            datasetCombo.setModel(new DefaultComboBoxModel(attrSpec.getFlowWeightAttrs().toArray()));
        } else {
            // TODO: move dataset combo out of the control panel
        }
    }

    public void initEdgeBundlingModels() {
        numberOfCyclesSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getNumCycles(), 1, 100, 1));
        stepsInCycleSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getI(), 1, 1000, 1));
        edgeCompatibilityThresholdSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getEdgeCompatibilityThreshold(), 0.0, 1.0, 0.1));

        double s = fdBundlingParams.getS();
        double sExp = AxisMarks.ordAlpha(s);
        double sStep = sExp;
//    double sStep = sExp / 100;
        double sMax = sExp * 100;
        stepSizeSpinner.setModel(new SpinnerNumberModel(s, 0.0, sMax, sStep));

        edgeStiffnessSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getK(), 0.0, 1000.0, 1.0));

        stepDampingFactorSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getStepDampingFactor(), 0.0, 1.0, 0.1));
        directionAffectsCompatibilityCheckBox.setSelected(fdBundlingParams.getDirectionAffectsCompatibility());
        binaryCompatibilityCheckBox.setSelected(fdBundlingParams.getBinaryCompatibility());
        inverseQuadraticModelCheckBox.setSelected(fdBundlingParams.getUseInverseQuadraticModel());
        repulsiveEdgesCheckBox.setSelected(fdBundlingParams.getUseRepulsionForOppositeEdges());
        simpleCompatibilityMeasureCheckBox.setSelected(fdBundlingParams.getUseSimpleCompatibilityMeasure());
        edgeValueAffectsAttractionCheckBox.setSelected(fdBundlingParams.getEdgeValueAffectsAttraction());
        repulsionSpinner.setModel(new SpinnerNumberModel(fdBundlingParams.getRepulsionAmount(), 0.0, 1.0, 0.1));
    }

    private void initModels() {
        initializing = true;
        initFilterModels();
        initAestheticsModels();
        initEdgeBundlingModels();
        initializing = false;
    }

    private void initAestheticsModels() {
        Pair<SpinnerModel, BoundedRangeModel> edgeOpacityModels =
                new BoundSpinnerSliderModels<Integer>(
                        0, 0, 255, 1, BoundSpinnerSliderModels.MAP_ID_INTEGER
                ).build();
        edgeOpacitySpinner.setModel(edgeOpacityModels.first());
        edgeOpacitySlider.setModel(edgeOpacityModels.second());

        Pair<SpinnerModel, BoundedRangeModel> edgeMarkerSizeModels =
                new BoundSpinnerSliderModels<Double>(
                        .0, .0, 0.5, 0.01,
                        BoundSpinnerSliderModels.createLinearMapping(100.0)
                ).build();
        edgeMarkerSizeSpinner.setModel(edgeMarkerSizeModels.first());
        edgeMarkerSizeSlider.setModel(edgeMarkerSizeModels.second());

        Pair<SpinnerModel, BoundedRangeModel> edgeMarkerOpacityModels =
                new BoundSpinnerSliderModels<Integer>(
                        0, 0, 255, 1, BoundSpinnerSliderModels.MAP_ID_INTEGER
                ).build();
        edgeMarkerOpacitySpinner.setModel(edgeMarkerOpacityModels.first());
        edgeMarkerOpacitySlider.setModel(edgeMarkerOpacityModels.second());

        Pair<SpinnerModel, BoundedRangeModel> maxEdgeWidthModels =
                new BoundSpinnerSliderModels<Double>(
                        .0, .0, 100.0, 1.0, BoundSpinnerSliderModels.MAP_ID_DOUBLE
                ).build();
        maxEdgeWidthSpinner.setModel(maxEdgeWidthModels.first());
        maxEdgeWidthSlider.setModel(maxEdgeWidthModels.second());

        colorSchemeCombo.setModel(new DefaultComboBoxModel(FlowMapColorSchemes.values()));
        updateColorScheme();
    }

    public void updateColorScheme() {
      colorSchemeCombo.setSelectedItem(FlowMapColorSchemes.findByScheme(flowMapView.getColorScheme()));
    }

    private void initFilterModels() {
        FlowMapStats stats = getGraphStats();

        SeqStat weightStats = stats.getEdgeWeightStats();

        final double minWeight = weightStats.getMin();
        final double maxWeight = weightStats.getMax();

        double weightFilterLogMappingShift = (minWeight >= 1.0 ? 0 : 1.0 - minWeight);
        Pair<SpinnerModel, BoundedRangeModel> minWeightFilterModels =
                new BoundSpinnerSliderModels<Double>(
                        minWeight, minWeight, maxWeight, 1.0,
                        BoundSpinnerSliderModels.createLogMapping(2, weightFilterLogMappingShift)
                ).build();
        minWeightFilterSpinner.setModel(minWeightFilterModels.first());
        minWeightFilterSlider.setModel(minWeightFilterModels.second());

        Pair<SpinnerModel, BoundedRangeModel> maxWeightFilterModels =
                new BoundSpinnerSliderModels<Double>(
                        maxWeight, minWeight, maxWeight, 1.0,
                        BoundSpinnerSliderModels.createLogMapping(2, 1.0 - minWeight)
                ).build();
        maxWeightFilterSpinner.setModel(maxWeightFilterModels.first());
        maxWeightFilterSlider.setModel(maxWeightFilterModels.second());

        SeqStat lengthStats = stats.getEdgeLengthStats();

//        Pair<SpinnerModel, BoundedRangeModel> minLengthFilterModels =
//                new BoundSpinnerSliderModels<Double>(
//                        lengthStats.getMin(), lengthStats.getMin(), lengthStats.getMax(), 1.0,
//                        BoundSpinnerSliderModels.MAP_ID_DOUBLE
//                ).build();
//        minLengthFilterSpinner.setModel(minLengthFilterModels.first());
//        minLengthFilterSlider.setModel(minLengthFilterModels.second());
//
//        Pair<SpinnerModel, BoundedRangeModel> maxLengthFilterModels =
//                new BoundSpinnerSliderModels<Double>(
//                        lengthStats.getMax(), lengthStats.getMin(), lengthStats.getMax(), 1.0,
//                        BoundSpinnerSliderModels.MAP_ID_DOUBLE
//                ).build();
//        maxLengthFilterSpinner.setModel(maxLengthFilterModels.first());
//        maxLengthFilterSlider.setModel(maxLengthFilterModels.second());
    }

    public void setData(VisualFlowMapModel data) {
        datasetCombo.setSelectedItem(flowMapView.getVisualFlowMap().getFlowWeightAttr());

        autoAdjustColorScaleCheckBox.setSelected(data.getAutoAdjustColorScale());
        useLogWidthScaleCheckbox.setSelected(data.getUseLogWidthScale());
        useLogColorScaleCheckbox.setSelected(data.getUseLogColorScale());

//        minLengthFilterSpinner.setValue(data.getEdgeLengthFilterMin());
//        maxLengthFilterSpinner.setValue(data.getEdgeLengthFilterMax());

        minWeightFilterSpinner.setValue(data.getEdgeWeightFilterMin());
        maxWeightFilterSpinner.setValue(data.getEdgeWeightFilterMax());

        edgeOpacitySpinner.setValue(data.getEdgeAlpha());
//    edgeOpacitySlider.setValue(data.getEdgeAlpha());
        edgeMarkerOpacitySpinner.setValue(data.getDirectionMarkerAlpha());
        edgeMarkerSizeSpinner.setValue(data.getDirectionMarkerSize());

        showDirectionMarkersCheckBox.setSelected(data.getShowDirectionMarkers());
        showNodesCheckBox.setSelected(data.getShowNodes());
        fillEdgesWithGradientCheckBox.setSelected(data.getFillEdgesWithGradient());
        proportionalDirectionMarkersCheckBox.setSelected(data.getUseProportionalDirectionMarkers());

        maxEdgeWidthSpinner.setValue(data.getMaxEdgeWidth());
//    maxEdgeWidthSlider.setValue((int) Math.round(data.getMaxEdgeWidth()));
    }

    private void initUIChangeListeners() {
        initDatasetListeners();
        initFilterListeners();
        initScalesListeners();
        initAestheticsListeners();
        initEdgeBundlingListeners();
    }

    private void initDatasetListeners() {
        datasetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (initializing) return;
                String selAttr = (String) datasetCombo.getSelectedItem();
                setSelectedFlowWeightAttr(selAttr);
//                jFlowMap.fitInView();
            }
        });
    }

    private void initFilterListeners() {
        // Edge value filter
        minWeightFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
//        minWeightFilterSlider.setValue(toValueEdgeFilterSliderValue((Double) minWeightFilterSpinner.getValue()));
                getFlowMapModel().setEdgeWeightFilterMin((Double) minWeightFilterSpinner.getValue());
            }
        });

        visualFlowMap.getModel().addPropertyChangeListener(
            VisualFlowMapModel.PROPERTY_VALUE_FILTER_MIN, new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent evt) {
                minWeightFilterSpinner.setValue(visualFlowMap.getModel().getEdgeWeightFilterMin());
              }
            });
//    minWeightFilterSlider.addChangeListener(new ChangeListener() {
//      public void stateChanged(ChangeEvent e) {
//        if (initializing) return;
//        double value = fromValueEdgeFilterSliderValue(minWeightFilterSlider.getValue());
//        getFlowMapModel().setEdgeWeightFilterMin(value);
//        minWeightFilterSpinner.setValue(value);
//      }
//    });
        maxWeightFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setEdgeWeightFilterMax((Double) maxWeightFilterSpinner.getValue());
            }
        });
//    maxWeightFilterSlider.addChangeListener(new ChangeListener() {
//      public void stateChanged(ChangeEvent e) {
//        if (initializing) return;
//        double value = fromValueEdgeFilterSliderValue(maxWeightFilterSlider.getValue());
//        maxWeightFilterSpinner.setValue(value);
//      }
//    });


//        // Edge length filter
//        minLengthFilterSpinner.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                if (initializing) return;
//                getFlowMapModel().setEdgeLengthFilterMin((Double) minLengthFilterSpinner.getValue());
//            }
//        });
//        maxLengthFilterSpinner.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                if (initializing) return;
//                getFlowMapModel().setEdgeLengthFilterMax((Double) maxLengthFilterSpinner.getValue());
//            }
//        });
    }

    private void initScalesListeners() {
        useLogColorScaleCheckbox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setUseLogColorScale(useLogColorScaleCheckbox.isSelected());
            }
        });
        useLogWidthScaleCheckbox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setUseLogWidthScale(useLogWidthScaleCheckbox.isSelected());
            }
        });
    }

    private void initAestheticsListeners() {
        // Aesthetics
        fillEdgesWithGradientCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setFillEdgesWithGradient(
                        fillEdgesWithGradientCheckBox.isSelected());
            }
        });
        showDirectionMarkersCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setShowDirectionMarkers(
                        showDirectionMarkersCheckBox.isSelected());
                updateMarkersInputs();
            }
        });
        showNodesCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setShowNodes(showNodesCheckBox.isSelected());
            }
        });
        proportionalDirectionMarkersCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setUseProportionalDirectionMarkers(
                        proportionalDirectionMarkersCheckBox.isSelected());
            }
        });

        // Direction marker size
        edgeMarkerSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setDirectionMarkerSize((Double) edgeMarkerSizeSpinner.getValue());
            }
        });

        // Edge opacity
        edgeOpacitySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setEdgeAlpha((Integer) edgeOpacitySpinner.getValue());
            }
        });

        // Edge marker opacity
        edgeMarkerOpacitySpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setDirectionMarkerAlpha((Integer) edgeMarkerOpacitySpinner.getValue());
            }
        });

        // Edge width
        maxEdgeWidthSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setMaxEdgeWidth((Double) maxEdgeWidthSpinner.getValue());
            }
        });

        colorSchemeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                flowMapView.setColorScheme(((FlowMapColorSchemes) e.getItem()).getScheme());
            }
        });
    }

    private void initEdgeBundlingListeners() {
        // Edge Bundling
        bundleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fdBundlingParams.setNumCycles((Integer) numberOfCyclesSpinner.getValue());
                fdBundlingParams.setI((Integer) stepsInCycleSpinner.getValue());
                fdBundlingParams.setK((Double) edgeStiffnessSpinner.getValue());
                fdBundlingParams.setEdgeCompatibilityThreshold((Double) edgeCompatibilityThresholdSpinner.getValue());
                fdBundlingParams.setS((Double) stepSizeSpinner.getValue());
                fdBundlingParams.setStepDampingFactor((Double) stepDampingFactorSpinner.getValue());
                fdBundlingParams.setDirectionAffectsCompatibility(directionAffectsCompatibilityCheckBox.isSelected());
                fdBundlingParams.setBinaryCompatibility(binaryCompatibilityCheckBox.isSelected());
                fdBundlingParams.setUseInverseQuadraticModel(inverseQuadraticModelCheckBox.isSelected());
                fdBundlingParams.setUseRepulsionForOppositeEdges(repulsiveEdgesCheckBox.isSelected());
                fdBundlingParams.setUseSimpleCompatibilityMeasure(simpleCompatibilityMeasureCheckBox.isSelected());
                fdBundlingParams.setRepulsionAmount((Double) repulsionSpinner.getValue());
                fdBundlingParams.setEdgeValueAffectsAttraction(edgeValueAffectsAttractionCheckBox.isSelected());
                getVisualFlowMap().bundleEdges(fdBundlingParams);
            }
        });
        resetBundlingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().resetBundling();
            }
        });
//    aggregateBundledEdgesButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        getVisualFlowMap().aggregateBundledEdges();
//      }
//    });
        defaultValuesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fdBundlingParams.resetToDefaults();
                initEdgeBundlingModels();
            }
        });
        simpleCompatibilityMeasureCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateDirectionAffectsCompatibilityCheckBox();
            }
        });
        repulsiveEdgesCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateSimpleCompatibilityMeasureCheckBox();
                updateDirectionAffectsCompatibilityCheckBox();
                updateRepulsionSpinner();
            }
        });
//        aggregateEdgesButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                getVisualFlowMap().aggregateBundledEdges();
//            }
//        });
    }

//  private void loadFlowMap(DatasetSpec dataset) {
//    VisualFlowMap visualFlowMap = jFlowMap.loadFlowMap(dataset);
//    jFlowMap.setVisualFlowMap(visualFlowMap);
//    jFlowMap.fitFlowMapInView();
//    loadVisualFlowMap(visualFlowMap);
//  }

    private final List<PropertyChangeListener> visualFlowMapListeners =
            new ArrayList<PropertyChangeListener>();

    private void attachVisualFlowMapListeners(VisualFlowMap visualFlowMap) {
        PropertyChangeListener selectionListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                VisualNode selectedNode = getVisualFlowMap().getSelectedNode();
                if (selectedNode == null) {
                    flowsTableModel.showAllVisualEdges();
                } else {
                    flowsTableModel.setVisualEdges(selectedNode.getEdges());
                }
                flowsTableSorter.setSortingStatus(2, TableSorter.DESCENDING);
            }
        };
        visualFlowMap.addPropertyChangeListener(
                VisualFlowMap.Attributes.NODE_SELECTION.name(),
                selectionListener
        );
        visualFlowMapListeners.add(selectionListener);
    }

    private void removeVisualFlowMapListeners(VisualFlowMap visualFlowMap) {
        for (PropertyChangeListener li : visualFlowMapListeners) {
            visualFlowMap.removePropertyChangeListener(li);
        }
        visualFlowMapListeners.clear();
    }

    public VisualFlowMapModel getFlowMapModel() {
        return getVisualFlowMap().getModel();
    }

    private VisualFlowMap getVisualFlowMap() {
        return flowMapView.getVisualFlowMap();
    }

    private FlowMapStats getGraphStats() {
        return getVisualFlowMap().getStats();
    }

    public JPanel getPanel() {
        return panel1;
    }

    private void createUIComponents() {
        // custom component creation code here

        // flowsTable
        flowsTableModel = new FlowsTableModel();
        flowsTableSorter = new TableSorter(flowsTableModel);
        flowsTable = new FancyTable(flowsTableSorter);
        flowsTable.setAutoCreateColumnsFromModel(false);
        flowsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        flowsTableSorter.setColumnSortable(0, true);
        flowsTableSorter.setColumnSortable(1, true);
        flowsTableSorter.setColumnSortable(2, true);
        flowsTableSorter.setTableHeader(flowsTable.getTableHeader());
        flowsTableSorter.setColumnComparator(Double.class, MathUtils.COMPARE_DOUBLES_SMALLEST_IS_NAN);
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:187px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:119px:noGrow,left:20dlu:noGrow,fill:max(d;4px):grow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:min(p;200px):grow"));
        tabbedPane1.addTab("Data", panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        datasetCombo = new JComboBox();
        CellConstraints cc = new CellConstraints();
        panel2.add(datasetCombo, cc.xy(3, 1));
        final JLabel label1 = new JLabel();
        label1.setText("Flow weight attr:");
        panel2.add(label1, cc.xy(1, 1));
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        panel2.add(separator1, cc.xywh(5, 1, 1, 9, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label2 = new JLabel();
        label2.setEnabled(false);
        label2.setText("Edge weight field:");
        panel2.add(label2, cc.xy(7, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox4 = new JComboBox();
        comboBox4.setEnabled(false);
        panel2.add(comboBox4, cc.xy(9, 1));
        final JLabel label3 = new JLabel();
        label3.setEnabled(false);
        label3.setText("Node label field:");
        panel2.add(label3, cc.xy(7, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox6 = new JComboBox();
        comboBox6.setEnabled(false);
        panel2.add(comboBox6, cc.xy(9, 3));
        final JSeparator separator2 = new JSeparator();
        separator2.setOrientation(1);
        panel2.add(separator2, cc.xywh(10, 1, 1, 9, CellConstraints.CENTER, CellConstraints.FILL));
        final JTabbedPane tabbedPane3 = new JTabbedPane();
        tabbedPane3.setTabPlacement(3);
        panel2.add(tabbedPane3, cc.xywh(11, 1, 1, 9, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JScrollPane scrollPane1 = new JScrollPane();
        tabbedPane3.addTab("Flows", scrollPane1);
        scrollPane1.setBorder(BorderFactory.createTitledBorder(""));
        flowsTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane1.setViewportView(flowsTable);
        final JLabel label4 = new JLabel();
        label4.setEnabled(false);
        label4.setText("Node X coord field:");
        panel2.add(label4, cc.xy(7, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox2 = new JComboBox();
        comboBox2.setEnabled(false);
        panel2.add(comboBox2, cc.xy(9, 5));
        final JLabel label5 = new JLabel();
        label5.setEnabled(false);
        label5.setText("Node Y coord field:");
        panel2.add(label5, cc.xy(7, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        comboBox3 = new JComboBox();
        comboBox3.setEnabled(false);
        panel2.add(comboBox3, cc.xy(9, 7));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow(2.0),left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,right:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:p:noGrow", "center:26px:noGrow,top:4dlu:noGrow,center:24px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:5dlu:noGrow,center:d:noGrow"));
        tabbedPane1.addTab("Filter", panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        minWeightFilterSpinner = new JSpinner();
        panel3.add(minWeightFilterSpinner, cc.xy(5, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator3 = new JSeparator();
        separator3.setOrientation(1);
        panel3.add(separator3, cc.xywh(7, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
//        final JLabel label6 = new JLabel();
//        label6.setText("Min length:");
//        panel3.add(label6, cc.xy(9, 1));
//        minLengthFilterSlider = new JSlider();
//        panel3.add(minLengthFilterSlider, cc.xy(11, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
//        minLengthFilterSpinner = new JSpinner();
//        panel3.add(minLengthFilterSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label7 = new JLabel();
        label7.setText("Max edge weight:");
        panel3.add(label7, cc.xy(1, 3));
        maxWeightFilterSlider = new JSlider();
        panel3.add(maxWeightFilterSlider, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.CENTER));
        maxWeightFilterSpinner = new JSpinner();
        panel3.add(maxWeightFilterSpinner, cc.xy(5, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        autoAdjustColorScaleCheckBox = new JCheckBox();
        autoAdjustColorScaleCheckBox.setEnabled(false);
        autoAdjustColorScaleCheckBox.setText("Auto adjust color scale");
        panel3.add(autoAdjustColorScaleCheckBox, cc.xyw(3, 5, 3));
//        final JLabel label8 = new JLabel();
//        label8.setText("Max length:");
//        panel3.add(label8, cc.xy(9, 3));
//        maxLengthFilterSlider = new JSlider();
//        panel3.add(maxLengthFilterSlider, cc.xy(11, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
//        maxLengthFilterSpinner = new JSpinner();
//        panel3.add(maxLengthFilterSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        panel3.add(panel4, cc.xy(5, 7));
        final JLabel label9 = new JLabel();
        label9.setText("Min edge weight:");
        panel3.add(label9, cc.xy(1, 1));
        minWeightFilterSlider = new JSlider();
        panel3.add(minWeightFilterSlider, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FormLayout("fill:d:noGrow,left:p:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:20dlu:noGrow,fill:max(d;4px):grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:24px:noGrow,top:6dlu:noGrow,top:4dlu:noGrow"));
        tabbedPane1.addTab("Scales", panel5);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator4 = new JSeparator();
        separator4.setOrientation(1);
        panel5.add(separator4, cc.xywh(3, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        useLogWidthScaleCheckbox = new JCheckBox();
        useLogWidthScaleCheckbox.setEnabled(true);
        useLogWidthScaleCheckbox.setText("Use log width scale");
        panel5.add(useLogWidthScaleCheckbox, cc.xy(2, 1));
        useLogColorScaleCheckbox = new JCheckBox();
        useLogColorScaleCheckbox.setEnabled(true);
        useLogColorScaleCheckbox.setText("Use log color scale");
        panel5.add(useLogColorScaleCheckbox, cc.xyw(1, 3, 2));
        mapEdgeValueToCheckBox = new JCheckBox();
        mapEdgeValueToCheckBox.setEnabled(false);
        mapEdgeValueToCheckBox.setText("Map edge value to color");
        panel5.add(mapEdgeValueToCheckBox, cc.xy(5, 1));
        mapEdgeValueToCheckBox1 = new JCheckBox();
        mapEdgeValueToCheckBox1.setEnabled(false);
        mapEdgeValueToCheckBox1.setText("Map edge value to width");
        panel5.add(mapEdgeValueToCheckBox1, cc.xy(5, 3));
        final JSeparator separator5 = new JSeparator();
        separator5.setOrientation(1);
        panel5.add(separator5, cc.xywh(6, 1, 1, 5, CellConstraints.CENTER, CellConstraints.FILL));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:110px:noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):grow,left:4dlu:noGrow,fill:max(m;50px):noGrow", "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Aesthetics", panel6);
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JLabel label10 = new JLabel();
        label10.setEnabled(true);
        label10.setText("Color scheme:");
        panel6.add(label10, cc.xy(1, 1));
        colorSchemeCombo = new JComboBox();
        colorSchemeCombo.setEnabled(true);
        panel6.add(colorSchemeCombo, cc.xy(3, 1));
        final JSeparator separator6 = new JSeparator();
        separator6.setOrientation(1);
        panel6.add(separator6, cc.xywh(5, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label11 = new JLabel();
        label11.setText("Edge width:");
        panel6.add(label11, cc.xy(10, 1, CellConstraints.RIGHT, CellConstraints.CENTER));
        maxEdgeWidthSlider = new JSlider();
        maxEdgeWidthSlider.setPaintLabels(false);
        maxEdgeWidthSlider.setPaintTicks(false);
        maxEdgeWidthSlider.setPaintTrack(true);
        panel6.add(maxEdgeWidthSlider, cc.xy(12, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxEdgeWidthSpinner = new JSpinner();
        panel6.add(maxEdgeWidthSpinner, cc.xy(14, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label12 = new JLabel();
        label12.setText("Edge opacity:");
        panel6.add(label12, cc.xy(10, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeOpacitySlider = new JSlider();
        panel6.add(edgeOpacitySlider, cc.xy(12, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeOpacitySpinner = new JSpinner();
        panel6.add(edgeOpacitySpinner, cc.xy(14, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        showNodesCheckBox = new JCheckBox();
        showNodesCheckBox.setText("Show nodes");
        panel6.add(showNodesCheckBox, cc.xy(7, 1));
        final JSeparator separator7 = new JSeparator();
        separator7.setOrientation(1);
        panel6.add(separator7, cc.xywh(8, 1, 1, 7, CellConstraints.CENTER, CellConstraints.FILL));
        showDirectionMarkersCheckBox = new JCheckBox();
        showDirectionMarkersCheckBox.setText("Show direction markers");
        panel6.add(showDirectionMarkersCheckBox, cc.xy(7, 5));
        fillEdgesWithGradientCheckBox = new JCheckBox();
        fillEdgesWithGradientCheckBox.setText("Fill edges with gradient");
        panel6.add(fillEdgesWithGradientCheckBox, cc.xy(7, 3));
        proportionalDirectionMarkersCheckBox = new JCheckBox();
        proportionalDirectionMarkersCheckBox.setText("Proportional direction markers");
        panel6.add(proportionalDirectionMarkersCheckBox, cc.xy(7, 7));
        edgeMarkerSizeSpinner = new JSpinner();
        panel6.add(edgeMarkerSizeSpinner, cc.xy(14, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerSizeSlider = new JSlider();
        panel6.add(edgeMarkerSizeSlider, cc.xy(12, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySpinner = new JSpinner();
        panel6.add(edgeMarkerOpacitySpinner, cc.xy(14, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerOpacitySlider = new JSlider();
        panel6.add(edgeMarkerOpacitySlider, cc.xy(12, 7, CellConstraints.FILL, CellConstraints.DEFAULT));
        edgeMarkerSizeLabel = new JLabel();
        edgeMarkerSizeLabel.setText("Direction marker size:");
        panel6.add(edgeMarkerSizeLabel, cc.xy(10, 5, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeMarkerOpacityLabel = new JLabel();
        edgeMarkerOpacityLabel.setText("Direction marker opacity:");
        panel6.add(edgeMarkerOpacityLabel, cc.xy(10, 7, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new FormLayout("fill:d:noGrow,left:6dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:12px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:p:noGrow,left:12dlu:noGrow,fill:p:noGrow,fill:d:noGrow,left:d:noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:25px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane1.addTab("Edge bundling", panel7);
        panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSeparator separator8 = new JSeparator();
        separator8.setOrientation(1);
        panel7.add(separator8, cc.xywh(9, 1, 1, 8, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label13 = new JLabel();
        label13.setHorizontalAlignment(4);
        label13.setText("Step damping factor:");
        panel7.add(label13, cc.xy(11, 3));
        stepDampingFactorSpinner = new JSpinner();
        panel7.add(stepDampingFactorSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        stepSizeSpinner = new JSpinner();
        panel7.add(stepSizeSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label14 = new JLabel();
        label14.setHorizontalAlignment(4);
        label14.setText("Step size (S):");
        panel7.add(label14, cc.xy(11, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JLabel label15 = new JLabel();
        label15.setHorizontalAlignment(4);
        label15.setText("Edge stiffness (K):");
        panel7.add(label15, cc.xy(5, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        edgeStiffnessSpinner = new JSpinner();
        panel7.add(edgeStiffnessSpinner, cc.xy(7, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JSeparator separator9 = new JSeparator();
        separator9.setOrientation(1);
        panel7.add(separator9, cc.xywh(14, 1, 1, 8, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label16 = new JLabel();
        label16.setHorizontalAlignment(4);
        label16.setText("Number of cycles:");
        panel7.add(label16, cc.xy(5, 1));
        numberOfCyclesSpinner = new JSpinner();
        panel7.add(numberOfCyclesSpinner, cc.xy(7, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label17 = new JLabel();
        label17.setHorizontalAlignment(4);
        label17.setText("Compatibility threshold:");
        panel7.add(label17, cc.xy(5, 5));
        edgeCompatibilityThresholdSpinner = new JSpinner();
        panel7.add(edgeCompatibilityThresholdSpinner, cc.xy(7, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        final JLabel label18 = new JLabel();
        label18.setHorizontalAlignment(4);
        label18.setText("Steps in 1st cycle (I):");
        panel7.add(label18, cc.xy(11, 5));
        stepsInCycleSpinner = new JSpinner();
        panel7.add(stepsInCycleSpinner, cc.xy(13, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        bundleButton = new JButton();
        bundleButton.setText("Bundle");
        panel7.add(bundleButton, cc.xy(1, 1));
        resetBundlingButton = new JButton();
        resetBundlingButton.setText("Reset");
        panel7.add(resetBundlingButton, cc.xy(1, 3));
        defaultValuesButton = new JButton();
        defaultValuesButton.setText("Default Values");
        panel7.add(defaultValuesButton, cc.xy(1, 5));
        final JSeparator separator10 = new JSeparator();
        separator10.setOrientation(1);
        panel7.add(separator10, cc.xywh(3, 1, 1, 8, CellConstraints.CENTER, CellConstraints.FILL));
        repulsiveEdgesCheckBox = new JCheckBox();
        repulsiveEdgesCheckBox.setText("Repulsion:");
        panel7.add(repulsiveEdgesCheckBox, cc.xy(5, 8, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        repulsionSpinner = new JSpinner();
        panel7.add(repulsionSpinner, cc.xy(7, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        inverseQuadraticModelCheckBox = new JCheckBox();
        inverseQuadraticModelCheckBox.setText("Inverse-quadratic model");
        panel7.add(inverseQuadraticModelCheckBox, cc.xyw(11, 8, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JSeparator separator11 = new JSeparator();
        panel7.add(separator11, cc.xyw(1, 7, 2, CellConstraints.FILL, CellConstraints.FILL));
        directionAffectsCompatibilityCheckBox = new JCheckBox();
        directionAffectsCompatibilityCheckBox.setText("Direction affects compatibility");
        panel7.add(directionAffectsCompatibilityCheckBox, cc.xy(17, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
        edgeValueAffectsAttractionCheckBox = new JCheckBox();
        edgeValueAffectsAttractionCheckBox.setText("Edge value affects attraction");
        panel7.add(edgeValueAffectsAttractionCheckBox, cc.xy(17, 3));
        simpleCompatibilityMeasureCheckBox = new JCheckBox();
        simpleCompatibilityMeasureCheckBox.setText("Simple compatibility measure");
        panel7.add(simpleCompatibilityMeasureCheckBox, cc.xy(17, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        binaryCompatibilityCheckBox = new JCheckBox();
        binaryCompatibilityCheckBox.setText("Binary compatibility");
        panel7.add(binaryCompatibilityCheckBox, cc.xy(17, 8, CellConstraints.LEFT, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
