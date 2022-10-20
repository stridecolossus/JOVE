package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.ptr.IntByReference;

class VulkanLibraryTest {
	@Test
	void version() {
		assertEquals("1.1.0", VulkanLibrary.VERSION.toString());
	}

	@Test
	void mapper() {
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(IntegerEnumeration.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(Boolean.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(boolean.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(Handle.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(NativeObject.class));
	}

	@Test
	void create() {
		assertNotNull(VulkanLibrary.create());
	}

	@Test
	void extensions() {
		final var lib = mock(VulkanLibrary.class);
		final var count = new IntByReference(1);
		assertNotNull(VulkanLibrary.extensions(lib, count));
		verify(lib).vkEnumerateInstanceExtensionProperties(null, count, null);
	}

	@Nested
	class CheckTests {
		@Test
		void success() {
			VulkanLibrary.check(VulkanLibrary.SUCCESS);
		}

		@Test
		void error() {
			assertThrows(VulkanException.class, () -> VulkanLibrary.check(VkResult.ERROR_DEVICE_LOST.value()));
		}

		@Test
		void unknown() {
			assertThrows(VulkanException.class, () -> VulkanLibrary.check(-1));
		}
	}
}
