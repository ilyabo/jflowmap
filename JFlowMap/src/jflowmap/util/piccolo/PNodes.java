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

import java.util.Iterator;

import com.google.common.collect.Iterables;

import edu.umd.cs.piccolo.PNode;

/**
 * @author Ilya Boyandin
 */
public class PNodes {

    private PNodes() {
    }

    @SuppressWarnings("unchecked")
    public static final <T extends PNode> T getAncestorOfType(PNode node, Class<T> klass) {
        PNode parent = node;
        while (parent != null) {
            if (parent != null  &&  klass.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public static final Iterable<PNode> childrenOf(final PNode node) {
            return new Iterable<PNode>() {
                @Override
    //            @SuppressWarnings("unchecked")
                public Iterator<PNode> iterator() {
    //                return node.getChildrenIterator();
                    return new Iterator<PNode>() {          // implement an iterator to avoid ConcurrentModificationException
                        int nextPos = 0;
                        @Override
                        public boolean hasNext() {
                            return (nextPos < node.getChildrenCount());
                        }
    
                        @Override
                        public PNode next() {
                            return node.getChild(nextPos++);
                        }
    
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }

    public static final <T extends PNode> Iterable<T> childrenOfType(PNode node, Class<T> type) {
        return Iterables.filter(childrenOf(node), type);
    }

    public static final PNode moveTo(PNode node, double x, double y) {
        node.offset(x - node.getX(), y - node.getY());
        return node;
    }

    public static final int indexOfChild(PNode parent, PNode child) {
        for (int i = 0, numChildren = parent.getChildrenCount(); i < numChildren; i++) {
            if (parent.getChild(i) == child) {
                return i;
            }
        }
        return -1;
    }



}
