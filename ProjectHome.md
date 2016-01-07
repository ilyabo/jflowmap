(This project is not developed further anymore. Check out my later work [on a JavaScript port](https://github.com/ilyabo/jflowmap.js)).

JFlowMap is a research prototype developed at the University of Fribourg in which we experiment with various visualization techniques for **spatial interactions**, i.e. interactions between pairs of geographic locations. These can be migrations, movement of goods and people, network traffic, or any kind of entities "flowing" between locations. Spatial interactions are often represented as **origin-destination data**, meaning that only the origins, the destinations and the magnitudes of the flows are known, but not the exact flow routes.

The goal of our work is to develop a tool which would help to explore and analyze **temporal changes** in origin-destination data. This is what our novel visualization, [Flowstrates](Flowstrates.md), is especially aimed for.

Check the [DEMO applets](http://jflowmap.googlecode.com/svn/trunk/JFlowMap/demo/demo-applets.html).

<a href='http://jflowmap.googlecode.com/svn/wiki/images/jflowmap-desktop.png' title='JFlowMap desktop version'><img src='http://jflowmap.googlecode.com/svn/wiki/images/jflowmap-desktop-192x120.png' /></a> <a href='http://jflowmap.googlecode.com/svn/wiki/images/JFlowMap-refugees.jpg' title="World's refugee flows (UNdata)"><img src='http://jflowmap.googlecode.com/svn/wiki/images/JFlowMap-refugees-thumb.jpg' /></a> <a href='http://jflowmap.googlecode.com/svn/wiki/images/JFlowMap-us-migrations-bundled.jpg' title='Force-directed edge bundling applied to the US migrations data (US census 2000)'><img src='http://jflowmap.googlecode.com/svn/wiki/images/JFlowMap-us-migrations-bundled-thumb.jpg' /></a> <a href='http://jflowmap.googlecode.com/svn/wiki/images/slo-commuters.png' title='Commuters in Slovenia'><img src='http://jflowmap.googlecode.com/svn/wiki/images/slo-commuters-sm.png' /></a>



<a href='http://jflowmap.googlecode.com/svn/wiki/images/refugees-2008-light.png' title='Refugees 2008'><img src='http://jflowmap.googlecode.com/svn/wiki/images/refugees-2008-light-152x120.png' /></a> <a href='http://jflowmap.googlecode.com/svn/wiki/images/refugees-bundled.png ' title='Refugees 2000 bundled'><img src='http://jflowmap.googlecode.com/svn/wiki/images/refugees-bundled-164x120.png ' /></a> <a href='http://jflowmap.googlecode.com/svn/wiki/images/Sudan-to-Europe-stroke.png' title='Flowstrates: Refugees from Sudan in 1975-2009'><img src='http://jflowmap.googlecode.com/svn/wiki/images/flowstrates/Flowstrates3_x120.png' /></a>





You can also watch [the video of the Flowstrates demo](http://youtu.be/UQPN7o6A3Cg?hd=1)  or read our [paper on Flowstrates](http://diuf.unifr.ch/people/boyandii/papers/flowstrates-eurovis11.pdf) published in Computer Graphics Forum.

If you want to visualize your own data in JFlowMap, check HowToPrepareData.


---

Send your questions and suggestions to the [JFlowMap discussion group](http://groups.google.com/group/jflowmap) (so that it can help others as well) or directly to Ilya Boyandin <[ilya.boyandin@unifr.ch](mailto:ilya.boyandin@unifr.ch?subject=JFlowMap)>.

If you discover a problem or want to propose an improvement, please, [submit an issue](http://code.google.com/p/jflowmap/issues/list).

If you use JFlowMap for a scientific publication, please, reference <a href='http://diuf.unifr.ch/main/diva/content/using-flow-maps-explore-migrations-over-time'>this paper</a> if you use the flow map view or bundling, and <a href='http://onlinelibrary.wiley.com/doi/10.1111/j.1467-8659.2011.01946.x/abstract'>this one</a> if you use Flowstrates.

---

JFlowMap is developed by <a href='http://boyandi.net/'>Ilya Boyandin</a> under the supervision of <a href='http://diuf.unifr.ch/people/lalanned/'>Denis Lalanne</a> and <a href='http://enrico.bertini.me'>Enrico Bertini</a>.