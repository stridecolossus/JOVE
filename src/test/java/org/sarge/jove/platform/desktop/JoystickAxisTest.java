package org.sarge.jove.platform.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JoystickAxisTest {
	private JoystickAxis axis;

	@BeforeEach
	void before() {
		axis = new JoystickAxis(1, 2);
	}

	@Test
	void constructor() {
		assertEquals(2, axis.value());
	}

	@Test
	void update() {
		axis.update(3);
		assertEquals(3, axis.value());
	}
}
