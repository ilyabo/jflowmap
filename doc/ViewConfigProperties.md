<table cellpadding='10' border='0' cellspacing='0'>

<tr>
<th>Property</th><th>Description</th><th>Supported values / Sample use</th>
</tr>
<tr><td><b>view</b></td><td>The view in which the data is to be shown.</td><td>flowmap, flowmapSmallMultiple, flowstrates</td></tr>



<tr>
<td><b>view.flowmap.colorScheme</b></td>
<td>Color scheme for the flow map view.</td>
<td>Dark, Light, Light Blue, Inverted, Gray red-green</td>
</tr>



<tr>
<td><b>data</b></td>
<td>Input data format.</td>
<td>csv, graphml</td>
</tr>

<tr>
<td><b>data.csv.separator</b></td>
<td>CSV field separator character.</td>
<td>E.g. , or ;</td>
</tr>
<tr>
<td><b>data.csv.charset</b></td>
<td>CSV files encoding.</td>
<td>e.g. utf-8 or windows-1252</td>
</tr>
<tr>
<td><b>data.csv.flows.src</b></td>
<td>Location of the CSV file with the flows.</td>
<td></td>
</tr>
<tr>
<td><b>data.csv.nodes.src</b></td>
<td>Location of the CSV file with the nodes.</td>
<td></td>
</tr>


<tr>
<td><b>data.graphml.src</b></td>
<td>Location of the GraphML file with the flows.</td>
<td></td>
</tr>



<tr>
<td><b>data.attrs.node.id</b></td>
<td>Name of the node attribute with the node ids.</td>
<td></td>
</tr>

<tr>
<td><b>data.attrs.node.label</b></td>
<td>Name of the node attribute with the node labels.</td>
<td></td>
</tr>

<tr>
<td><b>data.attrs.node.lat</b></td>
<td>Name of the node attribute with the node latitudes.</td>
<td></td>
</tr>

<tr>
<td><b>data.attrs.node.lon</b></td>
<td>Name of the node attribute with the node longitudes.</td>
<td></td>
</tr>

<tr>
<td><b>data.attrs.flow.origin</b></td>
<td>Name of the flow attribute with the flow origins (the values must correspond to the node ids).</td>
<td></td>
</tr>

<tr>
<td><b>data.attrs.flow.dest</b></td>
<td>Name of the flow attribute with the flow destinations (the values must correspond to the node ids).</td>
<td></td>
</tr>
<tr>
<td><b>data.attrs.flow.weight.csvList</b></td>
<td>Comma-separated list of flow attributes to be used as flow weight attributes.</td>
<td>1975,1976,1977,1978,1979</td>
</tr>
<tr>
<td><b>data.attrs.flow.weight.re</b></td>
<td>Regular expression for matching the flow attributes to be used as flow weight attributes (this is an alternative to data.attrs.flow.weight.csvList).</td>
<td>Match all four-digit years: <code>[0-9]{4</code>}</td>
</tr>




<tr>
<td><b>data.select.nodes.where</b></td>
<td>Filter criteria for nodes (like a WHERE clause in SQL).</td>
<td>Select only countries in Europe: <code>([region]='Europe')</code></td>
</tr>

<tr>
<td><b>data.select.flows.where</b></td>
<td>Filter criteria for flows.</td>
<td>Remove self-loops: <code>([source]!=[target])</code></td>
</tr>

<tr>
<td><b>data.select.flows.where.exists</b></td>
<td>Filter criteria for flows which must be satisfied for <b>at least one</b> flow weight attribute in order for a flow to be accepted. #weightAttr# can be used as a placeholder for the weight attributes.</td>
<td>Select only flows which have at least one attribute of the value greater than 100:<br>
<code> (NOT ISNAN([#weightAttr#])) AND ([#weightAttr#] &gt; 100) </code>
</td>
</tr>
<tr>
<td><b>data.select.flows.where.forAll</b></td>
<td>Filter criteria for flows which must be satisfied for <b>all</b> the flow weight attributes in order for a flow to be accepted. #weightAttr# can be used as a placeholder for the weight attributes.</td>
<td></td>
</tr>



<tr>
<td><b>map</b></td>
<td>Type of the map to use.</td>
<td>shapefile, xml</td>
</tr>



<tr>
<td><b>map.shapefile.src</b></td>
<td>Location of the map shapefile.</td>
<td></td>
</tr>


<tr>
<td><b>map.shapefile.dbf.areaIdField</b></td>
<td>Name of the DBF field with the IDs of the areas (to let JFlowMap to relate the polygons in the map to their corresponding nodes).</td>
<td></td>
</tr>


<tr>
<td><b>map.shapefile.dbf.select.shapes.where</b></td>
<td>Filter query for shapes based on the DBF field records' values. Only the shapes satisfying the query will be retained.</td>
<td>Select only countries of the population more than one million: <code>[POP_EST]&gt;1000000.0</code></td>
</tr>


<tr>
<td><b>map.projection</b></td>
<td>Projection to use for the nodes and map shapes.</td>
<td>None, FlipY, Mercator, WinkelTripel</td>
</tr>


<tr>
<td><b>map.background.src</b></td>
<td>Location of an image (PNG, JPG, GIF etc) to use as a background map. The image will be used as is, not projected. </td>
<td></td>
</tr>

<tr>
<td><b>map.background.boundingBox</b></td>
<td>Bounding box (in the projected coordinates) of the background map image.</td>
<td>7.303,-29.761,2.129,1.541</td>
</tr>

<tr>
<td><b>map.background.transparency</b></td>
<td>Transparency of the background map image.</td>
<td>a value from 0.0 to 1.0</td>
</tr>






<tr>
<td><b>view.flowstrates.messages.originsMapCaption</b></td>
<td>Origins map caption text.</td>
<td>default: Origins</td>
</tr>
<tr>
<td><b>view.flowstrates.messages.destsMapCaption</b></td>
<td>Destinations map caption text.</td>
<td>default: Destinations</td>
</tr>




<tr><td><b>window.size</b></td><td>The size of the view window will be set to the given dimensions.</td><td>e.g. 1024x768</td></tr>

<tr><td><b>window.settings.show</b></td><td>Show or hide the settings window when the app starts.</td><td>true or false</td></tr>

<tr><td><b>window.settings.activeTab</b></td><td>Which tab of the settings window to activate when the app starts.</td><td>e.g. Filter, Aesthetics, Animation</td></tr>


<tr>
<td><b>window.settings.showTabs</b></td>
<td>Which tabs of the settings window to show when the app starts (the others will be hidden).</td>
<td>Comma-separated list of the tab titles.</td>
</tr>


<tr>
<td><b>window.settings.embed</b></td>
<td>Whether the settings window should be embedded into the view as a panel.</td>
<td>true or false</td>
</tr>







</table>
