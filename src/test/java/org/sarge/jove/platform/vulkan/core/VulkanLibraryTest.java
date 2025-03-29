package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.*;

import com.sun.jna.Library;

class VulkanLibraryTest {
	@DisplayName("The Vulkan library has an implementation version number")
	@Test
	void version() {
		assertEquals("1.1.0", VulkanLibrary.VERSION.toString());
	}

	@DisplayName("The Vulkan library can marshal JOVE types")
	@ParameterizedTest
	@ValueSource(classes={boolean.class, Boolean.class, IntEnum.class, EnumMask.class, Handle.class, NativeObject.class})
	void mapper(Class<?> type) {
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(type));
	}

	@DisplayName("The Vulkan library options include the JOVE type mapper")
	@Test
	void options() {
		assertEquals(Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER), VulkanLibrary.options());
	}

	@DisplayName("The Vulkan library can be instantiated")
	@Test
	void create() {
		VulkanLibrary.create();
	}

	@DisplayName("A Vulkan API method that returns...")
	@Nested
	class CheckTests {
		@DisplayName("the SUCCESS result code is valid")
		@Test
		void success() {
			VulkanLibrary.check(VulkanLibrary.SUCCESS);
		}

		@DisplayName("an error result code fails")
		@Test
		void error() {
			assertThrows(VulkanException.class, () -> VulkanLibrary.check(VkResult.ERROR_DEVICE_LOST.value()));
		}

		@DisplayName("an unknown result code fails")
		@Test
		void unknown() {
			assertThrows(VulkanException.class, () -> VulkanLibrary.check(999));
		}
	}

	@Test
	void rectangle() {
		final var rect = new Rectangle(1, 2, 3, 4);
		final var struct = new VkRect2D();
		VulkanLibrary.populate(rect, struct);
		assertEquals(1, struct.offset.x);
		assertEquals(2, struct.offset.y);
		assertEquals(3, struct.extent.width);
		assertEquals(4, struct.extent.height);
	}

	@DisplayName("An offset or size must be a multiple of 4 bytes")
	@ParameterizedTest
	@ValueSource(ints={0, 4, 8})
	void checkAlignment(int size) {
		VulkanLibrary.checkAlignment(size);
	}

	@DisplayName("An offset or size that is not a multiple of 4 bytes is invalid")
	@ParameterizedTest
	@ValueSource(ints={1, 2, 3})
	void checkAlignmentInvalid(int size) {
		assertThrows(IllegalArgumentException.class, () -> VulkanLibrary.checkAlignment(size));
	}
}
