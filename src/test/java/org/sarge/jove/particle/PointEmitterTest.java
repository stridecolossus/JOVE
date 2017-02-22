package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class PointEmitterTest {
	@Test
	public void emit() {
		final Point pos = new Point(1, 2, 3);
		final Emitter emitter = Emitter.point(pos);
		assertEquals(pos, emitter.emit());
	}

	@Test
	public void origin() {
		final Emitter emitter = Emitter.ORIGIN;
		assertEquals(Point.ORIGIN, emitter.emit());
	}
}
