package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CosineTest {
	@Test
	void quadrant() {
		assertEquals(new Cosine(-1, 0), Cosine.quadrant(-1));
		assertEquals(new Cosine(0, +1), Cosine.quadrant(0));
		assertEquals(new Cosine(+1, 0), Cosine.quadrant(1));
		assertEquals(new Cosine(0, -1), Cosine.quadrant(2));
		assertEquals(new Cosine(-1, 0), Cosine.quadrant(3));
		assertEquals(new Cosine(0, +1), Cosine.quadrant(4));
	}

	// TODO - provider
}
