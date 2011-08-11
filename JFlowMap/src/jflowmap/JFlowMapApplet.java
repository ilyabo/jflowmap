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

import java.awt.Container;
import java.io.StringWriter;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import jflowmap.views.VisualCanvas;

import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;

import com.google.common.base.Strings;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapApplet extends JApplet {

  private static Logger logger = Logger.getLogger(JFlowMapApplet.class);

  public JFlowMapApplet() {
  }


  @Override
  public void init() {
    try {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          createUI();
        }
      });
    } catch (Exception ex) {
      logger.error(ex);
      JMsgPane.showProblemDialog(JFlowMapApplet.this, ex);
    }
  }

  public String exportToSvg() {
    Container cp = getContentPane();
    if (cp instanceof VisualCanvas) {
      VisualCanvas canvas = (VisualCanvas)cp;
      try {
        StringWriter sw = new StringWriter();
        canvas.paintToSvg(sw);
        return sw.getBuffer().toString();
      } catch (Exception e) {
        logger.error("SVG export failed", e);
      }
    }
    return "";
  }

  private void createUI() {
    String viewConfig = getParameter("viewConfig");
    if (Strings.isNullOrEmpty(viewConfig)) {
      JMsgPane.showProblemDialog(JFlowMapApplet.this,
          "Please, specify the location of the .jfmv view configuration " +
          "in the 'viewConfig' applet parameter");
    } else {
      try {
        ViewLoader.loadView(viewConfig, getContentPane());
      } catch (Exception ex) {
        logger.error("Cannot open view", ex);
        JMsgPane.showProblemDialog(JFlowMapApplet.this, ex);
      }
    }
  }

}
