package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.LogicalDevice.Queue;
import org.sarge.jove.platform.vulkan.LogicalDevice.Work;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;

public class LogicalDeviceTest extends AbstractVulkanTest {
	private PhysicalDevice parent;
	private QueueFamily family;

	@BeforeEach
	public void before() {
		// Create parent physical device
		parent = mock(PhysicalDevice.class);
		when(parent.vulkan()).thenReturn(vulkan);

		// Create a queue family
		family = mock(QueueFamily.class);
		when(family.count()).thenReturn(1);

		// Create logical device
		device = new LogicalDevice(new Pointer(42), parent, Map.of(family, List.of(mock(Pointer.class))));
	}

	@Test
	public void constructor() {
		assertEquals(parent, device.parent());
		assertEquals(vulkan, device.vulkan());
	}

	@Test
	public void queues() {
		final Queue queue = device.queue(family);
		assertNotNull(queue);
		assertEquals(family, queue.family());
		assertEquals(device, queue.device());
		assertEquals(queue, device.queue(family, 0));
		assertEquals(List.of(queue), device.queues(family));
		assertEquals(Map.of(family, List.of(queue)), device.queues());
	}

	@Test
	public void queuesInvalidFamily() {
		final QueueFamily other = mock(QueueFamily.class);
		assertThrows(IllegalArgumentException.class, () -> device.queues(other));
		assertThrows(IllegalArgumentException.class, () -> device.queue(other));
	}

	@Test
	public void queueInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> device.queue(family, 999));
	}

	@Test
	public void semaphore() {
		final PointerHandle semaphore = device.semaphore();
		final Pointer prev = semaphore.handle();
		assertNotNull(semaphore);
		verify(library).vkCreateSemaphore(eq(device.handle()), argThat(structure(new VkSemaphoreCreateInfo())), isNull(), eq(factory.reference()));
		semaphore.destroy();
		verify(library).vkDestroySemaphore(device.handle(), prev, null);
	}

	@Test
	public void waitIdle() {
		device.waitIdle();
		verify(library).vkDeviceWaitIdle(device.handle());
	}

	@Nested
	class QueueTests {
		private Queue queue;

		@BeforeEach
		public void before() {
			queue = device.queue(family);
		}

		@Test
		public void constructor() {
			assertEquals(device, queue.device());
			assertEquals(family, queue.family());
		}

		@Test
		public void work() {
			// Create dependencies
			final PointerHandle semaphore = mock(PointerHandle.class);
			final Fence fence = mock(Fence.class);
			final Command.Buffer buffer = mock(Command.Buffer.class);
			when(buffer.isReady()).thenReturn(true);

			// Create work
			final Work work = queue.work()
				.signal(semaphore)
				.wait(semaphore)
				.fence(fence)
				.add(buffer)
				.build();

			// Submit work
			assertNotNull(work);
			work.submit();
			// TODO - verify
		}

		@Test
		public void workMissingCommand() {
			assertThrows(IllegalArgumentException.class, () -> queue.work().build());
		}
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		public void before() {
			// Create parent device
			when(parent.supported()).thenReturn(mock(Supported.class));
			when(parent.families()).thenReturn(List.of(family));

			// Create builder
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		public void build() {
			// Create logical device with one queue
			device = builder
				.queue(family)
				.extension("ext")
				.layer("layer", 1)
				.build();

			// Check created device
			assertNotNull(device);
			verify(library).vkCreateDevice(eq(parent.handle()), any(VkDeviceCreateInfo.class), isNull(), eq(factory.reference()));

			// Check queue
			assertNotNull(device.queue(family, 0));
			verify(library).vkGetDeviceQueue(eq(device.handle()), eq(0), eq(0), eq(factory.reference()));

			// Check extensions and layers
			// TODO
		}

		@Test
		public void buildQueueInvalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(QueueFamily.class)).build());
		}

		@Test
		public void buildTooManyQueues() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(family, 2).build());
		}

		@Test
		public void buildInvalidPriority() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(family, new float[]{2}).build());
		}

		@Test
		public void buildUnsupportedFeature() {
			final VkPhysicalDeviceFeatures required = new VkPhysicalDeviceFeatures();
			when(parent.enumerateUnsupportedFeatures(required)).thenReturn(Set.of("doh"));
			builder.features(required);
			assertThrows(ServiceException.class, () -> builder.build());
		}
	}
}
