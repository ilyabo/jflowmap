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
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolo.util.PStack;

/**
 * @author Ilya Boyandin
 */
public final class PPickPaths {

  private PPickPaths() {
  }

  @SuppressWarnings("unchecked")
  public static final <T extends PNode> T topmostNodeOfType(PPickPath pickPath, Class<T> klass) {
    PStack nodeStack = pickPath.getNodeStackReference();
    for (int i = nodeStack.size() - 1; i >= 0; i--) {
      Object node = nodeStack.get(i);
      if (node != null  &&  klass.isAssignableFrom(node.getClass())) {
        return (T) node;
      }
    }
    return null;
  }

}
