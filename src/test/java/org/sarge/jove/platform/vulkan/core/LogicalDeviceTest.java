package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.Instance;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Queue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

public class LogicalDeviceTest {
	private LogicalDevice device;
	private PhysicalDevice parent;
	private QueueFamily family;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create instance
		final Instance instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create parent device
		parent = mock(PhysicalDevice.class);
		when(parent.instance()).thenReturn(instance);

		// Create queue family
		family = mock(QueueFamily.class);
		when(family.count()).thenReturn(2);
		when(family.device()).thenReturn(parent);
		when(parent.families()).thenReturn(List.of(family));

		// Create logical device
		device = new LogicalDevice.Builder()
				.parent(parent)
				.queues(family, new float[]{0.1f, 0.2f})
				.extension("ext")
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();
	}

	@Test
	void constructor() {
		assertNotNull(device.handle());
		assertEquals(parent, device.parent());
	}

	@Test
	void queues() {
		// Check queues
		assertNotNull(device.queues());
		assertEquals(1, device.queues().size());
		assertEquals(true, device.queues().containsKey(family));

		// Check queues for family
		final var list = device.queues().get(family);
		assertNotNull(list);
		assertEquals(2, list.size());

		final Queue queue = list.get(0);
		assertNotNull(queue);
		assertEquals(family, queue.family());
		assertNotNull(queue.handle());
	}

	@Test
	void queueWaitIdle() {
		final Queue queue = device.queues().get(family).get(0);
		queue.waitIdle();
		verify(lib).vkQueueWaitIdle(queue.handle());
	}

	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device.handle());
	}

	@Test
	void destroy() {
		device.destroy();
		verify(lib).vkDestroyDevice(device.handle(), null);
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder().parent(parent);
		}

		@Test
		void missingParent() {
			assertThrows(IllegalArgumentException.class, () -> new LogicalDevice.Builder().build());
		}

		@Test
		void invalidPriority() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, new float[]{999}));
		}

		@Test
		void invalidQueueCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3));
		}

		@Test
		void invalidQueueFamily() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(QueueFamily.class)));
		}

		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS));
		}
	}
}
