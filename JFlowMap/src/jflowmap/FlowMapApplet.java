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

import java.util.Arrays;

import javax.swing.JApplet;

/**
 * TODO: finish FlowMapApplet
 *
 * @author Ilya Boyandin
 */
public class FlowMapApplet extends JApplet {

  private static final long serialVersionUID = 1778664403741899654L;
  private JFlowMap jFlowMap;

  public FlowMapApplet() {
  }


  @Override
  public void init() {
    double weightFilterMin;
    String weightFilterMinStr = getParameter("weightFilterMin");
    if (weightFilterMinStr != null  &&  weightFilterMinStr.length() > 0) {
      weightFilterMin = Double.parseDouble(weightFilterMinStr);
    } else {
      weightFilterMin = Double.NaN;
    }
    jFlowMap = new JFlowMap(Arrays.asList(new DatasetSpec(
        getParameter("src") , getParameter("weightAttr"),
        getParameter("xNodeAttr"), getParameter("yNodeAttr"), getParameter("labelAttr"),
        getParameter("areaMapSrc"), weightFilterMin)), true);
    add(jFlowMap);

  }

  @Override
  public void start() {
    super.start();
    if (jFlowMap != null) {
      jFlowMap.fitInView();
    }
  }

}
