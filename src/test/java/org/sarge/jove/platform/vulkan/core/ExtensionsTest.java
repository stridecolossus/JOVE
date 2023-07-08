package org.sarge.jove.platform.vulkan.core;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import com.sun.jna.ptr.IntByReference;

class ExtensionsTest {
	@Test
	void extensions() {
		final var lib = mock(VulkanLibrary.class);
		final var count = new IntByReference(1);
		Extensions.extensions(lib, count);
		verify(lib).vkEnumerateInstanceExtensionProperties(null, count, null);
	}
}
