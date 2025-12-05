package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkResult;

class VulkanExceptionTest {
	@Test
	void exception() {
		final VulkanException exception = new VulkanException(VkResult.VK_ERROR_INCOMPATIBLE_DRIVER);
		assertEquals("VK_ERROR_INCOMPATIBLE_DRIVER[-9]", exception.getMessage());
	}
}
