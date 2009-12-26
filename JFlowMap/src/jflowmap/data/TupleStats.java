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

package jflowmap.data;

import java.util.Iterator;


import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;

/**
 * @author Ilya Boyandin
 */
public class TupleStats {
    
    private TupleStats() {
    }

    public static MinMax createFor(TupleSet tupleSet, final String attrName) {
        return MinMax.createFor(iteratorFor(tupleSet, attrName));
    }
    
    @SuppressWarnings("unchecked")
    static Iterator<Double> iteratorFor(TupleSet tupleSet, final String attrName) {
        return Iterators.transform(
                tupleSet.tuples(), 
                new Function<Tuple, Double>() {
                    public Double apply(Tuple from) {
                        return from.getDouble(attrName);
                    }
                }
        );
    }

}
