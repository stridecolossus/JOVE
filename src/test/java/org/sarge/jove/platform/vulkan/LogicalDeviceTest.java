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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;

import com.sun.jna.Pointer;

public class LogicalDeviceTest extends AbstractVulkanTest {
	private LogicalDevice dev;
	private QueueFamily family;
	private WorkQueue queue;

	@BeforeEach
	public void before() {
		family = mock(QueueFamily.class);
		queue = mock(WorkQueue.class);
		dev = new LogicalDevice(new VulkanHandle(new Pointer(42), Destructor.NULL), Map.of(family, List.of(queue)));
	}

	@Test
	public void queues() {
		assertEquals(Map.of(family, List.of(queue)), dev.queues());
		assertEquals(List.of(queue), dev.queues(family));
		assertEquals(queue, dev.queue(family, 0));
	}

	@Test
	public void queuesInvalidFamily() {
		assertThrows(IllegalArgumentException.class, () -> dev.queues(mock(QueueFamily.class)));
	}

	@Test
	public void queuesInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> dev.queue(family, 999));
	}

	@Test
	public void semaphore() {
		final Handle semaphore = dev.semaphore();
		final Pointer prev = semaphore.handle();
		assertNotNull(semaphore);
		verify(library).vkCreateSemaphore(eq(dev.handle()), argThat(structure(new VkSemaphoreCreateInfo())), isNull(), eq(factory.reference()));
		semaphore.destroy();
		verify(library).vkDestroySemaphore(dev.handle(), prev, null);
	}

	@Nested
	class BuilderTests {
		private PhysicalDevice parent;
		private LogicalDevice.Builder builder;

		@BeforeEach
		public void before() {
			// Create parent device
			parent = mock(PhysicalDevice.class);
			when(parent.supported()).thenReturn(mock(Supported.class));
			when(parent.families()).thenReturn(List.of(family));

			// Create queue family and entry
			final QueueFamily.Entry entry = mock(QueueFamily.Entry.class);
			when(entry.priorities()).thenReturn(new float[]{1});
			when(parent.families()).thenReturn(List.of(family));
			when(family.queue()).thenReturn(entry);
			when(entry.family()).thenReturn(family);

			// Create builder
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		public void build() {
			// Create logical device with one queue
			dev = builder
				.queue(family.queue())
				.extension("ext")
				.layer("layer", 1)
				.build();

			// Check created device
			assertNotNull(dev);
			verify(library).vkCreateDevice(eq(parent.handle()), any(VkDeviceCreateInfo.class), isNull(), eq(factory.reference()));

			// Check queue
			assertNotNull(dev.queue(family, 0));
			verify(library).vkGetDeviceQueue(eq(dev.handle()), eq(0), eq(0), eq(factory.reference()));

			// Check extensions and layers
			// TODO
		}

		@Test
		public void buildQueueInvalid() {
			when(parent.families()).thenReturn(List.of());
			assertThrows(IllegalArgumentException.class, () -> builder.queue(family.queue()).build());
		}
	}
}
