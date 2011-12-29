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

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;

/**
 * @author Ilya Boyandin
 */
public class ArrayUtils {

  private ArrayUtils() {
  }

  /**
   * NOTE: This method modifies the input array
   */
  public static int[] reverse(int[] a) {
    for (int left = 0, right = a.length - 1; left < right; left++, right--) {
      // exchange the first and last
      int _ = a[left];
      a[left] = a[right];
      a[right] = _;
    }
    return a;
  }

  /**
   * NOTE: This method modifies the input array
   */
  public static <T> T[] reverse(T[] a) {
    for (int left = 0, right = a.length - 1; left < right; left++, right--) {
      // exchange the first and last
      T _ = a[left];
      a[left] = a[right];
      a[right] = _;
    }
    return a;
  }

  public static double[] toArrayOfPrimitives(Iterable<Double> data) {
    if (data instanceof Collection) {
      return Doubles.toArray((Collection<Double>)data);
    } else {
      return Doubles.toArray(ImmutableList.copyOf(data));
    }
  }

}
