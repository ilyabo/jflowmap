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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class CollectionUtils {

  private CollectionUtils() {
  }

  /**
   * Note: This method creates an in-memory copy of the elements.
   * This can be inefficient for large lists.
   */
  public static <T> Iterable<T> reverse(Iterable<T> it) {
    return Iterables.reverse(ImmutableList.copyOf(it));
  }

  /**
   * Note: This method creates an in-memory copy of the elements.
   * This can be inefficient for large lists.
   */
  public static <T> Iterator<T> reverse(Iterator<T> it) {
    return Iterables.reverse(ImmutableList.copyOf(it)).iterator();
  }

  public static <T> Iterable<T> sort(Iterable<T> items, Comparator<T> comp) {
    List<T> list = Lists.newArrayList(items);
    Collections.sort(list, comp);
    return list;
  }

  /**
   * @param pattern Regular expression
   */
  public static Iterable<String> filterByPattern(Iterable<String> items, final String pattern) {
    return Iterables.filter(items, new Predicate<String>() {
      Pattern p = Pattern.compile(pattern);
      @Override
      public boolean apply(String attr) {
        return p.matcher(attr).matches();
      }
    });
  }


  private static final Predicate<Double> PREDICATE_NON_NAN = new Predicate<Double>() {
    @Override
    public boolean apply(Double val) {
      return !val.isNaN();
    }
  };

  public static Iterable<Double> removeNaNs(Iterable<Double> values) {
    return Iterables.filter(values, PREDICATE_NON_NAN);
  }

}
