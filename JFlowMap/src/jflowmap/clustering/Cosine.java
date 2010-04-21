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

package jflowmap.clustering;

/**
 * @author Ilya Boyandin
 */
public class Cosine {

    private Cosine() {
    }

    public static double cosine(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException();
        }
        double c = dotProduct(v1, v2) / (length(v1) * length(v2));
        assert(-1.0 <= c  &&  c <= 1.0);
        return c;
    }

    static double dotProduct(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException();
        }
        double sum = 0;
        for (int i = 0, length = v1.length; i < length; i++) {
            sum += v1[i] * v2[i];
        }
        return sum;
    }

    static double length(double[] v) {
        double sum = 0;
        for (int i = 0, length = v.length; i < length; i++) {
            sum += v[i] * v[i];
        }
        return Math.sqrt(sum);
    }

}
