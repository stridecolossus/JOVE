package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.Z;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.WindingOrder;

class WindingOrderTest {
	@Test
	void counter() {
		assertEquals(WindingOrder.COUNTER_CLOCKWISE, WindingOrder.of(Z.dot(Z)));
	}

	@Test
	void clockwise() {
		assertEquals(WindingOrder.CLOCKWISE, WindingOrder.of(Z.dot(Z.invert())));
	}

	@Test
	void colinear() {
		assertEquals(WindingOrder.COLINEAR, WindingOrder.of(0));
	}
}
