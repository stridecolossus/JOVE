package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.ptr.IntByReference;

public class ExtensionTest {
	@Test
	void extensions() {
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		final IntByReference count = new IntByReference();
		final VulkanFunction<VkExtensionProperties> func = mock(VulkanFunction.class);
		assertEquals(Set.of(), Extension.extensions(lib, count, func));
		verify(func).enumerate(count, null);
	}
}
