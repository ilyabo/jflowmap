package jflowmap.ui;

import static org.junit.Assert.assertEquals;

import javax.swing.BoundedRangeModel;
import javax.swing.SpinnerModel;

import jflowmap.util.Pair;

import org.junit.Test;

public class BoundSpinnerSliderModelsTest {

	@Test
	public void testIdMapping() {
		Pair<SpinnerModel, BoundedRangeModel> models =
			new BoundSpinnerSliderModels<Integer>(
					50, 0, 100, 1, BoundSpinnerSliderModels.MAP_ID_INTEGER
			).build();
		models.first().setValue(42);
		assertEquals(42, models.second().getValue());

		models.second().setValue(24);
		assertEquals(24, models.first().getValue());
	}
	
	@Test
	public void testLinearMapping() {
		Pair<SpinnerModel, BoundedRangeModel> models =
			new BoundSpinnerSliderModels<Double>(
					0.5, 0.0, 1.0, 0.1, BoundSpinnerSliderModels.createLinearMapping(100)
			).build();
		models.first().setValue(.75);
		assertEquals(75, models.second().getValue());
		
		models.second().setValue(25);
		assertEquals(0.25, models.first().getValue());
	}
	
	@Test
	public void testLogMapping() {
		Pair<SpinnerModel, BoundedRangeModel> models =
			new BoundSpinnerSliderModels<Double>(
					0.0, 0.0, 1000.0, 1.0, BoundSpinnerSliderModels.createLogMapping(2, 1)
			).build();
		models.first().setValue(3.0);
		assertEquals(2, models.second().getValue());
		
		models.first().setValue(15.0);
		assertEquals(4, models.second().getValue());

		models.second().setValue(8);
		assertEquals(255.0, (Double)models.first().getValue(), 1e-7);
	}

	@Test(expected = ClassCastException.class)
	public void testWrongType() {
		Pair<SpinnerModel, BoundedRangeModel> models =
			new BoundSpinnerSliderModels<Double>(
					0.5, 0.0, 1.0, 0.1, BoundSpinnerSliderModels.createLinearMapping(100)
			).build();
		models.first().setValue((Integer)1);  // should be double
//	 	assertEquals(1, models.second().getValue());
	}

}
