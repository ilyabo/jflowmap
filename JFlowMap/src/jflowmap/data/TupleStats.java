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

package jflowmap.data;

import java.util.Iterator;
import java.util.List;

import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Ilya Boyandin
 */
public class TupleStats {

  private TupleStats() {
  }

  public static MinMax createFor(TupleSet tupleSet, Iterable<String> attrNames) {
    return MinMax.createFor(attrValuesIterator(tupleSet, attrNames));
  }

  public static MinMax createFor(TupleSet tupleSet, String attrName) {
    return MinMax.createFor(attrValuesIterator(tupleSet, attrName));
  }


  public static MinMax createFor(Iterator<TupleSet> tupleSetIt, Iterator<String> attrNameIt) {
    List<Iterator<Double>> iterators = Lists.newArrayList();
    while (tupleSetIt.hasNext()) {
      assert attrNameIt.hasNext();
      iterators.add(attrValuesIterator(tupleSetIt.next(), attrNameIt.next()));
    }
    assert !attrNameIt.hasNext();
    return MinMax.createFor(Iterators.concat(iterators.iterator()));
  }

  @SuppressWarnings("unchecked")
  private static Iterator<Double> attrValuesIterator(TupleSet tupleSet, final String attrName) {
    return Iterators.transform(
        tupleSet.tuples(),
        new Function<Tuple, Double>() {
          public Double apply(Tuple from) {
            return from.getDouble(attrName);
          }
        }
    );
  };

  /**
   * Returns a concatenated iterator over tuple entries' values of the given set of attrNames
   */
  private static Iterator<Double> attrValuesIterator(TupleSet tupleSet, Iterable<String> attrNames) {
    List<Iterator<Double>> attrValueIterators = Lists.newArrayList();
    for (String attrName : attrNames) {
      attrValueIterators.add(attrValuesIterator(tupleSet, attrName));
    }
    return Iterators.concat(attrValueIterators.iterator());
  };

}
