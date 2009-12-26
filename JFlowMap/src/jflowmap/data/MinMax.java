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

/**
 * @author Ilya Boyandin
 */
public class MinMax {
    private final double min;
    private final double max;
	private final double avg;
    private final double minLog;
    private final double maxLog;

    private MinMax(double minValue, double avg, double maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue > maxValue");
        }
        if (avg > maxValue) {
            throw new IllegalArgumentException("avg > maxValue");
        }
        if (avg < minValue) {
            throw new IllegalArgumentException("avg < minValue");
        }
        this.avg = avg;
        this.min = minValue;
        this.max = maxValue;
        this.minLog = Math.log(min);
        this.maxLog = Math.log(max);
    }
    
    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getAvg() {
        return avg;
    }

    public double getMaxLog() {
        return maxLog;
    }

    public double getMinLog() {
        return minLog;
    }
    
    public static MinMax createFor(Iterator<Double> it) {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double sum = 0;
        int count = 0;

        while (it.hasNext()) {
            double v = (Double)it.next();
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
            sum += v;
            count++;
        }
        return new MinMax(min, sum / count, max);
    }

    /**
     * Returns a normalized value between 0 and 1 for the dataset
     * the min and max were calculated for.
     * In case if max == min the method always returns 1. 
     */
    public double normalize(double value) {
        if (getMax() == getMin()) return 1.0;
        return (value - getMin()) / (getMax() - getMin());
    }

    /**
     * Returns a normalized log(value) between 0 and 1.
     * In case if max == min the method always returns 1. 
     */
    public double normalizeLog(double value) {
        if (getMax() == getMin()) return 1.0;
        return (Math.log(value) - getMinLog()) / (getMaxLog() - getMinLog());
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    public String toString()
    {
        final String TAB = "    ";
        
        String retValue = "";
        
        retValue = "MinMax ( "
            + super.toString() + TAB
            + "min = " + this.min + TAB
            + "max = " + this.max + TAB
            + "avg = " + this.avg + TAB
            + "minLog = " + this.minLog + TAB
            + "maxLog = " + this.maxLog + TAB
            + " )";
    
        return retValue;
    }
   

}