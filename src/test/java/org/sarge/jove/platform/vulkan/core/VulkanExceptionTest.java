package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.core.VulkanException;

class VulkanExceptionTest {
	@Test
	void exception() {
		final VulkanException exception = new VulkanException(VkResult.VK_ERROR_INCOMPATIBLE_DRIVER);
		assertEquals("VK_ERROR_INCOMPATIBLE_DRIVER[-9]", exception.getMessage());
	}
}
