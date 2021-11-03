package org.sarge.jove.platform.vulkan.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class VulkanFunctionTest {
	private VulkanLibrary lib;
	private IntByReference count;

	@BeforeEach
	void before() {
		lib = mock(VulkanLibrary.class);
		count = new IntByReference(2);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().integer()).thenReturn(count);
	}

	@Test
	void invokeArray() {
		final VulkanFunction<byte[]> func = mock(VulkanFunction.class);
		final byte[] array = VulkanFunction.invoke(func, lib, byte[]::new);
		assertNotNull(array);
		assertEquals(2, array.length);
		verify(func).enumerate(lib, count, null);
		verify(func).enumerate(lib, count, array);
	}

	@Test
	void structure() {
		// Create identity instance
		final Structure struct = mock(Structure.class);
		final Structure[] array = new Structure[]{struct, struct};
		when(struct.toArray(2)).thenReturn(array);

		// Invoke and check resultant array
		final VulkanFunction<Structure> func = mock(VulkanFunction.class);
		final Structure[] result = VulkanFunction.invoke(func, lib, () -> struct);
		assertEquals(array, result);
		verify(func).enumerate(lib, count, null);
		verify(func).enumerate(lib, count, struct);
	}
}
