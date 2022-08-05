package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkResult;

public class VulkanExceptionTest {
	private VulkanException exception;

	@BeforeEach
	void before() {
		exception = new VulkanException(VkResult.ERROR_INCOMPATIBLE_DRIVER);
	}

	@Test
	void constructor() {
		assertEquals(VkResult.ERROR_INCOMPATIBLE_DRIVER.value(), exception.result());
		assertEquals("ERROR_INCOMPATIBLE_DRIVER[-9]", exception.getMessage());
	}

	@Test
	void code() {
		final VulkanException code = new VulkanException(-9);
		assertEquals(exception.result(), code.result());
	}
}
