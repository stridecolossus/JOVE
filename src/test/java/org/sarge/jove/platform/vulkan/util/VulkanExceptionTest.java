package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkResult;

class VulkanExceptionTest {
	@Test
	void exception() {
		final VulkanException exception = new VulkanException(VkResult.ERROR_INCOMPATIBLE_DRIVER);
		assertEquals("ERROR_INCOMPATIBLE_DRIVER[-9]", exception.getMessage());
	}
}
