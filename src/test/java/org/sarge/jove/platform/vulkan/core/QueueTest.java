package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.core.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Queue.Selector;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class QueueTest extends AbstractVulkanTest {
	private Queue queue;
	private Family family;
	private PhysicalDevice parent;
	private Handle surface;

	@BeforeEach
	void before() {
		// Create physical device
		parent = mock(PhysicalDevice.class);
		when(parent.instance()).thenReturn(mock(Instance.class));
		when(parent.instance().library()).thenReturn(lib);
		when(dev.parent()).thenReturn(parent);

		// Create queue
		family = new Family(parent, 1, 2, Set.of(VkQueueFlag.GRAPHICS));
		queue = new Queue(new Pointer(1), dev, family);

		// Add to devices
		when(parent.families()).thenReturn(List.of(family));
		when(dev.queues()).thenReturn(Map.of(family, List.of(queue)));

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
		assertEquals(parent, family.device());
		assertEquals(Set.of(VkQueueFlag.GRAPHICS), family.flags());
	}

	@Test
	void isPresentationSupported() {
		assertEquals(true, family.isPresentationSupported(surface));
	}

	@Test
	void isPresentationSupportedNotSupported() {
		assertEquals(true, family.isPresentationSupported(surface));
	}

	@Nested
	class SelectorTests {
		private Predicate<Family> predicate;
		private Selector selector;

		@SuppressWarnings("unchecked")
		@BeforeEach
		void before() {
			predicate = mock(Predicate.class);
			selector = new Selector(predicate);
		}

		private void init() {
			when(predicate.test(family)).thenReturn(true);
		}

		@DisplayName("Selector should match a supported queue family")
		@Test
		void predicate() {
			init();
			assertEquals(true, selector.test(parent));
			assertEquals(family, selector.family());
		}

		@DisplayName("Selector should not match a queue family not supported by the device")
		@Test
		void predicateNotMatched() {
			assertEquals(false, selector.test(parent));
			assertThrows(NoSuchElementException.class, () -> selector.family());
		}

		@DisplayName("Queue family is unavailable until the selector has been initialised")
		@Test
		void familyNotInitialised() {
			assertThrows(NoSuchElementException.class, () -> selector.family());
		}

		@DisplayName("Selector should retrieve matched queues from the logical device")
		@Test
		void list() {
			init();
			selector.test(parent);
			assertEquals(List.of(queue), selector.list(dev));
			assertEquals(queue, selector.queue(dev));
		}

		@DisplayName("Selector should fail when the queue has not been configured")
		@Test
		void listInvalidFamily() {
			assertThrows(NoSuchElementException.class, () -> selector.list(dev));
		}
	}
}
