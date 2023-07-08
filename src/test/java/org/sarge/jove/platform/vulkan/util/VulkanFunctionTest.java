package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.MockStructure;

import com.sun.jna.Pointer;
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
		final VulkanFunction<Pointer[]> func = mock(VulkanFunction.class);
		final Pointer[] array = VulkanFunction.invoke(func, count, Pointer[]::new);
		assertEquals(2, array.length);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, array);
	}

	@Test
	void structure() {
		final var structure = new MockStructure();
		final VulkanFunction<VulkanStructure> func = mock(VulkanFunction.class);
		final VulkanStructure[] result = VulkanFunction.invoke(func, count, structure);
		assertEquals(2, result.length);
		verify(func).enumerate(count, null);
		verify(func).enumerate(count, structure);
	}
}
