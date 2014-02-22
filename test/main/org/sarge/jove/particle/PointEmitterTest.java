package org.sarge.jove.particle;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sarge.jove.geometry.Point;

public class PointEmitterTest {
	@Test
	public void emit() {
		final PointEmitter emitter = new PointEmitter();
		assertEquals( Point.ORIGIN, emitter.emit() );
	}
}
