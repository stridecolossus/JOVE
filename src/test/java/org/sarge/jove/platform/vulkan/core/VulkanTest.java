package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.VulkanUtility;

class VulkanTest {
	@Test
	void create() {
		Vulkan.create();
	}

	@Test
	void alignment() {
		VulkanUtility.checkAlignment(0);
		VulkanUtility.checkAlignment(4);
		VulkanUtility.checkAlignment(8);
		assertThrows(IllegalArgumentException.class, () -> VulkanUtility.checkAlignment(1));
		assertThrows(IllegalArgumentException.class, () -> VulkanUtility.checkAlignment(2));
		assertThrows(IllegalArgumentException.class, () -> VulkanUtility.checkAlignment(3));
	}
}
