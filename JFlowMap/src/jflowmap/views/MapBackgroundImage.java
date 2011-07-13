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

package jflowmap.views;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import jflowmap.data.ViewConfig;
import prefuse.util.io.IOLib;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Ilya Boyandin
 */
public class MapBackgroundImage {

  private final Image image;
  private final double scale;
  private final double offsetX, offsetY;
  private final double transparency;
  private final PBounds boundingBox;

  private MapBackgroundImage(String location, PBounds boundingBox,
      double scale, double offsetX, double offsetY, double transparency) {
//    this.image = Toolkit.getDefaultToolkit().getImage(imageFilename);
    this.image = readImage(location);
    this.scale = scale;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.transparency = transparency;
    this.boundingBox = boundingBox;
  }

  private Image readImage(String location) {
    Image image;
    try {
      image = ImageIO.read(IOLib.streamFromString(location));
    } catch (IOException e) {
      throw new IllegalArgumentException("Location '" + location + "' could nod be read: " +
          e.getMessage());
    }
    return image;
  }

  public static MapBackgroundImage parseConfig(ViewConfig config) {
    String src = config.getString(ViewConfig.PROP_MAP_BACKGROUND_SRC);
    if (src == null) {
      return null;
    }
    src = config.relativeFileLocation(src);

    return new MapBackgroundImage(src,
        getBoundingBox(config),
        config.getDoubleOrElse(ViewConfig.PROP_MAP_BACKGROUND_SCALE, 1.0),
        config.getDoubleOrElse(ViewConfig.PROP_MAP_BACKGROUND_OFFSET_X, 0),
        config.getDoubleOrElse(ViewConfig.PROP_MAP_BACKGROUND_OFFSET_Y, 0),
        config.getDoubleOrElse(ViewConfig.PROP_MAP_BACKGROUND_TRANSPARENCY, 1.0)
        );
  }

  private static PBounds getBoundingBox(ViewConfig config) {
    String bbs = config.getString(ViewConfig.PROP_MAP_BACKGROUND_BOUNDING_BOX);
    PBounds bounds = null;
    if (bbs != null) {
      String[] parts = bbs.split(",");
      if (parts.length != 4) {
        throw new IllegalArgumentException(
            "Bounding box must specify four floating point values separated by commas");
      }
      bounds = new PBounds(
          Double.parseDouble(parts[0]),
          Double.parseDouble(parts[1]),
          Double.parseDouble(parts[2]),
          Double.parseDouble(parts[3])
      );
    }
    return bounds;
  }

  public PImage createImageNode() {
    PImage node = new PImage(image);
    if (boundingBox != null) {
      node.setBounds(boundingBox);
    }
    node.setScale(scale);
    node.setOffset(offsetX, offsetY);
    node.setTransparency((float)transparency);
    return node;
  }

}
