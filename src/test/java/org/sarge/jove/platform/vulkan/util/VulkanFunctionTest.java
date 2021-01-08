package org.sarge.jove.platform.vulkan.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

@SuppressWarnings("unchecked")
public class VulkanFunctionTest {
	private VulkanLibrary lib;
	private IntByReference count;

	@BeforeEach
	public void before() {
		lib = mock(VulkanLibrary.class);
		count = new IntByReference(1);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().integer()).thenReturn(count);
	}

	@Test
	void enumerate() {
		final VulkanFunction<String[]> func = mock(VulkanFunction.class);
		final String[] array = VulkanFunction.enumerate(func, lib, String[]::new);
		assertNotNull(array);
		assertEquals(1, array.length);
		verify(func).enumerate(lib, count, null);
		verify(func).enumerate(lib, count, array);
	}

	@Test
	void enumerateStructure() {
		// Create identity instance
		final Structure identity = mock(Structure.class);
		final Structure[] array = new Structure[]{identity};
		when(identity.toArray(1)).thenReturn(array);

		// Create function adapter
		final VulkanFunction<Structure> func = mock(VulkanFunction.class);
		final Structure[] result = VulkanFunction.enumerate(func, lib, () -> identity);

		// Invoke and check array is populated
		assertArrayEquals(array, result);
		verify(func).enumerate(lib, count, null);
		verify(func).enumerate(lib, count, identity);
	}

	@Test
	void enumerateStructureEmpty() {
		// Create identity instance
		final Structure identity = mock(Structure.class);
		when(identity.toArray(0)).thenReturn(new Structure[]{});

		// Create function
		final VulkanFunction<Structure> func = mock(VulkanFunction.class);
		count.setValue(0);

		// Invoke and check empty array returned
		final Structure[] result = VulkanFunction.enumerate(func, lib, () -> identity);
		assertNotNull(result);
		assertEquals(0, result.length);
	}
}
