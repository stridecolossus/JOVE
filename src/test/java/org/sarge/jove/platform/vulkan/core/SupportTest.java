package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;

import com.sun.jna.ptr.IntByReference;

public class SupportTest {
	private VulkanLibrary lib;
	private IntByReference count;

	@BeforeEach
	void before() {
		// Init Vulkan
		lib = mock(VulkanLibrary.class);

		// Init reference factory
		final var factory = mock(ReferenceFactory.class);
		when(lib.factory()).thenReturn(factory);

		// Create array length
		count = new IntByReference();
		when(factory.integer()).thenReturn(count);
	}

	@Test
	void extensions() {
		assertEquals(Set.of(), Support.extensions(lib));
		verify(lib).vkEnumerateInstanceExtensionProperties(null, count, null);
	}

	@Test
	void layers() {
		assertEquals(Set.of(), Support.layers(lib));
		verify(lib).vkEnumerateInstanceLayerProperties(count, null);
	}
}
