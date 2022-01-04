package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.IntegerEnumeration;

public class VulkanLibraryTest {
	@Test
	void version() {
		assertEquals("1.1.0", VulkanLibrary.VERSION.toString());
	}

	@Test
	void checkSuccess() {
		VulkanLibrary.check(VulkanLibrary.SUCCESS);
	}

	@Test
	void checkThrows() {
		assertThrows(VulkanException.class, () -> VulkanLibrary.check(VkResult.ERROR_DEVICE_LOST.value()));
	}

	@Test
	void checkThrowsUnknownErrorCode() {
		assertThrows(VulkanException.class, () -> VulkanLibrary.check(-1));
	}

	@Test
	void mapper() {
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(IntegerEnumeration.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(VulkanBoolean.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(Handle.class));
	}

	@Test
	void create() {
		final VulkanLibrary api = VulkanLibrary.create();
		assertNotNull(api);
	}
}
