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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jflowmap.FlowMap;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlNamespace;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.ParserFactory;
import prefuse.util.collections.IntIterator;
import prefuse.util.io.IOLib;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Ilya Boyandin
 */
public class GraphMLReader2 {

    private static final String NAMESPACE = "http://graphml.graphdrawing.org/xmlns";

    private static final String SRC = Graph.DEFAULT_SOURCE_KEY;
    private static final String TRG = Graph.DEFAULT_TARGET_KEY;
    private static final String SRCID = SRC + "_id";
    private static final String TRGID = TRG + "_id";

    private ParserFactory dataParser;
    private Schema nodeSchema, edgeSchema;
    private XmlNamespace namespace;
    private Map<String, String> attrIdToName;

    public Iterable<Graph> readFromFile(String filename) throws DataIOException {
        try {
            return readFromStream(IOLib.streamFromString(filename));
        } catch (DataIOException e) {
            throw e;
        } catch (IOException e) {
            throw new DataIOException(e);
        }
    }

    public Iterable<Graph> readFromStream(InputStream is) throws DataIOException {
        try {
            XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
            XmlDocument doc = builder.parseReader(new InputStreamReader(is));
            namespace = builder.newNamespace(NAMESPACE);

            dataParser = ParserFactory.getDefaultFactory();

            readKeys(doc.getDocumentElement());

            Iterable<Graph> graphs = readGraphs(doc.getDocumentElement());

            if (Iterables.size(graphs) == 0) {
                throw new DataIOException("No graphs found");
            }

            return graphs;
        } catch (Exception e) {
            throw new DataIOException(e);
        }
    }

    private void readKeys(XmlElement root) throws DataIOException {
        // Read the attr definitions
        nodeSchema = new Schema();
        nodeSchema.addColumn(FlowMap.GRAPH_NODE_TABLE_COLUMN_NAME__ID, String.class);
        edgeSchema = new Schema();
        edgeSchema.addColumn(SRC, int.class);
        edgeSchema.addColumn(TRG, int.class);
        edgeSchema.addColumn(SRCID, String.class);
        edgeSchema.addColumn(TRGID, String.class);

        attrIdToName = Maps.newHashMap();
        for (Iterator<?> it = root.elements(namespace, "key").iterator(); it.hasNext(); ) {
            XmlElement keyNode = (XmlElement) it.next();
            String id = keyNode.getAttributeValue(null, "id");
            String forWhat = keyNode.getAttributeValue(null, "for");
            String name = keyNode.getAttributeValue(null, "attr.name");
            Types type = Types.parse(keyNode.getAttributeValue(null, "attr.type"));

            attrIdToName.put(id, name);

            String defaultValStr = keyNode.getAttributeValue(null, "default");
            final Object defaultVal;
            if (defaultValStr == null) {
                defaultVal = null;
            } else {
                defaultVal = parseData(defaultValStr, type.klass);
            }

            if (forWhat == null  ||  forWhat.equals("all")) {
                nodeSchema.addColumn(name, type.klass, defaultVal);
                edgeSchema.addColumn(name, type.klass, defaultVal);
            } else if (forWhat.equals("node")) {
                nodeSchema.addColumn(name, type.klass, defaultVal);
            } else if (forWhat.equals("edge")) {
                edgeSchema.addColumn(name, type.klass, defaultVal);
            } else {
                throw new DataIOException("Unrecognized 'for' value: " + forWhat);
            }
        }
        nodeSchema.lockSchema();
        edgeSchema.lockSchema();
    }

