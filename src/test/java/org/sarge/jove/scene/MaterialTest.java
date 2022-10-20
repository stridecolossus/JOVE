package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class MaterialTest {
	@Test
	void none() {
		assertThrows(IllegalStateException.class, () -> Material.NONE.queue());
	}
}
