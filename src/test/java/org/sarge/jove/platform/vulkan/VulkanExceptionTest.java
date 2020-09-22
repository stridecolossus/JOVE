package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class VulkanExceptionTest {
	@Test
	void exception() {
		final int code = VkResult.VK_ERROR_INCOMPATIBLE_DRIVER.value();
		final VulkanException e = new VulkanException(code, "message");
		assertEquals(code, e.result);
		assertEquals("message: [-9] VK_ERROR_INCOMPATIBLE_DRIVER", e.getMessage());
	}
}
