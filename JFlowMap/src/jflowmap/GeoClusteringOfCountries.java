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

package jflowmap;

import java.io.IOException;
import java.util.List;

import jflowmap.data.GraphMLReader3;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Node;
import at.fhj.utils.misc.ProgressTracker;
import ch.unifr.dmlib.cluster.ClusterNode;
import ch.unifr.dmlib.cluster.DistanceMeasure;
import ch.unifr.dmlib.cluster.HierarchicalClusterer;
import ch.unifr.dmlib.cluster.Linkages;

import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class GeoClusteringOfCountries {
  private static Logger logger = Logger.getLogger(GeoClusteringOfCountries.class);

  public static void main(String[] args) throws IOException {
    GraphMLReader3 reader = new GraphMLReader3();
    Graph g = reader.readFromLocation("data/refugees/refugees-2008.xml.gz").iterator().next();

    List<Country> countries = Lists.newArrayList();

    for (int i = 0, numNodes = g.getNodeCount(); i < numNodes; i++) {
      Node node = g.getNode(i);
      countries.add(new Country(
          node.getString("name"), node.getString("code"),
          node.getDouble("x"), node.getDouble("y")
      ));
    }


    DistanceMeasure<Country> dm = new DistanceMeasure<Country>() {
      @Override
       public double distance(Country t1, Country t2) {
        double dx = t1.getX() - t2.getX();
        double dy = t1.getY() - t2.getY();
        return Math.sqrt(dx * dx + dy * dy);
       }
    };


    logger.info("Starting clustering nodes");
    ClusterNode<Country> c =
       HierarchicalClusterer
       .<Country>createWith(dm,
//           Linkages.<Country>average()
//           Linkages.<Country>single()
           Linkages.<Country>complete()
       )
       .build()
       .clusterToRoot(countries, new ProgressTracker());

    int[] ind = c.getItemIndices();
    for (int i : ind) {
      System.out.println(countries.get(i).getName());
    }


  }

  static class Country {
    private final String name, code;
    private final double x, y;
    public Country(String name, String code, double x, double y) {
      this.name = name;
      this.code = code;
      this.x = x;
      this.y = y;
    }
    public String getName() {
      return name;
    }
    public String getCode() {
      return code;
    }
    public double getX() {
      return x;
    }
    public double getY() {
      return y;
    }

  }

}
