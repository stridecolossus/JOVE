package org.sarge.jove.geometry;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Coordinate.Coordinate1D;
import org.sarge.jove.geometry.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Coordinate.Coordinate3D;

public class TextureCoordinateTest {
	@Test
	void one() {
		final Coordinate one = new Coordinate1D(4);
		assertNotNull(one);
		assertEquals(1 * Float.BYTES, one.length());
		assertArrayEquals(new float[]{4}, one.toArray());
		assertEquals(true, one.equals(one));
		assertEquals(false, one.equals(null));
		assertEquals(false, one.equals(new Coordinate1D(7)));
		assertEquals(false, one.equals(new Coordinate2D(7, 8)));
		assertEquals(false, one.equals(new Coordinate3D(7, 8, 9)));
		assertEquals(one, Coordinate.of(new float[]{4}));
	}

	@Test
	void two() {
		final Coordinate two = new Coordinate2D(4, 5);
		assertNotNull(two);
		assertEquals(2, Coordinate2D.SIZE);
		assertEquals(2 * Float.BYTES, two.length());
		assertArrayEquals(new float[]{4, 5}, two.toArray());
		assertEquals(true, two.equals(two));
		assertEquals(false, two.equals(null));
		assertEquals(false, two.equals(new Coordinate1D(7)));
		assertEquals(false, two.equals(new Coordinate2D(7, 8)));
		assertEquals(false, two.equals(new Coordinate3D(7, 8, 9)));
		assertEquals(two, Coordinate.of(new float[]{4, 5}));
	}

	@Test
	void three() {
		final Coordinate three = new Coordinate3D(4, 5, 6);
		assertNotNull(three);
		assertEquals(3 * Float.BYTES, three.length());
		assertArrayEquals(new float[]{4, 5, 6}, three.toArray());
		assertEquals(true, three.equals(three));
		assertEquals(false, three.equals(null));
		assertEquals(false, three.equals(new Coordinate1D(7)));
		assertEquals(false, three.equals(new Coordinate2D(7, 8)));
		assertEquals(false, three.equals(new Coordinate3D(7, 8, 9)));
		assertEquals(three, Coordinate.of(new float[]{4, 5, 6}));
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
	void quad() {
		final var expected = List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT);
		assertEquals(expected, Coordinate2D.QUAD);
	}
}
