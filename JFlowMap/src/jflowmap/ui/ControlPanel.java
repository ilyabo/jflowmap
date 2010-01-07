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

package jflowmap.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

import jflowmap.DatasetSpec;
import jflowmap.JFlowMap;
import jflowmap.bundling.ForceDirectedBundlerParameters;
import jflowmap.clustering.NodeDistanceMeasure;
import jflowmap.data.FlowMapStats;
import jflowmap.data.MinMax;
import jflowmap.models.FlowMapModel;
import jflowmap.util.Pair;
import jflowmap.visuals.VisualFlowMap;
import jflowmap.visuals.VisualNode;
import jflowmap.visuals.VisualNodeCluster;
import at.fhj.utils.graphics.AxisMarks;
import at.fhj.utils.swing.FancyTable;
import at.fhj.utils.swing.TableSorter;
import ch.unifr.dmlib.cluster.Linkage;
import ch.unifr.dmlib.cluster.Linkages;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ControlPanel {

    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JCheckBox useLogColorScaleCheckbox;
    private JSlider maxEdgeWidthSlider;
    private JSpinner maxEdgeWidthSpinner;
    private JSpinner minWeightFilterSpinner;
    private JSlider minLengthFilterSlider;
    private JSpinner minLengthFilterSpinner;
    private JSlider maxWeightFilterSlider;
    private JSpinner maxWeightFilterSpinner;
    private JCheckBox autoAdjustColorScaleCheckBox;
    private JSlider maxLengthFilterSlider;
    private JSpinner maxLengthFilterSpinner;
    private JSlider minWeightFilterSlider;
    private JCheckBox useLogWidthScaleCheckbox;
    private JComboBox datasetCombo;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox comboBox5;
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
    private JTable clusterDistancesTable;
    private JButton clusterButton;
    private JSlider maxClusterDistanceSlider;
    private JCheckBox fillEdgesWithGradientCheckBox;
    private JCheckBox showDirectionMarkersCheckBox;
    private JSlider edgeMarkerSizeSlider;
    private JSpinner edgeMarkerSizeSpinner;
    private JCheckBox proportionalDirectionMarkersCheckBox;
    private JLabel edgeMarkerSizeLabel;
    private JLabel edgeMarkerOpacityLabel;
    private JSpinner maxClusterDistanceSpinner;
    private JComboBox distanceMeasureCombo;
    private JTabbedPane tabbedPane2;
    private JTable clusterNodesTable;
    private JTable flowsTable;
    private JComboBox linkageComboBox;
    private JButton joinClusterEdgesButton;
    private JButton resetClustersButton;
    private JLabel maxClusterDistanceLabel;
    private JSlider euclideanMaxClusterDistanceSlider;
    private JSpinner euclideanMaxClusterDistanceSpinner;
    private JCheckBox combineWithEuclideanClustersCheckBox;
    private JLabel euclideanMaxClusterDistanceLabel;
    private JLabel numberOfClustersValueLabel;
    private JLabel numberOfClustersLabel;
    private JButton resetJoinedEdgesButton;
    private JTable clustersTable;
    private JButton aggregateEdgesButton;
    private final JFlowMap jFlowMap;
    private boolean initializing;
    private ForceDirectedBundlerParameters fdBundlingParams;
    private NodeSimilarityDistancesTableModel clusterDistancesTableModel;
    private TableSorter clusterDistancesTableSorter;
    private boolean modelsInitialized;
    private ClusterNodesTableModel clusterNodesTableModel;
    private TableSorter clusterNodesTableSorter;
    private FlowsTableModel flowsTableModel;
    private TableSorter flowsTableSorter;
    private ClustersTableModel clustersTableModel;
    private VisualFlowMap visualFlowMap;
    private final List<DatasetSpec> datasetSpecs;


    public ControlPanel(JFlowMap flowMap, List<DatasetSpec> datasetSpecs) {
        this.jFlowMap = flowMap;
        this.datasetSpecs = datasetSpecs;
        $$$setupUI$$$();

        loadVisualFlowMap(flowMap.getVisualFlowMap());
        initUIChangeListeners();

        updateDirectionAffectsCompatibilityCheckBox();
        updateDirectionAffectsCompatibilityCheckBox();
        updateRepulsionSpinner();
        updateMarkersInputs();
    }

    public void loadVisualFlowMap(VisualFlowMap newVisualFlowMap) {
        // attach listeners
        if (visualFlowMap != null) {
            removeVisualFlowMapListeners(visualFlowMap);
        }
        visualFlowMap = newVisualFlowMap;
        attachVisualFlowMapListeners(newVisualFlowMap);

        // load data
        fdBundlingParams = new ForceDirectedBundlerParameters(visualFlowMap.getModel());
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
        if (datasetSpecs != null) {
            datasetCombo.setModel(new DefaultComboBoxModel(datasetSpecs.toArray()));
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
//        double sStep = sExp / 100;
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
        initNodeClusteringModels();
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
    }

    private void initFilterModels() {
        FlowMapStats stats = getGraphStats();

        MinMax weightStats = stats.getEdgeWeightStats();

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

        MinMax lengthStats = stats.getEdgeLengthStats();

        Pair<SpinnerModel, BoundedRangeModel> minLengthFilterModels =
                new BoundSpinnerSliderModels<Double>(
                        lengthStats.getMin(), lengthStats.getMin(), lengthStats.getMax(), 1.0,
                        BoundSpinnerSliderModels.MAP_ID_DOUBLE
                ).build();
        minLengthFilterSpinner.setModel(minLengthFilterModels.first());
        minLengthFilterSlider.setModel(minLengthFilterModels.second());

        Pair<SpinnerModel, BoundedRangeModel> maxLengthFilterModels =
                new BoundSpinnerSliderModels<Double>(
                        lengthStats.getMax(), lengthStats.getMin(), lengthStats.getMax(), 1.0,
                        BoundSpinnerSliderModels.MAP_ID_DOUBLE
                ).build();
        maxLengthFilterSpinner.setModel(maxLengthFilterModels.first());
        maxLengthFilterSlider.setModel(maxLengthFilterModels.second());
    }

    public void setData(FlowMapModel data) {
        autoAdjustColorScaleCheckBox.setSelected(data.getAutoAdjustColorScale());
        useLogWidthScaleCheckbox.setSelected(data.isUseLogWidthScale());
        useLogColorScaleCheckbox.setSelected(data.isUseLogColorScale());

        minLengthFilterSpinner.setValue(data.getEdgeLengthFilterMin());
        maxLengthFilterSpinner.setValue(data.getEdgeLengthFilterMax());

        minWeightFilterSpinner.setValue(data.getEdgeWeightFilterMin());
        maxWeightFilterSpinner.setValue(data.getEdgeWeightFilterMax());

        edgeOpacitySpinner.setValue(data.getEdgeAlpha());
//        edgeOpacitySlider.setValue(data.getEdgeAlpha());
        edgeMarkerOpacitySpinner.setValue(data.getDirectionMarkerAlpha());
        edgeMarkerSizeSpinner.setValue(data.getDirectionMarkerSize());

        showDirectionMarkersCheckBox.setSelected(data.getShowDirectionMarkers());
        showNodesCheckBox.setSelected(data.getShowNodes());
        fillEdgesWithGradientCheckBox.setSelected(data.getFillEdgesWithGradient());
        proportionalDirectionMarkersCheckBox.setSelected(data.getUseProportionalDirectionMarkers());

        maxEdgeWidthSpinner.setValue(data.getMaxEdgeWidth());
//        maxEdgeWidthSlider.setValue((int) Math.round(data.getMaxEdgeWidth()));
    }

    private void initUIChangeListeners() {
        initDatasetListeners();
        initFilterListeners();
        initAestheticsListeners();
        initEdgeBundlingListeners();
        initNodeClusterListeners();
    }

    private void initDatasetListeners() {
        datasetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (initializing) return;
                jFlowMap.loadFlowMap((DatasetSpec) datasetCombo.getSelectedItem());
                jFlowMap.fitFlowMapInView();
            }
        });
    }

    private void initFilterListeners() {
        // Edge value filter
        minWeightFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
//                minWeightFilterSlider.setValue(toValueEdgeFilterSliderValue((Double) minWeightFilterSpinner.getValue()));
                getFlowMapModel().setEdgeWeightFilterMin((Double) minWeightFilterSpinner.getValue());
            }
        });
