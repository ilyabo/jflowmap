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

import prefuse.data.Node;

/**
 * @author Ilya Boyandin
 */
public enum FlowDirection {

  INCOMING(FlowEndpoint.DEST) {
    @Override
    public int degreeOf(Node node) {
      return node.getInDegree();
    }
  },

  OUTGOING(FlowEndpoint.ORIGIN) {
    @Override
    public int degreeOf(Node node) {
      return node.getOutDegree();
    }
  };

  public final FlowEndpoint thisEndpoint;

  private FlowDirection(FlowEndpoint endpoint) {
    this.thisEndpoint = endpoint;
  }

  public abstract int degreeOf(Node node);

}
