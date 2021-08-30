package org.sarge.jove.platform.vulkan.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanException;

import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.IntByReference;

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
		assertThrows(VulkanException.class, () -> VulkanLibrary.check(VkResult.VK_ERROR_DEVICE_LOST.value()));
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
	class SupportTests {
		private VulkanLibrary lib;
		private IntByReference count;

		@BeforeEach
		void before() {
			lib = mock(VulkanLibrary.class);
			count = new IntByReference(0);
			when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
			when(lib.factory().integer()).thenReturn(count);
		}

		@Test
		void extensions() {
			VulkanLibrary.extensions(lib);
			verify(lib).vkEnumerateInstanceExtensionProperties(null, count, null);
		}

		@Test
		void layers() {
			VulkanLibrary.layers(lib);
			verify(lib).vkEnumerateInstanceLayerProperties(count, null);
		}
	}

	// Note has to be public
	@FieldOrder("sType")
	public static class MockStructure extends VulkanStructure {
		public VkStructureType sType;
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
