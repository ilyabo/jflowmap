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

package jflowmap.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jflowmap.util.MathUtils;
import jflowmap.util.Pair;
import jflowmap.util.MathUtils.Rounding;

/**
 * @author Ilya Boyandin
 */
public class BoundSpinnerSliderModels<T extends Number> {

	private T initialValue;
	private T minimum;
	private T maximum;
	private T stepSize;
	private ValueMapping<T, Integer> valueMapping;
	private int sliderExtent = 0;

	/**
	 * @param valueMapping Spinner-to-slider value mapping
	 */
	public BoundSpinnerSliderModels(
			T value, T minimum, T maximum, T stepSize,
			ValueMapping<T, Integer> valueMapping) {
		this.initialValue = value;
		this.stepSize = stepSize;
		checkType(minimum);
		checkType(maximum);
		this.minimum = minimum;
		this.maximum = maximum;
		this.valueMapping = valueMapping;
	}
	
	public BoundSpinnerSliderModels<T> withSliderExtent(int extent) {
		this.sliderExtent = extent;
		return this;
	}

	@SuppressWarnings("unchecked")
	private void checkType(T number) {
		if (!(number instanceof Comparable)) {
			throw new IllegalArgumentException("Type must implement Comparable");
		}
	}
	
	public Pair<SpinnerModel,BoundedRangeModel> build() {
		@SuppressWarnings("unchecked")
		final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				initialValue, (Comparable<T>)(minimum), (Comparable<T>)(maximum), stepSize);
		final DefaultBoundedRangeModel sliderModel =
			new DefaultBoundedRangeModel(
					valueMapping.apply(initialValue, Rounding.ROUND), sliderExtent,

					// min integer value should be smaller than the smallest double (e.g. for log mapping) 
					valueMapping.apply(minimum, Rounding.FLOOR), 
					valueMapping.apply(maximum, Rounding.CEIL)
			);
		final AtomicBoolean lock = new AtomicBoolean(false);
		spinnerModel.addChangeListener(new ChangeListener() {
			@SuppressWarnings("unchecked")
			public void stateChanged(ChangeEvent e) {
				if (lock.compareAndSet(false, true)) {
					T value = (T)spinnerModel.getValue();
//					Comparable<T> min = (Comparable<T>)spinnerModel.getMinimum();
//					Comparable<T> max = (Comparable<T>)spinnerModel.getMaximum();
					int mappedValue;
//					if (min.equals(value)) {
//						mappedValue = sliderModel.getMinimum();
//					} else if (max.equals(value)) {
//						mappedValue = sliderModel.getMaximum();
//					} else {
						mappedValue = valueMapping.apply(value, Rounding.ROUND);
//					}
					sliderModel.setValue(mappedValue);
					lock.set(false);
				}
			}
		});
		sliderModel.addChangeListener(new ChangeListener() {
			@SuppressWarnings("unchecked")
			public void stateChanged(ChangeEvent e) {
				if (lock.compareAndSet(false, true)) {
					T value = valueMapping.reverse(sliderModel.getValue());
					Comparable<T> min = (Comparable<T>)spinnerModel.getMinimum();
					Comparable<T> max = (Comparable<T>)spinnerModel.getMaximum();
					if (min.compareTo(value) > 0) {
						value = (T) min;
					}
					if (max.compareTo(value) < 0) {
						value = (T) max;
					}
 					spinnerModel.setValue(value);
					lock.set(false);
				}
			}
		});
		return Pair.of((SpinnerModel)spinnerModel, (BoundedRangeModel)sliderModel);
	}
	
	public interface ValueMapping<S, T> {
		T apply(S s, Rounding mode);
		S reverse(T t);
	}
	
	public static final ValueMapping<Double, Integer> MAP_ID_DOUBLE =  
		new ValueMapping<Double, Integer>() {
			@Override
			public Integer apply(Double s, Rounding mode) {
				return (int) mode.round(s);
			}
			@Override
			public Double reverse(Integer t) {
				return Double.valueOf(t);
			}
		};
		
	public static final ValueMapping<Integer, Integer> MAP_ID_INTEGER =  
		new ValueMapping<Integer, Integer>() {
			@Override
			public Integer apply(Integer s, Rounding mode) {
				return s;
			}
			@Override
			public Integer reverse(Integer t) {
				return t;
			}
		};

		
	public static ValueMapping<Double, Integer> createLinearMapping(double alpha) {
		return createLinearMapping(0, alpha);
	}
	
	public static ValueMapping<Double, Integer> createLinearMapping(
			final double shift, final double alpha) {  
		return new ValueMapping<Double, Integer>() {
			@Override
			public Integer apply(Double s, Rounding mode) {
				return (int)mode.round(s * alpha - shift);
			}
			@Override
			public Double reverse(Integer t) {
				return shift + t / alpha;
			}
		};
	}

	public static ValueMapping<Double, Integer> createLogMapping(
			final double base, final double shift) {  
		return new ValueMapping<Double, Integer>() {
			@Override
			public Integer apply(Double s, Rounding mode) {
				return (int)mode.round(MathUtils.log(s + shift, base));
			}
			@Override
			public Double reverse(Integer t) {
				return Math.pow(base, t) - shift;
			}
		};
	}
}
