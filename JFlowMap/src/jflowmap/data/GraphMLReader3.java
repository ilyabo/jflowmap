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

package jflowmap.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.ParserFactory;

/**
 * TODO: Finish GraphMLReader3 based on StAX: it should take much less memory than GraphMLReader2
 *
 * @author Ilya Boyandin
 */
public class GraphMLReader3 {

    public static final String GRAPH_CLIENT_PROPERTY__ID = "id";

    private ParserFactory dataParser;
    private LineNumberReader lineNumberReader;

    private GraphMLReader3() {
    }

    public Iterable<Graph> readGraph(InputStream is) throws DataIOException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader;
        try {
            lineNumberReader = new LineNumberReader(new InputStreamReader(is));
            reader = inputFactory.createXMLStreamReader(lineNumberReader);

            while (reader.hasNext()) {
                int type = reader.nextTag();

            }

            return null;
        } catch (Exception e) {
            throw new DataIOException("Parse error in line " + lineNumberReader.getLineNumber() + ": " + e.getMessage(), e);
        }
    }

    private Object parseData(String defaultValStr, Class<?> klass) throws DataIOException {
        try {
            return dataParser.getParser(klass).parse(defaultValStr);
        } catch (DataParseException e) {
            throw new DataIOException(e);
        }
    }

}
