package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;

public class QuadTest {
	@Test
	void coordinates() {
		assertEquals(List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT), Quad.COORDINATES);
	}

	@Test
	void constructor() {
		assertNotNull(Quad.STRIP);
	}

	@DisplayName("Quad indices should be counter-clockwise by default")
	@Test
	void indices() {
		final IntStream indices = Quad.indices(false);
		assertNotNull(indices);
		assertArrayEquals(new int[]{0, 1, 3, 2}, indices.toArray());
	}

	@DisplayName("Quad indices can be clockwise")
	@Test
	void clockwise() {
		final IntStream indices = Quad.indices(true);
		assertNotNull(indices);
		assertArrayEquals(new int[]{0, 2, 3, 1}, indices.toArray());
	}

	@DisplayName("A strip of quads should share the last two indices of each quad")
	@Test
	void strip() {
		final int[] expected = {
				0, 1, 3, 2,
				2, 3, 5, 4,
		};
		final IntStream index = Quad.STRIP.index(2, false);
		assertNotNull(index);
		assertEquals(Arrays.toString(expected), Arrays.toString(index.toArray()));
	}

	@DisplayName("A quad strip can be clockwise")
	@Test
	void stripClockwise() {
		final int[] expected = {
				0, 2, 3, 1,
				2, 4, 5, 3,
		};
		final IntStream index = Quad.STRIP.index(2, true);
		assertNotNull(index);
		assertEquals(Arrays.toString(expected), Arrays.toString(index.toArray()));
	}
}
