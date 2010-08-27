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

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;

/**
 * @author Ilya Boyandin
 */
public abstract class BaseApplet extends JApplet {

  protected IView view;

  protected abstract IView createView() throws IOException;

  public BaseApplet(String name) {
    super();
    setName(name);
  }


  @Override
  public void init() {
    try {
      view = createView();
      add(view.getViewComponent());

      initControls();
    } catch (IOException ex) {
      JMsgPane.showProblemDialog(this, "Error: " + ex.getMessage());
      Logger.getLogger(getClass()).error("Exception: ", ex);
    }
  }

  protected void initControls() {
    JComponent controls = view.getControls();
    if (controls != null) {
        add(controls, BorderLayout.NORTH);

//      JFrame frame = new JFrame(getName() + " Controls");
//      frame.setResizable(false);
//      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//      frame.add(controls);
//      frame.pack();
//      frame.setVisible(true);
    }
  }

  @Override
  public void start() {
    view.fitInView();
  }

  protected DatasetSpec getDatasetSpec() {
    return new DatasetSpec(
        getParameter("src"),
        getParameter("weightAttr"),
        getParameter("xNodeAttr"),
        getParameter("yNodeAttr"),
        getParameter("labelAttr"),
        getParameter("areaMapSrc"),
        getDoubleParameter("weightFilterMin"));
  }

  protected double getDoubleParameter(String name) {
    double value;
    String strValue = getParameter(name);
    if (strValue != null  &&  strValue.length() > 0) {
      value = Double.parseDouble(strValue);
    } else {
      value = Double.NaN;
    }
    return value;
  }


}
