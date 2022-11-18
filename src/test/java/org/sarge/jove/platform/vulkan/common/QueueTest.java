package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.BitField;

import com.sun.jna.Pointer;

class QueueTest {
	private Queue queue;
	private Family family;

	@BeforeEach
	void before() {
		family = new Family(1, 2, Set.of(VkQueueFlag.GRAPHICS));
		queue = new Queue(new Handle(new Pointer(3)), family);
	}

	@Test
	void constructor() {
		assertEquals(family, queue.family());
	}

	@Test
	void family() {
		assertEquals(1, family.index());
		assertEquals(2, family.count());
		assertEquals(Set.of(VkQueueFlag.GRAPHICS), family.flags());
	}

	@Test
	void waitIdle() {
		final VulkanLibrary lib = mock(VulkanLibrary.class);
		queue.waitIdle(lib);
		verify(lib).vkQueueWaitIdle(queue);
	}

	@Test
	void of() {
		final var props = new VkQueueFamilyProperties();
		props.queueCount = 2;
		props.queueFlags = BitField.reduce(VkQueueFlag.GRAPHICS);
		assertEquals(family, Family.of(1, props));
	}
}
