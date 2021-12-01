package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkResult;

public class VulkanExceptionTest {
	@SuppressWarnings("static-method")
	@Test
	void exception() {
		final int code = VkResult.ERROR_INCOMPATIBLE_DRIVER.value();
		final VulkanException e = new VulkanException(code);
		assertEquals(code, e.result);
		assertEquals("ERROR_INCOMPATIBLE_DRIVER[-9]", e.getMessage());
	}
}
