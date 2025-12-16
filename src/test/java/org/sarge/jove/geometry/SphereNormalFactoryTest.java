package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.SphereNormalFactory.DefaultSphereNormalFactory;
import org.sarge.jove.util.MathsUtility;

class SphereNormalFactoryTest {
	private SphereNormalFactory factory;

	@BeforeEach
	void before() {
		factory = new DefaultSphereNormalFactory();
	}

	@Test
	void normal() {
		assertEquals(Axis.X, factory.normal(0, 0));
	}

	@Test
	void inverse() {
		assertEquals(Axis.X.invert(), factory.normal(MathsUtility.PI, 0));
	}

	@Test
	void poles() {
		assertEquals(Axis.Y, factory.normal(0, MathsUtility.HALF_PI));
		assertEquals(Axis.Y.invert(), factory.normal(0, -MathsUtility.HALF_PI));
	}

	@Test
	void rotate() {
		assertEquals(Axis.Z.invert(), factory.rotate().normal(0, 0));
	}
}
