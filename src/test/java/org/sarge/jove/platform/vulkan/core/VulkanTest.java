package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VulkanTest {
	@Test
	void create() {
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
}
