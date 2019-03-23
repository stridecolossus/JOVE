package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;

public class ExtentsPositionFactoryTest {
	private PositionFactory factory;

	@BeforeEach
	public void before() {
		factory = new ExtentsPositionFactory(new Extents(new Point(1, 2, 3), new Point(4, 5, 6)));
	}

	@Test
	public void generate() {
		final Point pos = factory.position();
		assertNotNull(pos);
	}
}