    private Iterable<Graph> readGraphs(XmlElement root) throws DataIOException {
        // Read the graphs
        List<Graph> graphs = Lists.newArrayList();

        for (Iterator<?> git = root.elements(namespace, "graph").iterator(); git.hasNext(); ) {

            // Start reading ONE graph
            Map<String, Integer> nodeIdToIndex = Maps.newHashMap();
            XmlElement graphElt = (XmlElement) git.next();

            boolean directed = ("directed".equals(graphElt.getAttributeValue(null, "edgedefault")));

            // Read the nodes
            Table nodeTable = nodeSchema.instantiate();
            for (Iterator<?> nit = graphElt.elements(namespace, "node").iterator(); nit.hasNext(); ) {
                XmlElement nodeElt = (XmlElement) nit.next();
                int ri = nodeTable.addRow();
                String nodeId = nodeElt.getAttributeValue(null, "id");
                nodeIdToIndex.put(nodeId, ri);

                nodeTable.set(ri, FlowMap.GRAPH_NODE_TABLE_COLUMN_NAME__ID, nodeId);

                readData(nodeElt, nodeTable, ri);
            }

            // Read the edges
            Table edgeTable = edgeSchema.instantiate();
            for (Iterator<?> eit = graphElt.elements(namespace, "edge").iterator(); eit.hasNext(); ) {
                XmlElement edgeElt = (XmlElement) eit.next();
                int ri = edgeTable.addRow();

                edgeTable.setString(ri, SRCID, edgeElt.getAttributeValue(null, "source"));
                edgeTable.setString(ri, TRGID, edgeElt.getAttributeValue(null, "target"));

                readData(edgeElt, edgeTable, ri);
            }

            // Map edge starts/ends to nodes
            IntIterator rows = edgeTable.rows();
            while (rows.hasNext()) {
                int ri = rows.nextInt();

                String src = edgeTable.getString(ri, SRCID);
                if (!nodeIdToIndex.containsKey(src)) {
                    throw new DataIOException(
                        "Tried to create edge with source node id=" + src
                        + " which does not exist.");
                }
                edgeTable.setInt(ri, SRC, nodeIdToIndex.get(src));

                String trg = edgeTable.getString(ri, TRGID);
                if (!nodeIdToIndex.containsKey(trg)) {
                    throw new DataIOException(
                        "Tried to create edge with target node id=" + trg
                        + " which does not exist.");
                }
                edgeTable.setInt(ri, TRG, nodeIdToIndex.get(trg));
            }
            edgeTable.removeColumn(SRCID);
            edgeTable.removeColumn(TRGID);

            // Finally, create the graph
            Graph graph = new Graph(nodeTable, edgeTable, directed);
            FlowMap.setGraphId(graph, graphElt.getAttributeValue(null, "id"));
            graphs.add(graph);
        }

        return graphs;
    }

    private void readData(XmlElement parentElt, Table table, int tableRowIdx)
            throws DataIOException {
        for (Iterator<?> dit = parentElt.elements(namespace, "data").iterator(); dit.hasNext(); ) {
            XmlElement dataNode = (XmlElement) dit.next();
            String key = attrIdToName.get(dataNode.getAttributeValue(null, "key"));
            String valueStr = dataNode.requiredTextContent();
            Object value;
            if (valueStr == null) {
                value = null;
            } else {
                Class<?> columnType = table.getColumnType(key);
                if (columnType == null) {
                    throw new DataIOException("Column type for " + key + " not found");
                }
                value = parseData(valueStr, columnType);
                table.set(tableRowIdx, key, value);
            }
        }
    }

    private Object parseData(String defaultValStr, Class<?> klass) throws DataIOException {
        try {
            return dataParser.getParser(klass).parse(defaultValStr);
        } catch (DataParseException e) {
            throw new DataIOException(e);
        }
    }

    enum Types {
        INT(int.class, "int", "integer"),
        LONG(long.class, "long"),
        FLOAT(float.class, "float"),
        DOUBLE(double.class, "double", "real"),
        BOOLEAN(boolean.class, "boolean"),
        STRING(String.class, "string"),
        DATE(Date.class, "date");

        private final Class<?> klass;
        private final String[] names;

        private Types(Class<?> klass, String ... names) {
            this.klass = klass;
            this.names = names;
        }

        public static Types parse(String typeName) {
            for (Types type : values()) {
                for (String name : type.names) {
                    if (typeName.equals(name)) {
                        return type;
                    }
                }
            }
            throw new IllegalArgumentException("Type " + typeName + " is not supported");
        }
    }

}
