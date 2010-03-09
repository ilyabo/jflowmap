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
import java.util.Collection;
import java.util.List;

import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

import prefuse.data.Graph;
import prefuse.data.io.DataIOException;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class GraphMLReader2 {


    @SuppressWarnings("unchecked")
    public Collection<Graph> readGraph(InputStream is) throws DataIOException {
        XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
        XmlDocument doc = builder.parseReader(new InputStreamReader(is));

        Xb1XPath
            keysPath = new Xb1XPath("/key"),
            graphsPath = new Xb1XPath("/graph"),
            nodesPath = new Xb1XPath("/nodes"),
            edgesPath = new Xb1XPath("/edges")
            ;

        List<Graph> graphs = Lists.newArrayList();


        List<XmlElement> graphNodes = graphsPath.selectNodes(doc);
        for (XmlElement graphNode : graphNodes) {
            Graph graph = new Graph();

            graphNode.getAttributeValue(null, "directed");

            // Create the attr definitions
            List<XmlElement> keyNodes = keysPath.selectNodes(doc);
            for (XmlElement keyNode : keyNodes) {
                String id = keyNode.getAttributeValue(null, "id");
                String forNodeOrEdge = keyNode.getAttributeValue(null, "for");
                String name = keyNode.getAttributeValue(null, "attr.name");
                String type = keyNode.getAttributeValue(null, "attr.type");
            }


            // Create nodes

            // Create edges

            graphs.add(graph);
        }


        return graphs;
    }

}
