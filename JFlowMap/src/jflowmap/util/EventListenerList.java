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

package jflowmap.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

/**
 * Example usage:
 *
 * <code><pre>
 * EventListenerList<PropertyChangeListener> list =
 *   new EventListenerList<PropertyChangeListener>(PropertyChangeListener.class);
 *
 * list.addListener(new PropertyChangeListener() {
 *  public void propertyChange(PropertyChangeEvent evt) {
 *  }
 * });
 *
 * list.fire().propertyChange(new PropertyChangeEvent("bebe", "name", null, null));
 * </pre></code>
 *
 * See also http://skavish.livejournal.com/189435.html
 *
 * @author Dmitry Skavish
 */
public class EventListenerList<L> extends ArrayList<L> implements InvocationHandler {

  private static final long serialVersionUID = 5260566399219798770L;
  private final Class<? extends L> listenerInterface;
  private final L proxy;

  @SuppressWarnings("unchecked")
  public EventListenerList(Class<? extends L> listenerInterface) {
    this.listenerInterface = listenerInterface;
    proxy = (L) Proxy.newProxyInstance(listenerInterface.getClassLoader(),
           new Class<?>[]{listenerInterface}, this);
  }

  public static <L> EventListenerList<L> createFor(Class<? extends L> klass) {
    return new EventListenerList<L>(klass);
  }

  public void addListener(L l) {
    synchronized (proxy) {
      if (!contains(l)) {
        add(l);
      }
    }
  }

  public boolean removeListener(L l) {
    synchronized (proxy) {
      return remove(l);
    }
  }

  @SuppressWarnings("unchecked")
  public L[] getListeners() {
    synchronized (proxy) {
      return toArray((L[]) Array.newInstance(listenerInterface, size()));
    }
  }

  public L fire() {
    return proxy;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object[] objects;
    synchronized (this.proxy) {
      objects = toArray(new Object[size()]);
    }
    for (Object l : objects) {
      method.setAccessible(true);   // improves the invoke performance
      method.invoke(l, args);
    }
    return null;
  }
}
