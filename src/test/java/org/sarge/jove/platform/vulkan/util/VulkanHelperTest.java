package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkRect2D;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.ptr.IntByReference;

public class VulkanHelperTest {
	@Test
	void populate() {
		final VkRect2D rect = new VkRect2D();
		VulkanHelper.populate(new Rectangle(1, 2, 3, 4), rect);
		assertEquals(1, rect.offset.x);
		assertEquals(2, rect.offset.y);
		assertEquals(3, rect.extent.width);
		assertEquals(4, rect.extent.height);
	}

	@Test
	void buffer() {
		final ByteBuffer bb = VulkanHelper.buffer(42);
		assertNotNull(bb);
		assertEquals(42, bb.limit());
		assertEquals(42, bb.capacity());
		assertEquals(0, bb.position());
		assertEquals(ByteOrder.nativeOrder(), bb.order());
	}

	@Test
	void extensions() {
		// Init Vulkan
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));

		// Init reference factory
		final IntByReference count = new IntByReference(0);
		when(lib.factory().integer()).thenReturn(count);

		// Enumerate extensions
		final VulkanFunction<VkExtensionProperties> func = mock(VulkanFunction.class);
		assertEquals(Set.of(), VulkanHelper.extensions(lib, func));
		verify(func).enumerate(lib, count, null);
	}
}
