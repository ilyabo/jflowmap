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

  @SuppressWarnings("unchecked")
  public static SeqStat createFor(final TupleSet tupleSet, Iterable<String> attrNames) {
    Iterable it = new Iterable<Tuple>() {@Override
      public Iterator<Tuple> iterator() {
        return tupleSet.tuples();
      } };
    return createFor(it, attrNames);
  }

  @SuppressWarnings("unchecked")
  public static SeqStat createFor(TupleSet tupleSet, String attrName) {
    return SeqStat.createFor(attrValuesIterator(tupleSet.tuples(), attrName));
  }

  public static SeqStat createFor(Iterable<Tuple> edges, Iterable<String> attrNames) {
    return SeqStat.createFor(attrValuesIterator(edges, attrNames));
  };


  @SuppressWarnings("unchecked")
  public static SeqStat createFor(Iterator<TupleSet> it, Iterator<String> attrNameIt) {
    List<Iterator<Double>> iterators = Lists.newArrayList();
    while (it.hasNext()) {
      assert attrNameIt.hasNext();
      iterators.add(attrValuesIterator(it.next().tuples(), attrNameIt.next()));
    }
    assert !attrNameIt.hasNext();
    return SeqStat.createFor(Iterators.concat(iterators.iterator()));
  }

  private static Iterator<Double> attrValuesIterator(Iterator<Tuple> it, final String attrName) {
    return Iterators.transform(
        it,
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
  @SuppressWarnings("unchecked")
  private static Iterator<Double> attrValuesIterator(Iterable<Tuple> it, Iterable<String> attrNames) {
    List<Iterator<Double>> attrValueIterators = Lists.newArrayList();
    for (String attrName : attrNames) {
      attrValueIterators.add(attrValuesIterator(it.iterator(), attrName));
    }
    return Iterators.concat(attrValueIterators.iterator());
  }

}
