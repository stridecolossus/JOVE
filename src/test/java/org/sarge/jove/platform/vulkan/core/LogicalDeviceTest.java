package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class LogicalDeviceTest {
	private LogicalDevice device;
	private PhysicalDevice parent;
	private Queue.Family family;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		// Init API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(mock(ReferenceFactory.class));
		when(lib.factory().pointer()).thenReturn(new PointerByReference(new Pointer(1)));

		// Create instance
		final Instance instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create parent device
		parent = mock(PhysicalDevice.class);
		when(parent.instance()).thenReturn(instance);

		// Create queue family
		family = new Family(1, 2, Set.of());
		when(parent.families()).thenReturn(List.of(family));

		// Create logical device
		device = new LogicalDevice.Builder(parent)
				.queues(family, List.of(Percentile.HALF, Percentile.ONE))
				.extension("ext")
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();
	}

	@Test
	void constructor() {
		assertEquals(parent, device.parent());
		assertEquals(lib, device.library());
		assertEquals(false, device.isDestroyed());
	}

	@DisplayName("Query device for available queues")
	@Test
	void queues() {
		// Check queues map
		final var map = device.queues();
		assertNotNull(map);
		assertEquals(Set.of(family), map.keySet());

		// Check allocated queues
		final var queues = map.get(family);
		assertEquals(2, queues.size());

		// Check queue
		final Queue queue = queues.get(0);
		assertNotNull(queue);
		assertEquals(family, queue.family());
	}

	@DisplayName("Query device for the first queue of the given family")
	@Test
	void queue() {
		final Queue queue = device.queue(family);
		assertNotNull(queue);
		assertEquals(family, queue.family());
	}

	@DisplayName("Wait for queue to complete execution")
	@Test
	void queueWaitIdle() {
		final Queue queue = device.queues().get(family).get(0);
		queue.waitIdle(lib);
		verify(lib).vkQueueWaitIdle(queue);
	}

	@DisplayName("Wait for all queues to complete execution")
	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device);
	}

	@DisplayName("Create a semaphore for this device")
	@Test
	void semaphore() {
		// Create semaphore
		final Semaphore semaphore = device.semaphore();
		assertNotNull(semaphore);

		// Check API
		final ArgumentCaptor<VkSemaphoreCreateInfo> captor = ArgumentCaptor.forClass(VkSemaphoreCreateInfo.class);
		verify(lib).vkCreateSemaphore(eq(device), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check create descriptor
		final VkSemaphoreCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);
	}

	@Test
	void destroy() {
		device.close();
		verify(lib).vkDestroyDevice(device, null);
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder(parent);
		}

		@DisplayName("Duplicate queues should be aggregated")
		@Test
		void duplicate() {
			builder.queue(family);
			builder.queue(family);
			device = builder.build();
			assertEquals(1, device.queues().get(family).size());
		}

		@DisplayName("Cannot request more queues than available")
		@Test
		void invalidQueueCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3));
		}

		@DisplayName("Cannot request a queue from a different device")
		@Test
		void invalidQueueFamily() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new Family(3, 4, Set.of())));
		}

		@DisplayName("Cannot request an extension that is not available")
		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS));
		}
	}
}
