package jflowmap.data;

import java.io.IOException;
import java.util.Set;

import jflowmap.FlowMapAttrSpec;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Sets;

/**
 * @author Ilya Boyandin
 */
public class CsvDisaggregatedFlowMapGraphReader {

  public static FlowMapGraphBuilder readFlowMapGraph(String nodesLocation, String flowsLocation,
      FlowMapAttrSpec attrSpec, String weightAttrsAttr,
      char separator, String charset) throws IOException {

    CsvFlowMapGraphReader reader = new CsvFlowMapGraphReader(
        attrSpec, nodesLocation, flowsLocation, separator, charset);

    final FlowMapGraphBuilder builder = reader.getBuilder();
    //builder.withCumulatedEdges();
    builder.withDisaggregatedEdges(weightAttrsAttr);
    /*
    reader.setFlowsLineParser(new CsvFlowMapGraphReader.LineParser() {
      public void apply(Map<String, String> attrs) {
        builder.addEdge(attrs);
      }
    });
    */
    return reader.read();
  }

  public static Iterable<String> readAttrNames(String csvLocation, String weightAttrsAttr,
      char separator, String charset) throws IOException {
    CSVReader csv = null;
    Set<String> attrNames = Sets.newTreeSet();
    int lineNum = 1;
    try {
      csv = CsvFlowMapGraphReader.createReader(csvLocation, separator, charset);
      String[] header = csv.readNext();


      // find column corresponding to weightAttrsAttr
      int attrsColumnIndex = findAttrValuesAttrColumn(weightAttrsAttr, header);
      if (attrsColumnIndex < 0)
        throw new IOException("Column '"+weightAttrsAttr+"' is missing");

      String[] values;
      while ((values = csv.readNext()) != null) {
        lineNum++;
        if (values.length <= attrsColumnIndex)
          throw new IOException("No value for '"+weightAttrsAttr+"' (to few values in the line)");
        attrNames.add(values[attrsColumnIndex].trim());
      }


    } catch (Exception ioe) {
      throw new IOException("Error loading '" + csvLocation + "' (line " + lineNum + "): " +
          ioe.getMessage(), ioe);
    } finally {
      try { if (csv != null) csv.close(); } catch (IOException ioe) {}
    }
    return attrNames;
  }

  private static int findAttrValuesAttrColumn(String weightAttrsAttr, String[] header) {
    for (int i = 0; i < header.length; i++) {
      if (weightAttrsAttr.equals(header[i])) {
        return i;
      }
    }
    return -1;
  }


}
