package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.util.Interpolator.StepInterpolator;
import org.sarge.jove.util.Interpolator.StepInterpolator.Entry;

public class InterpolatorTest {
	@Test
	public void linear() {
		assertFloatEquals(42, Interpolator.LINEAR.interpolate(42));
	}

	@Test
	public void range() {
		final Interpolator range = Interpolator.range(3, 5, Interpolator.LINEAR);
		assertFloatEquals(3, range.interpolate(0));
		assertFloatEquals(4, range.interpolate(0.5f));
		assertFloatEquals(5, range.interpolate(1));
	}

	@Test
	public void sine() {
		assertFloatEquals(0, Interpolator.SINE.interpolate(0));
		assertFloatEquals(0.5f, Interpolator.SINE.interpolate(0.5f));
		assertFloatEquals(1, Interpolator.SINE.interpolate(1));
	}

	@Test
	public void step() {
		final Interpolator step = new StepInterpolator(List.of(new Entry(0.5f, 1), new Entry(0, 0)));
		assertFloatEquals(0, step.interpolate(0));
		assertFloatEquals(1, step.interpolate(0.5f));
		assertFloatEquals(1, step.interpolate(1));
	}

	@Test
	public void stepEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new StepInterpolator(List.of()));
	}
}
