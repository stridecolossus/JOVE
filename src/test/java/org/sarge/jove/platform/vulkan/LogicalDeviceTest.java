package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.LogicalDevice.Queue;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class LogicalDeviceTest {
	private LogicalDevice device;
	private PhysicalDevice parent;
	private QueueFamily family;
	private Vulkan vulkan;
	private VulkanLibrary api;

	@BeforeEach
	void before() {
		// Init Vulkan
		final PointerByReference ref = new PointerByReference(new Pointer(42));
		vulkan = mock(Vulkan.class);
		when(vulkan.pointer()).thenReturn(ref);

		// Init API
		api = mock(VulkanLibrary.class);
		when(vulkan.api()).thenReturn(api);

		// Create parent device
		parent = mock(PhysicalDevice.class);
		when(parent.vulkan()).thenReturn(vulkan);

		// Create queue family
		family = mock(QueueFamily.class);
		when(parent.families()).thenReturn(List.of(family));

		// Create logical device
		device = new LogicalDevice(new Pointer(1), parent, Map.of(family, List.of(new Pointer(2))));
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
		assertEquals(1, list.size());
		assertNotNull(list.get(0));
	}

	@Test
	void waitIdle() {
		device.waitIdle();
		verify(api).vkDeviceWaitIdle(device.handle());
	}

	@Test
	void destroy() {
		device.destroy();
		verify(vulkan.api()).vkDestroyDevice(device.handle(), null);
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder();
			when(family.count()).thenReturn(2);
		}

		@Test
		void build() {
			// Build device
			device = builder
					.parent(parent)
					.extension("ext")
					.layer(new ValidationLayer("layer", 42))
					.queue(family, new float[]{0.1f, 0.2f})
					.build();

			// Check device
			assertNotNull(device);
			assertEquals(parent, device.parent());

			// Check queues
			assertNotNull(device.queues());
			assertEquals(1, device.queues().size());
			assertTrue(device.queues().containsKey(family));
			assertEquals(2, device.queues().get(family).size());

			// TODO - capture info
			// TODO - verify queue lookup API
		}

		@Test
		void missingParent() {
			assertThrows(IllegalArgumentException.class, () -> new LogicalDevice.Builder().build());
		}

		@Test
		void invalidPriority() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(family, new float[]{999}));
		}

		@Test
		void invalidQueueCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(family, 3));
		}
	}

	@Nested
	class QueueTests {
		private Queue queue;

		@BeforeEach
		void before() {
			queue = device.queues().get(family).get(0);
		}

		@Test
		void constructor() {
			assertEquals(family, queue.family());
			assertEquals(device, queue.device());
			assertNotNull(queue.handle());
		}

		@Test
		void waitIdle() {
			queue.waitIdle();
			verify(api).vkQueueWaitIdle(queue.handle());
		}
	}
}
