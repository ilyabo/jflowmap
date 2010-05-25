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

/**
 * @author Ilya Boyandin
 *     Date: 25-Sep-2009
 */
public class Area {

  private final String id;
  private final String name;
  private final Polygon[] polygons;

  public Area(String id, String name, Polygon[] polygons) {
    this.id = id;
    this.name = name;
    this.polygons = polygons.clone();
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Polygon[] getPolygons() {
    return polygons.clone();
  }
}
