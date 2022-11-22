package org.sarge.jove.platform.vulkan.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;

import com.sun.jna.ptr.IntByReference;

@SuppressWarnings("unchecked")
class VulkanFunctionTest {
	private IntByReference count;

	@BeforeEach
	void before() {
		count = new IntByReference(2);
	}

	@Test
	void array() {
		final VulkanFunction<byte[]> func = spy(VulkanFunction.class);
		final byte[] array = func.invoke(count, byte[]::new);
		assertNotNull(array);
		assertEquals(2, array.length);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, array);
	}

	@Test
	void structure() {
		// Create identity instance
		final var struct = mock(VulkanStructure.class);
		final var array = new VulkanStructure[]{struct, struct};
		when(struct.toArray(2)).thenReturn(array);

		// Invoke and check resultant array
		final StructureVulkanFunction<VulkanStructure> func = spy(StructureVulkanFunction.class);
		final VulkanStructure[] result = func.invoke(count, struct);
		assertEquals(array, result);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, struct);
	}
}
