package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class QueueTest extends AbstractVulkanTest {
	private Queue queue;
	private Family family;

	@BeforeEach
	void before() {
		family = new Family(1, 2, Set.of(VkQueueFlag.GRAPHICS));
		queue = new Queue(new Handle(new Pointer(3)), dev, family);
	}

	@Test
	void constructor() {
		assertEquals(family, queue.family());
		assertEquals(dev, queue.device());
	}

	@Test
	void family() {
		assertEquals(1, family.index());
		assertEquals(2, family.count());
		assertEquals(Set.of(VkQueueFlag.GRAPHICS), family.flags());
	}

	@Test
	void waitIdle() {
		queue.waitIdle();
		verify(lib).vkQueueWaitIdle(queue.handle());
	}

	@Test
	void isPresentationSupported() {
		// Mock physical device
		final DeviceContext ctx = mock(DeviceContext.class);
		when(ctx.handle()).thenReturn(DEVICE);
		when(ctx.library()).thenReturn(lib);

		// Check API
		final Handle surface = new Handle(new Pointer(5));
		assertEquals(true, family.isPresentationSupport(dev, surface));
		verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(DEVICE, 1, surface, INTEGER);
	}
}