//        minWeightFilterSlider.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                if (initializing) return;
//                double value = fromValueEdgeFilterSliderValue(minWeightFilterSlider.getValue());
//                getFlowMapModel().setEdgeWeightFilterMin(value);
//                minWeightFilterSpinner.setValue(value);
//            }
//        });
        maxWeightFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setEdgeWeightFilterMax((Double) maxWeightFilterSpinner.getValue());
            }
        });
//        maxWeightFilterSlider.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                if (initializing) return;
//                double value = fromValueEdgeFilterSliderValue(maxWeightFilterSlider.getValue());
//                maxWeightFilterSpinner.setValue(value);
//            }
//        });


        // Edge length filter
        minLengthFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setEdgeLengthFilterMin((Double) minLengthFilterSpinner.getValue());
            }
        });
        maxLengthFilterSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                getFlowMapModel().setEdgeLengthFilterMax((Double) maxLengthFilterSpinner.getValue());
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
//        aggregateBundledEdgesButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                getVisualFlowMap().aggregateBundledEdges();
//            }
//        });
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
        aggregateEdgesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().aggregateBundledEdges();
            }
        });
    }

    private void initNodeClusterListeners() {
        // Node clustering
        clusterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().clusterNodes(
                        (NodeDistanceMeasure) distanceMeasureCombo.getSelectedItem(),
                        (Linkage<VisualNode>) linkageComboBox.getSelectedItem(),
                        combineWithEuclideanClustersCheckBox.isSelected()
                );
                initNodeClusteringModels();
                updateNumberOfClustersLabel();
                updateNodeClustersTables();
            }
        });
        joinClusterEdgesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().joinClusterEdges();
                updateClusterButtons();
                updateNumberOfClustersLabel();
            }
        });
        resetClustersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().resetClusters();
                clearClusterTableModels();
                updateClusterDistanceSliderSpinner();
                updateClusterButtons();
            }
        });
        resetJoinedEdgesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getVisualFlowMap().resetJoinedNodes();
                updateClusterButtons();
                updateNumberOfClustersLabel();
                updateNumberOfClustersLabel();
                updateNodeClustersTables();
            }
        });
        maxClusterDistanceSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double spinnerValue = ((Number) maxClusterDistanceSpinner.getValue()).doubleValue();
                getVisualFlowMap().setClusterDistanceThreshold(spinnerValue);
                updateNumberOfClustersLabel();
                updateNodeClustersTables();
            }
        });
        euclideanMaxClusterDistanceSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (initializing) return;
                double spinnerValue = ((Number) euclideanMaxClusterDistanceSpinner.getValue()).doubleValue();
                getVisualFlowMap().setEuclideanClusterDistanceThreshold(spinnerValue);
                updateNumberOfClustersLabel();
                updateNodeClustersTables();
            }
        });
    }

