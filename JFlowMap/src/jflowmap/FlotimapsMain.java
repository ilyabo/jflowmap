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

package jflowmap;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

import jflowmap.geo.MapProjections;
import jflowmap.models.map.AreaMap;
import jflowmap.views.flowtimaps.FlowtimapsView;

/**
 * @author Ilya Boyandin
 */
public class FlotimapsMain extends JFrame {

  public FlotimapsMain(DatasetSpec ds) throws IOException {
    super("DuoTimeline");
    final FlowtimapsView view = new FlowtimapsView(
        FlowMapGraph.loadGraphML(ds), AreaMap.load(ds.getAreaMapFilename())
    );
    add(view.getViewComponent());
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        view.fitInView();
      }
    });
    setSize(1600, 800);
//    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setVisible(true);
  }

  public static void main(String[] args) throws IOException {
    new FlotimapsMain(new DatasetSpec(
        "data/refugees/refugees.xml.gz",
        "r.*",
        "lon", "lat",
        "name",
        "data/refugees/countries-areas-ll.xml.gz",
        MapProjections.MERCATOR));
  }

}
