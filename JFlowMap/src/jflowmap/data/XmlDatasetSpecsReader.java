package jflowmap.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import jflowmap.geo.MapProjections;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

import prefuse.util.io.IOLib;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class XmlDatasetSpecsReader {

  private static Logger logger = Logger.getLogger(XmlDatasetSpecsReader.class);

  private XmlDatasetSpecsReader() {
  }

  public static List<GraphMLDatasetSpec> readDatasetSpecs(String location) throws IOException {
    logger.info("Loading dataset specs from '" + location + "'");
    XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
    try {
      InputStream is = IOLib.streamFromString(location);
      if (is == null) {
        throw new IOException("Cannot load dataset specs from location: '" + location + "'");
      }
      return loadFrom(location, builder.parseReader(new InputStreamReader(is)));
    } catch (XmlPullParserException e) {
      throw new IOException(e);
    }
  }

  private static List<GraphMLDatasetSpec> loadFrom(String name, XmlDocument doc) throws XmlPullParserException, IOException {

    List<GraphMLDatasetSpec> listOfDatasetSpecs = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    List<XmlElement> listOfDatasetXmlNodes = new Xb1XPath("/datasets/dataset").selectNodes(doc);

    for (XmlElement datasetNode : listOfDatasetXmlNodes) {
//      String valueFilterMin = datasetNode.getAttributeValue(null, "valueFilterMin");
      String projName = datasetNode.getAttributeValue(null, "mapProjection");
      listOfDatasetSpecs.add(
          new GraphMLDatasetSpec(
              datasetNode.getAttributeValue(null, "src"),
              datasetNode.getAttributeValue(null, "weightAttr"),
              datasetNode.getAttributeValue(null, "xNodeAttr"),
              datasetNode.getAttributeValue(null, "yNodeAttr"),
              datasetNode.getAttributeValue(null, "labelAttr"),
              datasetNode.getAttributeValue(null, "areaMapSrc"),
              null,
              null
, (projName != null ? MapProjections.valueOf(projName) : MapProjections.NONE)
//              valueFilterMin != null ? Double.parseDouble(valueFilterMin) : Double.NaN
          )
      );
    }

    return listOfDatasetSpecs;
  }


}
