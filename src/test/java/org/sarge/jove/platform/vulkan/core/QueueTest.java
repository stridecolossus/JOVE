package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.core.Queue.Family;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class QueueTest extends AbstractVulkanTest {
	private Queue queue;
	private Family family;
	private PhysicalDevice physical;
	private Handle surface;

	@BeforeEach
	void before() {
		// Create physical device
		physical = mock(PhysicalDevice.class);
		when(physical.instance()).thenReturn(mock(Instance.class));
		when(physical.instance().library()).thenReturn(lib);

		// Create queue
		family = new Family(physical, 1, 2, Set.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT));
		queue = new Queue(new Pointer(1), dev, family);

		// Create a Vulkan surface
		surface = new Handle(new Pointer(2));
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), queue.handle());
		assertEquals(dev, queue.device());
		assertEquals(family, queue.family());
	}

	@Test
	void waitIdle() {
		queue.waitIdle();
		verify(lib).vkQueueWaitIdle(queue.handle());
	}

	@Test
	void family() {
		assertEquals(1, family.index());
		assertEquals(2, family.count());
		assertEquals(physical, family.device());
		assertEquals(Set.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT), family.flags());
	}

	@Test
	void isPresentationSupported() {
		assertEquals(true, family.isPresentationSupported(surface));
	}

	@Test
	void isPresentationSupportedNotSupported() {
		assertEquals(true, family.isPresentationSupported(surface));
	}

	@Test
	void predicateQueueFlags() {
		final var predicate = Family.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		assertNotNull(predicate);
		assertEquals(true, predicate.test(family));
		assertEquals(false, predicate.test(mock(Family.class)));
	}

	@Test
	void predicatePresentation() {
		final var predicate = Family.predicate(surface);
		assertNotNull(predicate);
		assertEquals(true, predicate.test(family));
		assertEquals(false, predicate.test(mock(Family.class)));
	}
}
