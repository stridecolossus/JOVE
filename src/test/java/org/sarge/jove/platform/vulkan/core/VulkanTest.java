package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix;

class VulkanTest {
	@Test
	void create() {
		Vulkan.create();
	}

	@Test
	void alignment() {
		Vulkan.checkAlignment(0);
		Vulkan.checkAlignment(4);
		Vulkan.checkAlignment(8);
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(1));
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(2));
		assertThrows(IllegalArgumentException.class, () -> Vulkan.checkAlignment(3));
	}

	@Test
	void matrix() {
		final var expected = new Matrix.Builder()
				.identity()
				.set(1, 1, -1)
				.set(2, 2, -1)
				.build();

		assertEquals(expected, Vulkan.matrix());
	}
}
