package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public class VulkanFunctionTest extends AbstractVulkanTest {
	private IntByReference count;

	@BeforeEach
	public void before() {
		count = new IntByReference(1);
	}

	@Test
	public void enumerate() {
		@SuppressWarnings("unchecked")
		final VulkanFunction<Structure> func = mock(VulkanFunction.class);
		final Structure struct = mock(Structure.class);
		when(struct.toArray(1)).thenReturn(new Structure[]{struct});
		final var results = VulkanFunction.enumerate(func, count, struct);
		assertNotNull(results);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, struct);
	}

	@Test
	public void array() {
		@SuppressWarnings("unchecked")
		final VulkanFunction<String[]> func = mock(VulkanFunction.class);
		final var array = VulkanFunction.array(func, count, String[]::new);
		assertNotNull(array);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, new String[1]);
	}
}
