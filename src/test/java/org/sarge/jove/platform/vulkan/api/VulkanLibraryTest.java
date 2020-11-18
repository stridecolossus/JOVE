package org.sarge.jove.platform.vulkan.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.INTEGRATION_TEST;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
		assertThrows(RuntimeException.class, () -> VulkanLibrary.check(VkResult.VK_ERROR_DEVICE_LOST.value()));
	}

	@Test
	void checkThrowsUnknownErrorCode() {
		assertThrows(RuntimeException.class, () -> VulkanLibrary.check(-1));
	}

	@Test
	void mapper() {
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(IntegerEnumeration.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(VulkanBoolean.class));
		assertNotNull(VulkanLibrary.MAPPER.getToNativeConverter(Handle.class));
	}

	@Tag(INTEGRATION_TEST)
	@Test
	void create() {
		final VulkanLibrary api = VulkanLibrary.create();
		assertNotNull(api);
		assertNotNull(api.factory());
	}

	@Nested
	class StructureTests {
		private MockStructure struct;

		@BeforeEach
		void before() {
			struct = new MockStructure();
		}

		@Test
		void copy() {
			final MockStructure copy = struct.copy();
			assertNotNull(copy);
			assertEquals(true, struct.dataEquals(copy));
		}

		@Test
		void toArray() {
			final var array = struct.toArray(2);
			assertNotNull(array);
			assertEquals(2, array.length);
			assertEquals(true, struct.dataEquals(array[0]));
			assertEquals(true, struct.dataEquals(array[1]));
		}

		@Test
		void populate() {
			final var array = VulkanStructure.populateArray(() -> struct, List.of(VkStructureType.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO), (data, out) -> out.sType = data);
			assertNotNull(array);
			assertEquals(1, array.length);
			assertEquals(VkStructureType.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO, array[0].sType);
		}
	}
}

