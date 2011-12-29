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

package jflowmap.util.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Ilya Boyandin
 */
public abstract class PTypedBasicInputEventHandler<T extends PNode> extends PBasicInputEventHandler {
  private final Class<T> nodeClass;

  public PTypedBasicInputEventHandler(Class<T> nodeClass) {
    this.nodeClass = nodeClass;
  }

  protected T node(PInputEvent event) {
    return PPickPaths.topmostNodeOfType(event.getPath(), nodeClass);
  }

  @Override
  public boolean acceptsEvent(PInputEvent event, int type) {
    return super.acceptsEvent(event, type)  &&  node(event) != null;
  }

}
