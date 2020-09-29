package org.sarge.jove.platform.vulkan.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.INTEGRATION_TEST;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkResult;

public class VulkanLibraryTest {
	@Test
	void version() {
		assertEquals("1.0.2", VulkanLibrary.VERSION.toString());
	}

	@Test
	void checkSuccess() {
		VulkanLibrary.check(VulkanLibrary.SUCCESS);
	}

	@Test
	void checkThrows() {
		assertThrows(ServiceException.class, () -> VulkanLibrary.check(VkResult.VK_ERROR_DEVICE_LOST.value()));
	}

	@Test
	void checkThrowsUnknownErrorCode() {
		assertThrows(ServiceException.class, () -> VulkanLibrary.check(-1));
	}

	@Tag(INTEGRATION_TEST)
	@Test
	void create() {
		final VulkanLibrary api = VulkanLibrary.create();
		assertNotNull(api);
		assertNotNull(api.factory());
	}
}
