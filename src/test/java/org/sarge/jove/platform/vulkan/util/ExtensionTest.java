package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.ptr.IntByReference;

public class ExtensionTest {
	@Test
	void extensions() {
		// Init function
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		final IntByReference count = new IntByReference(1);
		final VulkanFunction<VkExtensionProperties> func = mock(VulkanFunction.class);

		// Return an extension
		final String ext = "ext";
		final Answer<Integer> answer = inv -> {
			final VkExtensionProperties props = inv.getArgument(1);
			props.extensionName = ext.getBytes();
			return 0;
		};
		doAnswer(answer).when(func).enumerate(eq(count), any(VkExtensionProperties.class));

		// Check extension
		assertEquals(Set.of(ext), Extension.extensions(lib, count, func));
	}
}
