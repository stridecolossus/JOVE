package org.sarge.jove.platform.vulkan.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.MockStructure;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
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

	@Tag(AbstractVulkanTest.INTEGRATION_TEST)
	@Test
	void create() {
		final VulkanLibrary api = VulkanLibrary.create();
		assertNotNull(api);
	}

	@Nested
	class StructureTests {
		private MockStructure struct;

		@BeforeEach
		void before() {
			struct = new MockStructure();
		}

		@Test
		void getFieldList() {
			assertNotNull(struct.getFieldList());
		}

		@Test
		void toArray() {
			final MockStructure[] array = (MockStructure[]) struct.toArray(2);
			assertNotNull(array);
			assertEquals(2, array.length);
			assertEquals(struct.sType, array[0].sType);
		}
	}
}
