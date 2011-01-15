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

package jflowmap.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMapAttrSpec;
import jflowmap.FlowMapGraph;
import jflowmap.geom.Point;

import org.apache.log4j.Logger;

import prefuse.util.io.IOLib;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class CsvFlowMapGraphReader {

  public static Logger logger = Logger.getLogger(CsvFlowMapGraphReader.class);

  private final String nodeIdAttr, srcNodeAttr, targetNodeAttr;
  private final FlowMapAttrSpec attrSpec;

  private FlowMapGraphBuilder builder;

  private CsvFlowMapGraphReader(
      String nodeIdAttr,
      String srcNodeAttr, String targetNodeAttr,
      FlowMapAttrSpec attrSpec) {
    this.nodeIdAttr = nodeIdAttr;
    this.srcNodeAttr = srcNodeAttr;
    this.targetNodeAttr = targetNodeAttr;
    this.attrSpec = attrSpec;
  }

  public static FlowMapGraph readGraph(
      String nodesLocation, String edgesLocation,
      String nodeIdAttr, String srcNodeAttr, String targetNodeAttr,
      FlowMapAttrSpec attrSpec) throws IOException {

    CsvFlowMapGraphReader reader = new CsvFlowMapGraphReader(
        nodeIdAttr, srcNodeAttr, targetNodeAttr, attrSpec);
    return reader.read(nodesLocation, edgesLocation);
  }

  public FlowMapGraph read(String nodesLocation, String edgesLocation) throws IOException {
    builder = new FlowMapGraphBuilder(null, attrSpec);
    parseCsv(nodesLocation, new LineParser() {
      @Override
      public void apply(String[] csvLine, Map<String, Integer> colsByName) throws IOException {
        builder.addNode(
            attrValue(nodeIdAttr, csvLine, colsByName),
            new Point(
                parseDouble(attrValue(attrSpec.getXNodeAttr(), csvLine, colsByName)),
                parseDouble(attrValue(attrSpec.getYNodeAttr(), csvLine, colsByName))),
            attrValue(attrSpec.getNodeLabelAttr(), csvLine, colsByName)
        );
      }
    });

    parseCsv(edgesLocation, new LineParser() {
      @Override
      public void apply(String[] csvLine, Map<String, Integer> colsByName) throws IOException {
        builder.addEdge(
            attrValue(srcNodeAttr, csvLine, colsByName),
            attrValue(targetNodeAttr, csvLine, colsByName),
            weightAttrValues(attrSpec.getEdgeWeightAttrs(), csvLine, colsByName)
        );
      }
    });

    return builder.build();
  }

  private void parseCsv(String csvLocation, LineParser lp) throws IOException {
    CSVReader csv = null;
    int lineNum = 1;
    try {
      Map<String, Integer> colsByName = null;
      csv = new CSVReader(new InputStreamReader(IOLib.streamFromString(csvLocation)));
      String[] csvLine;
      while ((csvLine = csv.readNext()) != null) {
        if (lineNum == 1) {
          // parse header
          colsByName = createColsByNameMap(csvLine);
        } else {
          // parse the rest of the lines
          lp.apply(csvLine, colsByName);
        }
        lineNum++;
      }
    } catch (Exception ioe) {
      throw new IOException("Parse error in '" + csvLocation + "' line " + lineNum + ": " +
          ioe.getMessage(), ioe);
    } finally {
      try {
        csv.close();
      } catch (IOException ioe) {
        // can't do anything about it
      }
    }
  }

  private interface LineParser {
    void apply(String[] csvLine, Map<String, Integer> colsByName) throws IOException;
  }

  private double parseDouble(String str) throws IOException {
    try {
      if (str.trim().length() == 0) {
        return Double.NaN;
      }
      return Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      throw new IOException("Cannot parse number '" + str + "'");
    }
  }

  private String attrValue(String attrName, String[] csvLine, Map<String, Integer> colsByName)
    throws IOException {
    Integer col = colsByName.get(attrName);
    if (col == null) {
      throw new IOException("No column '"+attrName+"' in CSV");
    }
    if (col >= csvLine.length) {
      throw new IOException("No value for column '"+attrName+"'");
    }
    return csvLine[col];
  }

  private Iterable<Double> weightAttrValues(List<String> attrs, String[] csvLine,
      Map<String, Integer> colsByName) throws IOException {
    List<Double> vals = new ArrayList<Double>(attrs.size());
    for (String attr : attrs) {
      vals.add(parseDouble(attrValue(attr, csvLine, colsByName)));
    }
    return vals;
  }

  private Map<String, Integer> createColsByNameMap(String[] line) {
    Map<String, Integer> map = Maps.newHashMap();
    for (int i = 0; i < line.length; i++) {
      map.put(line[i], i);
    }
    return map;
  }

  public static Iterable<String> readAttrNames(String location) throws IOException {
    List<String> list = null;
    CSVReader csv = null;
    try {
      csv = new CSVReader(new InputStreamReader(IOLib.streamFromString(location)));
      String[] header = csv.readNext();
      list = new ArrayList<String>(header.length);
      for (String attr : header) {
        list.add(attr);
      }
    } finally {
      try { if (csv != null) csv.close(); } catch (IOException ioe) {}
    }
    return list;
  }

}
