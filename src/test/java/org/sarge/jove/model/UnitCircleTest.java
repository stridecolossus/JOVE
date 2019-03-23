package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.MathsUtil;

public class UnitCircleTest {
	@Test
	public void segment() {
		final var segment = UnitCircle.segment(3, 4, 0f, MathsUtil.toRadians(90));
		final float angle = 4 * MathsUtil.sin(MathsUtil.toRadians(45));
		final Point top = new Point(0, 4, 0);
		final Point middle = new Point(angle, angle, 0);
		final Point right = new Point(4, 0, 0);
		assertEquals(List.of(top, middle, right), segment);
	}

	@Test
	public void circle() {
		final var circle = UnitCircle.circle(5, 4);
		final Point top = new Point(0, 4, 0);
		final Point right = new Point(4, 0, 0);
		final Point bottom = new Point(0, -4, 0);
		final Point left = new Point(-4, 0, 0);
		assertEquals(List.of(top, right, bottom, left, top), circle);
	}
}