//    private void loadFlowMap(DatasetSpec dataset) {
//        VisualFlowMap visualFlowMap = jFlowMap.loadFlowMap(dataset);
//        jFlowMap.setVisualFlowMap(visualFlowMap);
//        jFlowMap.fitFlowMapInView();
//        loadVisualFlowMap(visualFlowMap);
//    }

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

    public FlowMapModel getFlowMapModel() {
        return getVisualFlowMap().getModel();
    }

    private VisualFlowMap getVisualFlowMap() {
        return jFlowMap.getVisualFlowMap();
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


        // clusterNodesTable
        clusterNodesTableModel = new ClusterNodesTableModel();
        clusterNodesTableSorter = new TableSorter(clusterNodesTableModel);
        clusterNodesTableSorter.setColumnSortable(0, true);
        clusterNodesTableSorter.setColumnSortable(1, true);
        clusterNodesTableSorter.setSortingStatus(1, TableSorter.ASCENDING);


        clusterNodesTable = new FancyTable(clusterNodesTableSorter);
        clusterNodesTable.setDefaultRenderer(ClusterIcon.class,
                ((FancyTable) clusterNodesTable).new FancyIconRenderer());
        clusterNodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//        clusterNodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        clusterNodesTable.setAutoCreateColumnsFromModel(false);
        clusterNodesTableSorter.setTableHeader(clusterNodesTable.getTableHeader());

        TableColumnModel tcm1 = clusterNodesTable.getColumnModel();
        tcm1.getColumn(0).setPreferredWidth(300);
//        tcm1.getColumn(0).setResizable(false);
//        tcm1.getColumn(0).setMaxWidth(350);
        tcm1.getColumn(1).setPreferredWidth(50);
        tcm1.getColumn(1).setMaxWidth(100);

        // clusterDistancesTable
        clusterDistancesTableModel = new NodeSimilarityDistancesTableModel();
        clusterDistancesTableSorter = new TableSorter(clusterDistancesTableModel);
        clusterDistancesTableSorter.setColumnSortable(0, true);
        clusterDistancesTableSorter.setColumnSortable(1, true);
        clusterDistancesTableSorter.setColumnSortable(2, true);

        clusterDistancesTable = new FancyTable(clusterDistancesTableSorter);
        clusterDistancesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        clusterDistancesTable.setAutoCreateColumnsFromModel(false);
        clusterDistancesTableSorter.setTableHeader(clusterDistancesTable.getTableHeader());

        TableColumnModel tcm2 = clusterDistancesTable.getColumnModel();
        tcm2.getColumn(0).setPreferredWidth(100);
        tcm2.getColumn(0).setMaxWidth(150);
        tcm2.getColumn(1).setPreferredWidth(100);
        tcm2.getColumn(1).setMaxWidth(150);
        tcm2.getColumn(1).setPreferredWidth(120);
        tcm2.getColumn(1).setMaxWidth(150);


        // clusterFilter table
        clustersTableModel = new ClustersTableModel();
        clustersTable = new FancyTable(clustersTableModel);
        clustersTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        clustersTable.setAutoCreateColumnsFromModel(false);
        clustersTable.setDefaultRenderer(ClusterIcon.class,
                ((FancyTable) clustersTable).new FancyIconRenderer());
        clustersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (visualFlowMap.hasClusters()) {
                        List<VisualNodeCluster> all = visualFlowMap.getVisualNodeClusters();
                        List<VisualNodeCluster> selected = new ArrayList<VisualNodeCluster>();
                        for (int i : clustersTable.getSelectedRows()) {
                            selected.add(all.get(i));
                        }
                        visualFlowMap.setNodeClustersToShow(selected);
                    }
                }
            }
        });

        TableColumnModel tcm3 = clustersTable.getColumnModel();
        tcm3.getColumn(0).setPreferredWidth(30);
        tcm3.getColumn(0).setMinWidth(20);
        tcm3.getColumn(1).setPreferredWidth(200);
