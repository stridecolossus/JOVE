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
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

import com.sun.jna.ptr.PointerByReference;

public class LogicalDeviceTest {
	private static final String FEATURE = "samplerAnisotropy";

	private LogicalDevice device;
	private PhysicalDevice parent;
	private Queue.Family family;
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

		// Init supported features
		when(parent.features()).thenReturn(DeviceFeatures.of(Set.of(FEATURE)));

		// Create queue family
		family = mock(Queue.Family.class);
		when(family.count()).thenReturn(2);
		when(family.device()).thenReturn(parent);
		when(parent.families()).thenReturn(List.of(family));

		// Init supported features
		final var features = new VkPhysicalDeviceFeatures();
		features.samplerAnisotropy = VulkanBoolean.TRUE;

		// Create logical device
		device = new LogicalDevice.Builder(parent)
				.queues(family, List.of(0.1f, 0.2f))
				.extension("ext")
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.features(new DeviceFeatures(features))
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
	void queuesFamily() {
		final var queues = device.queues(family);
		assertNotNull(queues);
		assertEquals(2, queues.size());
	}

	@Test
	void queue() {
		final Queue queue = device.queue(family);
		assertNotNull(queue);
		assertEquals(family, queue.family());
	}

	@Test
	void queueWaitIdle() {
		final Queue queue = device.queues().get(family).get(0);
		queue.waitIdle();
		verify(lib).vkQueueWaitIdle(queue.handle());
	}

	@Test
	void features() {
		final DeviceFeatures features = device.features();
		assertNotNull(features);
		assertEquals(true, features.isSupported(FEATURE));
	}

	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device.handle());
	}

	@Test
	void allocator() {
		assertNotNull(device.allocator());
	}

	@Test
	void semaphore() {
		// Create semaphore
		final Semaphore semaphore = device.semaphore();
		assertNotNull(semaphore);

		// Check API
		final ArgumentCaptor<VkSemaphoreCreateInfo> captor = ArgumentCaptor.forClass(VkSemaphoreCreateInfo.class);
		verify(lib).vkCreateSemaphore(eq(device.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check create descriptor
		final VkSemaphoreCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);
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
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		void duplicate() {
			builder.queue(family);
			builder.queue(family);
			device = builder.build();
			assertEquals(1, device.queues(family).size());
		}

		@Test
		void invalidPriority() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, List.of(2f)));
		}

		@Test
		void invalidQueueCount() {
			assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3));
		}

		@Test
		void invalidQueueFamily() {
			assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(Queue.Family.class)));
		}

		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS));
		}

		@Test
		void invalidSupportedFeature() {
			final var required = new VkPhysicalDeviceFeatures();
			required.wideLines = VulkanBoolean.TRUE;
			assertThrows(IllegalStateException.class, "wideLines", () -> builder.features(new DeviceFeatures(required)));
		}
	}
}
