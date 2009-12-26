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

package jflowmap.data._old;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.xpath.Xb1XPath;

/**
 * @author Ilya Boyandin
 */
public class XmlNodeData extends AbstractNodeData {

	private XmlNodeData() {
	}

	public static INodeData loadFrom(URL resource) throws XmlBuilderException, XmlPullParserException, IOException {
		XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
		return loadFrom(builder.parseLocation(resource.toString()));
	}

	public static XmlNodeData loadFrom(String filename) throws XmlPullParserException, IOException {
		XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance();
		return loadFrom(builder.parseReader(new FileReader(filename)));
	}
	
	private static XmlNodeData loadFrom(XmlDocument doc) throws XmlPullParserException, IOException {
		XmlNodeData data = new XmlNodeData();

		@SuppressWarnings("unchecked")
		List<XmlElement> nodes = new Xb1XPath("/nodes/node").selectNodes(doc);
		for (XmlElement node : nodes) {
			data.addNode(
					node.getAttributeValue(null, "id"), node.getAttributeValue(null, "label"),
					Double.parseDouble(node.getAttributeValue(null, "x")),
					Double.parseDouble(node.getAttributeValue(null, "y")));
		}
		
		/*
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		xpp.setInput(new FileReader(filename));
		
		List<String> ids = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		List<Point2D.Double> positions = new ArrayList<Point2D.Double>();
		
		Stack<String> stack = new Stack<String>();

		int event = xpp.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				stack.add(xpp.getName());
				if (stack.get(stack.size() - 2).equals("nodes")  &&  xpp.getName().equals("node")) {
					ids.add(xpp.getAttributeValue(null, "id"));
					labels.add(xpp.getAttributeValue(null, "label"));
					positions.add(
							new Point2D.Double(
								Double.parseDouble(xpp.getAttributeValue(null, "x")),
								Double.parseDouble(xpp.getAttributeValue(null, "y"))
							));
				}
				break;
			case XmlPullParser.END_TAG:
				stack.pop();
				break;
			case XmlPullParser.TEXT:
				break;
			}
			event = xpp.next();
		}
		
		return new XmlNodeData(ids, labels, positions);
		*/
		
		return data;
	}

}
