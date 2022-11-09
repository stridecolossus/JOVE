package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.Trigonometric.*;

import org.junit.jupiter.api.Test;

class TrigonometricTest {
	@Test
	void degrees() {
		assertEquals(0,		toDegrees(0));
		assertEquals(90,  	toDegrees(HALF_PI));
		assertEquals(180, 	toDegrees(PI));
		assertEquals(360, 	toDegrees(TWO_PI));
	}

	@Test
	void radians() {
		assertEquals(0, 		toRadians(0));
		assertEquals(HALF_PI, 	toRadians(90));
		assertEquals(PI, 		toRadians(180));
		assertEquals(TWO_PI, 	toRadians(360));
	}
}
