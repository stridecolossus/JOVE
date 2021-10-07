package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Queue.Family;

import com.sun.jna.Pointer;

public class QueueTest {
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
}
