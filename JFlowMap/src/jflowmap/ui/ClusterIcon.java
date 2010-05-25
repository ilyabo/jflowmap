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

package jflowmap.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.Icon;

class ClusterIcon implements Icon, Comparable<ClusterIcon> {

    final int r = 8;
    private final Paint clusterPaint;
    private final int clusterId;

    public ClusterIcon(int clusterId, Paint clusterPaint) {
      this.clusterId = clusterId;
      this.clusterPaint = clusterPaint;
    }

    public int getIconHeight() {
      return r * 2 + 2;
    }

    public int getIconWidth() {
      return r * 2 + 2 + 32;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle b = c.getBounds();
      int ox = x + 2;
      int oy = y + 3;
      g2.setColor(Color.darkGray);
      g2.drawOval(ox, oy, r, r);
      g2.setPaint(clusterPaint);
      g2.fillOval(ox, oy, r, r);
      g2.setColor(Color.black);
      Font f = g2.getFont();
      g2.drawString(Integer.toString(clusterId), x + r * 2 + 2 + 2, y + (b.height + f.getSize())/2 - 1);
    }

    public int compareTo(ClusterIcon o) {
//      if (clusterId <= 0  &&  o.clusterId > 0) return +1;
//      if (clusterId > 0  &&  o.clusterId <= 0) return -1;
      return clusterId - o.clusterId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + clusterId;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ClusterIcon other = (ClusterIcon) obj;
      if (clusterId != other.clusterId)
        return false;
      return true;
    }

  }