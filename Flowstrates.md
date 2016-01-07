Flowstrates extend the idea of a [flow map](http://en.wikipedia.org/wiki/Flow_map) (which are used for representing entities flowing between geographic locations) to the temporal dimension and allow to analyze the changes of the flow magnitudes over time.
In Flowstrates the origins and the destinations of the flows are displayed in two separate maps, and the temporal changes of the flow magnitudes are displayed between the two maps in a heatmap in which the columns represent time periods.

<a href='http://jflowmap.googlecode.com/svn/wiki/images/Sudan-to-Europe-stroke.png' title='Flowstrates: Refugees from Sudan in 1975-2009'><img src='http://jflowmap.googlecode.com/svn/wiki/images/flowstrates/Flowstrates3_x120.png' /></a>

As in most flow maps which focus on representing the flow magnitudes, the exact routes of the flows are not accurately represented in Flowstrates. Instead, the flow lines are rerouted so that
they connect the flow origins and destinations with the corresponding rows of the heatmap, as if the flows were going
through it.
The flow lines help to see in the geographic maps the  origins and the destinations corresponding to each of the flows shown in the heatmap.


Flowstrates are meant for interactive exploration. Unlike
[OD-matrices](http://people.hofstra.edu/geotrans/eng/ch5en/meth5en/odmatrix.html) which represent exactly one flow in each
matrix cell, in Flowstrates every flow occupies one whole row
of the heatmap for showing how the flow changed over time. Hence, much more screen real estate is used
to represent the same number of flows, and for many
datasets it is impossible to display all the flows simultaneously on the screen
without filtering or aggregating them.
Therefore, to allow the users to explore the whole data in every bit of
detail Flowstrates provide interactive support for performing spatial visual queries, focusing on different regions of interest for the origins
and destinations, zooming and panning, sorting and aggregating the heatmap rows.


Here is a short video which can give you an impression of how Flowstrates can be used:

<a href='http://www.youtube.com/watch?feature=player_embedded&v=nij8OUyiaV0' target='_blank'><img src='http://img.youtube.com/vi/nij8OUyiaV0/0.jpg' width='590' height=355 /></a>

Try out a [demo of Flowstrates](http://jflowmap.googlecode.com/svn/trunk/JFlowMap/demo/demo-applets.html?refugees-flowstrates) representing refugee flows between the world's countries.

You can also watch [the video of the Flowstrates demo](http://youtu.be/UQPN7o6A3Cg?hd=1)  or read our [paper on Flowstrates](http://diuf.unifr.ch/people/boyandii/papers/flowstrates-eurovis11.pdf) published in Computer Graphics Forum.