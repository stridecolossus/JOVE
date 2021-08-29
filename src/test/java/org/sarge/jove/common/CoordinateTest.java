package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate1D;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Coordinate.Coordinate3D;
import org.sarge.jove.common.Vertex.Layout;

class CoordinateTest {
	@Test
	void one() {
		final Coordinate1D one = new Coordinate1D(1);
		assertEquals(1, one.u());
		assertEquals(Layout.of(1, Float.class), one.layout());
		assertEquals(1 * Float.BYTES, one.length());
		assertEquals(true, one.equals(one));
		assertEquals(true, one.equals(new Coordinate1D(1)));
		assertEquals(false, one.equals(null));
		assertEquals(false, one.equals(new Coordinate1D(2)));
	}

	@Test
	void two() {
		final Coordinate2D two = new Coordinate2D(1, 2);
		assertEquals(1, two.u());
		assertEquals(2, two.v());
		assertEquals(Layout.of(2, Float.class), two.layout());
		assertEquals(2 * Float.BYTES, two.length());
		assertEquals(true, two.equals(two));
		assertEquals(true, two.equals(new Coordinate2D(1, 2)));
		assertEquals(false, two.equals(null));
		assertEquals(false, two.equals(new Coordinate2D(7, 8)));
	}

	@Test
	void three() {
		final Coordinate3D three = new Coordinate3D(1, 2, 3);
		assertEquals(1, three.u());
		assertEquals(2, three.v());
		assertEquals(3, three.w());
		assertEquals(Layout.of(3, Float.class), three.layout());
		assertEquals(3 * Float.BYTES, three.length());
		assertEquals(true, three.equals(three));
		assertEquals(true, three.equals(new Coordinate3D(1, 2, 3)));
		assertEquals(false, three.equals(null));
		assertEquals(false, three.equals(new Coordinate3D(7, 8, 9)));
	}

	@Test
	void array() {
		assertEquals(new Coordinate1D(1), Coordinate.of(new float[]{1}));
		assertEquals(new Coordinate2D(1, 2), Coordinate.of(new float[]{1, 2}));
		assertEquals(new Coordinate3D(1, 2, 3), Coordinate.of(new float[]{1, 2, 3}));
	}

	@Test
	void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> Coordinate.of(new float[]{}));
		assertThrows(IllegalArgumentException.class, () -> Coordinate.of(new float[]{1, 2, 3, 4}));
	}

	@Test
	void coordinates() {
		assertEquals(new Coordinate2D(0, 0), Coordinate2D.TOP_LEFT);
		assertEquals(new Coordinate2D(0, 1), Coordinate2D.BOTTOM_LEFT);
		assertEquals(new Coordinate2D(1, 0), Coordinate2D.TOP_RIGHT);
		assertEquals(new Coordinate2D(1, 1), Coordinate2D.BOTTOM_RIGHT);
	}

	@Test
	void layout() {
		assertEquals(Layout.of(1), Coordinate.layout(1));
		assertEquals(Layout.of(2), Coordinate.layout(2));
		assertEquals(Layout.of(3), Coordinate.layout(3));
	}

	@Test
	void layoutInvalidSize() {
		assertThrows(IllegalArgumentException.class, () -> Coordinate.layout(0));
		assertThrows(IllegalArgumentException.class, () -> Coordinate.layout(4));
	}
}
