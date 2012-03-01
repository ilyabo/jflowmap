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
import jflowmap.util.IOUtils;

import org.apache.log4j.Logger;

import at.fhj.utils.misc.FileUtils;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class CsvFlowMapGraphReader {

  private static Logger logger = Logger.getLogger(CsvFlowMapGraphReader.class);

  private final FlowMapAttrSpec attrSpec;
  private final char separator;
  private final String charset;

  private final FlowMapGraphBuilder builder;

  private final String nodesLocation;
  private final String flowsLocation;

  private LineParser flowsLineParser;

  protected CsvFlowMapGraphReader(FlowMapAttrSpec attrSpec,
      String nodesLocation, String flowsLocation,
      char separator, String charset) {
    this.attrSpec = attrSpec;
    this.separator = separator;
    this.charset = charset;
    this.nodesLocation = nodesLocation;
    this.flowsLocation = flowsLocation;
    this.builder = new FlowMapGraphBuilder(FileUtils.getFilenameOnly(flowsLocation), attrSpec);
  }

  public FlowMapGraphBuilder getBuilder() {
    return builder;
  }

  public FlowMapAttrSpec getAttrSpec() {
    return attrSpec;
  }

  public void setFlowsLineParser(LineParser lineParser) {
    this.flowsLineParser = lineParser;
  }
  /**
   * Calling this method prior to readGraph() might be useful for
   * initializing FlowMapAttrSpec which can then be passed to readGraph().
   */
  public static Iterable<String> readAttrNames(String csvLocation, char separator, String charset)
  throws IOException {
    List<String> list = null;
    CSVReader csv = null;
    try {
      csv = createReader(csvLocation, separator, charset);
      String[] header = csv.readNext();
      list = new ArrayList<String>(header.length);
      for (String attr : header) {
        list.add(attr);
      }
    } catch (Exception ioe) {
      throw new IOException(
          "Error reading from location '" + csvLocation + "': " + ioe.getMessage(), ioe);
    } finally {
      try { if (csv != null) csv.close(); } catch (IOException ioe) {}
    }
    return list;
  }


  public static FlowMapGraphBuilder readFlowMapGraph(String nodesLocation, String flowsLocation,
      FlowMapAttrSpec attrSpec, char separator, String charset) throws IOException {
    return new CsvFlowMapGraphReader(
        attrSpec, nodesLocation, flowsLocation, separator, charset).read();
  }

  FlowMapGraphBuilder read() throws IOException {
    parseCsv(nodesLocation, new LineParser() {
      public void apply(Map<String, String> attrs) { builder.addNode(attrs); }
    });

    if (flowsLineParser != null) {
      parseCsv(flowsLocation, flowsLineParser);
    } else {
      parseCsv(flowsLocation, new LineParser() {
        public void apply(Map<String, String> attrs) { builder.addEdge(attrs); }
      });
    }

    return builder;
  }

  private void parseCsv(String csvLocation, LineParser lp) throws IOException {
    logger.info("Parsing CSV '" + csvLocation + "'");
    CSVReader csv = null;
    int lineNum = 1;
    try {
      Map<String, Integer> colsByName = null;
      csv = createReader(csvLocation, separator, charset);
      String[] csvLine;
      while ((csvLine = csv.readNext()) != null) {
        if (lineNum == 1) {
          // parse header
          colsByName = createColsByNameMap(csvLine);
        } else {
          // parse the rest of the lines
          Map<String, String> attrs = asAttrValuesMap(csvLine, colsByName);
          lp.apply(attrs);
        }
        lineNum++;
      }
    } catch (Exception ioe) {
      throw new IOException("Error loading '" + csvLocation + "' (line " + lineNum + "): " +
          ioe.getMessage(), ioe);
    } finally {
      try {
        if (csv != null) csv.close();
      } catch (IOException ioe) {
        // can't do anything about it
      }
    }
  }

  public static CSVReader createReader(String csvLocation, char separator, String charset)
  throws IOException {
    return new CSVReader(new InputStreamReader(
        IOUtils.asInputStream(csvLocation), charset), separator);
  }

  private Map<String, String> asAttrValuesMap(final String[] csvLine,
      Map<String, Integer> colsByName) {
    return Maps.transformValues(colsByName, new Function<Integer, String>() {
      public String apply(Integer col) {
        return csvLine[col].trim();
      }
    });
  }

  interface LineParser {
    void apply(Map<String, String> attrValues) throws IOException;
  }

  private Map<String, Integer> createColsByNameMap(String[] line) {
    Map<String, Integer> map = Maps.newHashMap();
    for (int i = 0; i < line.length; i++) {
      map.put(line[i].trim(), i);
    }
    return map;
  }
}
