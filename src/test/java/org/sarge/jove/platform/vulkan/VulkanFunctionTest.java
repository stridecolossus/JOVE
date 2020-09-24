package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class VulkanFunctionTest {
	private Vulkan vulkan;
	private VulkanLibrary api;
	private IntByReference count;

	@BeforeEach
	public void before() {
		// Create API
		api = mock(VulkanLibrary.class);

		// Create Vulkan
		vulkan = mock(Vulkan.class);
		when(vulkan.api()).thenReturn(api);

		// Create counter
		count = new IntByReference(1);
		when(vulkan.integer()).thenReturn(count);
	}

	@SuppressWarnings("unchecked")
	@Test
	void enumerate() {
		final VulkanFunction<String[]> func = mock(VulkanFunction.class);
		final String[] array = VulkanFunction.enumerate(func, vulkan, String[]::new);
		assertNotNull(array);
		assertEquals(1, array.length);
		verify(func).enumerate(api, count, null);
		verify(func).enumerate(api, count, array);
	}

	@SuppressWarnings("unchecked")
	@Test
	void enumerateStructure() {
		// Create a structure array
		final Structure identity = mock(Structure.class);
		final Structure[] array = new Structure[]{identity};
		when(identity.toArray(1)).thenReturn(array);

		// Create function adapter
		final VulkanFunction<Structure> func = mock(VulkanFunction.class);
		final Structure[] result = VulkanFunction.enumerate(func, vulkan, identity);

		// Invoke and check array is populated
		assertArrayEquals(array, result);
		verify(func).enumerate(api, count, null);
		verify(func).enumerate(api, count, identity);
	}
}
