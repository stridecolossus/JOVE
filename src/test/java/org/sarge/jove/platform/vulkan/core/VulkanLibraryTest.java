package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

class VulkanLibraryTest {
	@Test
	void version() {
		assertEquals("1.1.0", VulkanLibrary.VERSION.toString());
	}

	@ParameterizedTest
	@ValueSource(classes={boolean.class, Boolean.class, IntegerEnumeration.class, Handle.class, NativeObject.class})
	void mapper(Class<?> type) {
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(type));
	}

	@Test
	void options() {
		assertEquals(Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER), VulkanLibrary.options());
	}

	@Test
	void create() {
		VulkanLibrary.create();
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
