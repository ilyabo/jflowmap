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
import java.io.LineNumberReader;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import prefuse.util.io.IOLib;
import at.fhj.utils.misc.StringUtils;

import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class XmlRegionsReader {

  public static final String REGION_SEPARATOR = " | ";

  private XmlRegionsReader() {
  }

  /**
   * Returns a Map: CountryCode -> FullRegion,
   * where FullRegion is e.g. "Africa|Eastern Africa" or "Oceania|Australia and New Zealand"
   */
  public static Map<String, String> readFrom(String location) throws XMLStreamException, IOException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(IOLib.streamFromString(location)));
    XMLStreamReader reader = inputFactory.createXMLStreamReader(lineNumberReader);
    Stack<String> regionStack = new Stack<String>();

    Map<String, String> regions = Maps.newLinkedHashMap();

    while (reader.hasNext()) {
      reader.next();
      String tag;
      switch (reader.getEventType()) {
         case XMLEvent.START_ELEMENT:
           tag = reader.getName().getLocalPart();
           if (tag.equals("country")) {
             String name = reader.getAttributeValue(null, "name");
             if (name == null) {
               error(lineNumberReader, "Attr 'country' is missing");
             }
             String isoCode = reader.getAttributeValue(null, "iso-code");
             if (isoCode == null) {
               error(lineNumberReader, "Attr 'iso-code' is missing");
             }

             regions.put(isoCode, StringUtils.join(regionStack, REGION_SEPARATOR));
           }
           if (tag.equals("region")) {
             String name = reader.getAttributeValue(null, "name");
             if (name == null) {
               error(lineNumberReader, "Attr 'country' is missing");
             }
             regionStack.add(name);
           }
           break;

         case XMLEvent.END_ELEMENT:
           tag = reader.getName().getLocalPart();
           if (tag.equals("region")) {
             regionStack.pop();
           }
           break;
         }
    }
    return regions;
  }

  private static void error(LineNumberReader lineNumberReader, String errMsg) throws XMLStreamException {
    throw new XMLStreamException("Error in line " + lineNumberReader.getLineNumber() + ": " + errMsg);
  }

}
