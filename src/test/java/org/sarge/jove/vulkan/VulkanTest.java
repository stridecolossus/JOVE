package org.sarge.jove.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class VulkanTest {
	@Test
	void vulkan() {
		final Vulkan vulkan = Vulkan.vulkan();
		assertNotNull(vulkan);
		assertEquals(vulkan, Vulkan.vulkan());
	}
}
