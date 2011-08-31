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

import java.applet.Applet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import jflowmap.ui.BlockingGlassPane;
import jflowmap.util.SwingUtils;
import jflowmap.views.VisualCanvas;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import at.fhj.utils.swing.JMsgPane;

import com.google.common.base.Strings;

/**
 * @author Ilya Boyandin
 */
public class JFlowMapApplet extends JApplet {

  private static Logger logger = Logger.getLogger(JFlowMapApplet.class);
  private final BlockingGlassPane blockingGlassPane;

  public JFlowMapApplet() {
    SwingUtils.initNimbusLF();
    blockingGlassPane = new BlockingGlassPane();
    setGlassPane(blockingGlassPane);
    blockingGlassPane.setVisible(false);
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

  public String exportToPng() {
    String bytes = null;
    VisualCanvas canvas = getCanvas();
    if (canvas != null) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        canvas.paintToPng(out);
        bytes = new String(Base64.encodeBase64(out.toByteArray()));

      } catch (IOException e) {
        logger.error("PNG export failed", e);
        JMsgPane.showErrorDialog(this,
            "SVG export failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      }
    }
    return bytes;
  }

  public String exportToSvg() {
    blockingGlassPane.setVisible(true);
    String svg = null;
    VisualCanvas canvas = getCanvas();
    if (canvas != null) {
      try {
        StringWriter sw = new StringWriter();
        canvas.paintToSvg(sw);

        svg = sw.getBuffer().toString();
      } catch (Exception e) {
        logger.error("SVG export failed", e);
        JMsgPane.showErrorDialog(this,
            "SVG export failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
      }
    }
    blockingGlassPane.setVisible(false);
    return svg;
  }

  /**
   * Notify the web page in which the applet is running that the view loaded.
   */
  public void jsFlowMapViewLoaded() {
    jsFlowMapFunctionCall("viewLoaded");
  }

  public Object jsFlowMapFunctionCall(String memberFunction, Object ... args) {
    Class<?> klass;
    try {
      klass = Class.forName("netscape.javascript.JSObject");
      Object window = klass.getMethod("getWindow", Applet.class).invoke(null, this); // static
      Method call = klass.getMethod("call", String.class, Object[].class);
      Method getMember = klass.getMethod("getMember", String.class);
      Object jFlowMap = getMember.invoke(window, "jsFlowMap");
      if (jFlowMap != null) {
        return call.invoke(jFlowMap, memberFunction, args != null ? args : new Object[] { });
      }
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e);
    }
    return null;
  }


  public VisualCanvas getCanvas() {
    return SwingUtils.getChildOfType(getContentPane(), VisualCanvas.class);
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
