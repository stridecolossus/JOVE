package org.sarge.jove.platform.vulkan.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

@SuppressWarnings("unchecked")
public class VulkanFunctionTest {
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
		final Structure struct = mock(Structure.class);
		final Structure[] array = new Structure[]{struct, struct};
		when(struct.toArray(2)).thenReturn(array);

		// Invoke and check resultant array
		final StructureVulkanFunction<Structure> func = spy(StructureVulkanFunction.class);
		final Structure[] result = func.invoke(count, () -> struct);
		assertEquals(array, result);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, struct);
	}
}
