package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.TextureCoordinate;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate1D;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate3D;

public class TextureCoordinateTest {
	@Test
	public void one() {
		final TextureCoordinate one = new Coordinate1D(4);
		assertNotNull(one);
		assertFloatArrayEquals(new float[]{4}, one.toArray());
		assertEquals(true, one.equals(one));
		assertEquals(false, one.equals(null));
		assertEquals(false, one.equals(new Coordinate1D(7)));
		assertEquals(false, one.equals(new Coordinate2D(7, 8)));
		assertEquals(false, one.equals(new Coordinate3D(7, 8, 9)));
	}

	@Test
	public void two() {
		final TextureCoordinate two = new Coordinate2D(4, 5);
		assertNotNull(two);
		assertFloatArrayEquals(new float[]{4, 5}, two.toArray());
		assertEquals(true, two.equals(two));
		assertEquals(false, two.equals(null));
		assertEquals(false, two.equals(new Coordinate1D(7)));
		assertEquals(false, two.equals(new Coordinate2D(7, 8)));
		assertEquals(false, two.equals(new Coordinate3D(7, 8, 9)));
	}

	@Test
	public void three() {
		final TextureCoordinate three = new Coordinate3D(4,5, 6);
		assertNotNull(three);
		assertFloatArrayEquals(new float[]{4, 5, 6}, three.toArray());
		assertEquals(true, three.equals(three));
		assertEquals(false, three.equals(null));
		assertEquals(false, three.equals(new Coordinate1D(7)));
		assertEquals(false, three.equals(new Coordinate2D(7, 8)));
		assertEquals(false, three.equals(new Coordinate3D(7, 8, 9)));
	}

	@Test
	public void arrayInvalidLength() {
		assertThrows(IllegalArgumentException.class, () -> TextureCoordinate.of(new float[]{}));
		assertThrows(IllegalArgumentException.class, () -> TextureCoordinate.of(new float[]{1, 2, 3, 4}));
	}

	@Test
	public void coordinates() {
		assertEquals(new Coordinate2D(0, 0), Coordinate2D.TOP_LEFT);
		assertEquals(new Coordinate2D(0, 1), Coordinate2D.BOTTOM_LEFT);
		assertEquals(new Coordinate2D(1, 0), Coordinate2D.TOP_RIGHT);
		assertEquals(new Coordinate2D(1, 1), Coordinate2D.BOTTOM_RIGHT);
	}

	@Test
	public void quad() {
		final var expected = List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT);
		assertEquals(expected, Coordinate2D.QUAD);
	}
}
