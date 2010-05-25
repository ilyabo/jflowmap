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

package jflowmap.models.map;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jflowmap.data.XmlAreaMapModelReader;

import org.apache.log4j.Logger;


/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class AreaMap {

  private static Logger logger = Logger.getLogger(AreaMap.class);

  private final String name;
  private final List<Area> areas;

  public AreaMap(String name, List<Area> areas) {
    this.name = name;
    this.areas = areas;
  }

  public String getName() {
    return name;
  }

  public Collection<Area> getAreas() {
    return Collections.unmodifiableCollection(areas);
  }

  public static final AreaMap load(String filename) throws IOException {
    logger.info("Loading area map \"" + filename + "\"");
    return XmlAreaMapModelReader.readMap(filename);
  }
}