//        tcm3.getColumn(1).setMaxWidth(350);


        distanceMeasureCombo = new JComboBox(NodeDistanceMeasure.values());
        linkageComboBox = new JComboBox(new Linkage[]{
                Linkages.average(), Linkages.complete(), Linkages.single()
        });
    }

    private void initNodeClusteringModels() {
        VisualFlowMap flowMap = getVisualFlowMap();

        updateClusterDistanceSliderSpinner();
        updateClusterButtons();

        initMaxClusterDistanceControls(
                getVisualFlowMap().getClusterDistanceThreshold(),
                flowMap.getMaxNodeDistance(),
                maxClusterDistanceSpinner,
                maxClusterDistanceSlider);

        if (getVisualFlowMap().hasEuclideanClusters()) {
            initMaxClusterDistanceControls(
                    getVisualFlowMap().getEuclideanClusterDistanceThreshold(),
                    flowMap.getEuclideanMaxNodeDistance(),
                    euclideanMaxClusterDistanceSpinner,
                    euclideanMaxClusterDistanceSlider);
        } else {
            initMaxClusterDistanceControls(
                    0, 0,
                    euclideanMaxClusterDistanceSpinner,
                    euclideanMaxClusterDistanceSlider);
        }


//        clusterDistancesTableModel.setDistances(flowMap.getNodeDistanceList());
//        clusterDistancesTableSorter.setSortingStatus(2, TableSorter.ASCENDING);
//        clusterNodesTableModel.setVisualNodes(flowMap.getVisualNodes());     // to re-filter
//        clusterNodesTableSorter.setSortingStatus(1, TableSorter.ASCENDING);   // to re-sort

//      maxClusterDistanceSpinner.setEditor(new JSpinner.NumberEditor(maxClusterDistanceSpinner, "0.0######"));
//      maxClusterDistanceSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 1));
//      maxClusterDistanceSlider.setMinimum(0);
//      maxClusterDistanceSlider.setMaximum(10000);

        clearClusterTableModels();

        flowsTableModel.setVisualFlowMap(flowMap);
        flowsTableSorter.setSortingStatus(2, TableSorter.DESCENDING);

//        clusterFilterTableModel.setClusters(flowMap.);
    }

    private void updateClusterDistanceSliderSpinner() {
        boolean hasClusters = getVisualFlowMap().hasClusters();
        boolean hasEuClusters = getVisualFlowMap().hasEuclideanClusters();
        maxClusterDistanceSpinner.setEnabled(hasClusters);
        maxClusterDistanceSlider.setEnabled(hasClusters);
        maxClusterDistanceLabel.setEnabled(hasClusters);

        euclideanMaxClusterDistanceSpinner.setEnabled(hasEuClusters);
        euclideanMaxClusterDistanceSlider.setEnabled(hasEuClusters);
        euclideanMaxClusterDistanceLabel.setEnabled(hasEuClusters);
    }

    private void updateClusterButtons() {
        joinClusterEdgesButton.setEnabled(getVisualFlowMap().hasClusters());
        resetClustersButton.setEnabled(getVisualFlowMap().hasClusters());
        resetJoinedEdgesButton.setEnabled(getVisualFlowMap().hasJoinedEdges());
    }

    private void initMaxClusterDistanceControls(double val, double max, JSpinner spinner, JSlider slider) {
        Pair<SpinnerModel, BoundedRangeModel> maxClusterDistanceModels =
                new BoundSpinnerSliderModels<Double>(
                        val, 0.0, max, AxisMarks.ordAlpha(max / 100.0),
                        BoundSpinnerSliderModels.createLinearMapping(100.0 / max)
                ).build();
        spinner.setModel(maxClusterDistanceModels.first());
        slider.setModel(maxClusterDistanceModels.second());
    }

    private void updateNodeClustersTables() {
        clusterNodesTableModel.setVisualNodes(getVisualFlowMap().getVisualNodes()); // to update list (re-run filtering)
        clusterNodesTableSorter.fireTableDataChanged();
        clusterNodesTableSorter.setSortingStatus(1, TableSorter.ASCENDING); // to re-sort

        clusterDistancesTableModel.setDistances(getVisualFlowMap().getNodeDistanceList());
        clusterDistancesTableSorter.setSortingStatus(2, TableSorter.ASCENDING);

        clustersTableModel.setClusters(getVisualFlowMap().getVisualNodeClusters());
    }

    private void updateNumberOfClustersLabel() {
        String text;
        boolean enabled;
        if (getVisualFlowMap().hasClusters()) {
            text = Integer.toString(getVisualFlowMap().getNumberOfClusters());
            enabled = true;
        } else {
            text = "";
            enabled = false;
        }
        numberOfClustersLabel.setEnabled(enabled);
        numberOfClustersValueLabel.setEnabled(enabled);
        numberOfClustersValueLabel.setText(text);
    }

    private void clearClusterTableModels() {
        clusterDistancesTableModel.clearData();
        clusterNodesTableModel.clearData();
        clustersTableModel.clearData();
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
        tabbedPane1.addTab("Dataset", panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        datasetCombo = new JComboBox();
        CellConstraints cc = new CellConstraints();
        panel2.add(datasetCombo, cc.xy(3, 1));
        final JLabel label1 = new JLabel();
        label1.setText("Dataset:");
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
        final JLabel label6 = new JLabel();
        label6.setText("Min length:");
        panel3.add(label6, cc.xy(9, 1));
        minLengthFilterSlider = new JSlider();
        panel3.add(minLengthFilterSlider, cc.xy(11, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        minLengthFilterSpinner = new JSpinner();
        panel3.add(minLengthFilterSpinner, cc.xy(13, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
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
        final JLabel label8 = new JLabel();
        label8.setText("Max length:");
        panel3.add(label8, cc.xy(9, 3));
        maxLengthFilterSlider = new JSlider();
        panel3.add(maxLengthFilterSlider, cc.xy(11, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxLengthFilterSpinner = new JSpinner();
        panel3.add(maxLengthFilterSpinner, cc.xy(13, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
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
        useLogWidthScaleCheckbox.setEnabled(false);
        useLogWidthScaleCheckbox.setText("Use log width scale");
        panel5.add(useLogWidthScaleCheckbox, cc.xy(2, 1));
        useLogColorScaleCheckbox = new JCheckBox();
        useLogColorScaleCheckbox.setEnabled(false);
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
        label10.setEnabled(false);
        label10.setText("Color scheme:");
        panel6.add(label10, cc.xy(1, 1));
        comboBox5 = new JComboBox();
        comboBox5.setEnabled(false);
        panel6.add(comboBox5, cc.xy(3, 1));
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
        aggregateEdgesButton = new JButton();
        aggregateEdgesButton.setText("Aggregate edges");
        panel7.add(aggregateEdgesButton, cc.xy(1, 8));
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
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:20px:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):grow,left:4dlu:noGrow,fill:max(p;75px):noGrow,left:10dlu:noGrow,fill:1px:noGrow,left:10dlu:noGrow,fill:max(p;200px):noGrow", "center:max(p;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:m:noGrow,top:d:noGrow,center:max(d;6px):noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:min(p;200px):grow"));
        tabbedPane1.addTab("Node clustering", panel8);
        panel8.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        panel8.add(distanceMeasureCombo, cc.xyw(7, 1, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));
        final JLabel label19 = new JLabel();
        label19.setText("Distance measure:");
        panel8.add(label19, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        tabbedPane2 = new JTabbedPane();
        tabbedPane2.setTabPlacement(3);
        panel8.add(tabbedPane2, cc.xywh(15, 1, 1, 14, CellConstraints.DEFAULT, CellConstraints.FILL));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new FormLayout("fill:d:grow", "center:d:grow"));
        tabbedPane2.addTab("Clusters", panel9);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel9.add(scrollPane2, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(""));
        clustersTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane2.setViewportView(clustersTable);
        final JScrollPane scrollPane3 = new JScrollPane();
        tabbedPane2.addTab("Nodes", scrollPane3);
        scrollPane3.setBorder(BorderFactory.createTitledBorder(""));
        clusterNodesTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane3.setViewportView(clusterNodesTable);
        final JScrollPane scrollPane4 = new JScrollPane();
        tabbedPane2.addTab("Distances", scrollPane4);
        scrollPane4.setBorder(BorderFactory.createTitledBorder(""));
        clusterDistancesTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        scrollPane4.setViewportView(clusterDistancesTable);
        final JSeparator separator12 = new JSeparator();
        separator12.setOrientation(1);
        panel8.add(separator12, cc.xywh(13, 1, 1, 14, CellConstraints.CENTER, CellConstraints.FILL));
        final JLabel label20 = new JLabel();
        label20.setText("Linkage:");
        panel8.add(label20, cc.xy(5, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        panel8.add(linkageComboBox, cc.xy(7, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
        maxClusterDistanceSlider = new JSlider();
        maxClusterDistanceSlider.setEnabled(false);
        panel8.add(maxClusterDistanceSlider, cc.xyw(6, 8, 4, CellConstraints.FILL, CellConstraints.DEFAULT));
        euclideanMaxClusterDistanceSlider = new JSlider();
        euclideanMaxClusterDistanceSlider.setEnabled(false);
        panel8.add(euclideanMaxClusterDistanceSlider, cc.xyw(6, 10, 4, CellConstraints.FILL, CellConstraints.DEFAULT));
        maxClusterDistanceSpinner = new JSpinner();
        maxClusterDistanceSpinner.setEnabled(false);
        panel8.add(maxClusterDistanceSpinner, cc.xy(11, 8, CellConstraints.FILL, CellConstraints.DEFAULT));
        euclideanMaxClusterDistanceSpinner = new JSpinner();
        euclideanMaxClusterDistanceSpinner.setEnabled(false);
        panel8.add(euclideanMaxClusterDistanceSpinner, cc.xy(11, 10, CellConstraints.FILL, CellConstraints.DEFAULT));
        euclideanMaxClusterDistanceLabel = new JLabel();
        euclideanMaxClusterDistanceLabel.setEnabled(false);
        euclideanMaxClusterDistanceLabel.setText("Euclidean max cluster distance:");
        panel8.add(euclideanMaxClusterDistanceLabel, cc.xy(5, 10, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        maxClusterDistanceLabel = new JLabel();
        maxClusterDistanceLabel.setEnabled(false);
        maxClusterDistanceLabel.setText("Max cluster distance:");
        panel8.add(maxClusterDistanceLabel, cc.xy(5, 8, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        final JSeparator separator13 = new JSeparator();
        separator13.setOrientation(1);
        panel8.add(separator13, cc.xywh(3, 1, 1, 4, CellConstraints.FILL, CellConstraints.FILL));
        clusterButton = new JButton();
        clusterButton.setText("Cluster");
        panel8.add(clusterButton, cc.xy(1, 1));
        final JSeparator separator14 = new JSeparator();
        panel8.add(separator14, cc.xyw(1, 5, 12, CellConstraints.FILL, CellConstraints.FILL));
        numberOfClustersLabel = new JLabel();
        numberOfClustersLabel.setEnabled(false);
        numberOfClustersLabel.setText("Number of clusters:");
        panel8.add(numberOfClustersLabel, cc.xy(5, 12, CellConstraints.RIGHT, CellConstraints.DEFAULT));
        numberOfClustersValueLabel = new JLabel();
        numberOfClustersValueLabel.setText("");
        panel8.add(numberOfClustersValueLabel, cc.xy(7, 12));
        joinClusterEdgesButton = new JButton();
        joinClusterEdgesButton.setEnabled(true);
        joinClusterEdgesButton.setText("Join edges");
        panel8.add(joinClusterEdgesButton, cc.xy(1, 8));
        resetClustersButton = new JButton();
        resetClustersButton.setEnabled(true);
        resetClustersButton.setText("Reset");
        panel8.add(resetClustersButton, cc.xy(1, 3));
        resetJoinedEdgesButton = new JButton();
        resetJoinedEdgesButton.setText("Reset");
        panel8.add(resetJoinedEdgesButton, cc.xy(1, 10));
        final JSeparator separator15 = new JSeparator();
        separator15.setOrientation(1);
        panel8.add(separator15, cc.xywh(3, 7, 1, 7, CellConstraints.FILL, CellConstraints.FILL));
        combineWithEuclideanClustersCheckBox = new JCheckBox();
        combineWithEuclideanClustersCheckBox.setEnabled(true);
        combineWithEuclideanClustersCheckBox.setSelected(true);
        combineWithEuclideanClustersCheckBox.setText("Combine with Euclidean clusters");
        panel8.add(combineWithEuclideanClustersCheckBox, cc.xyw(9, 3, 3));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
