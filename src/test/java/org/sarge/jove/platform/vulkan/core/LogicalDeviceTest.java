package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.ReferenceFactory;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class LogicalDeviceTest {
	private LogicalDevice device;
	private PhysicalDevice parent;
	private Queue.Family family;
	private Queue queue;
	private VulkanLibrary lib;
	private PointerByReference ref;

	@BeforeEach
	void before() {
		// Create instance
		final Instance instance = mock(Instance.class);

		// Init Vulkan
		lib = mock(VulkanLibrary.class);
		when(instance.library()).thenReturn(lib);

		// Init reference factory
		final ReferenceFactory factory = mock(ReferenceFactory.class);
		ref = new PointerByReference(new Pointer(1));
		when(factory.pointer()).thenReturn(ref);
		when(instance.factory()).thenReturn(factory);

		// Create parent device
		parent = mock(PhysicalDevice.class);
		when(parent.instance()).thenReturn(instance);

		// Init device properties
		final var props = new VkPhysicalDeviceProperties();
		props.limits = new VkPhysicalDeviceLimits();
		when(parent.properties()).thenReturn(props);

		// Init enabled features
		final DeviceFeatures features = mock(DeviceFeatures.class);

		// Create queue family
		family = new Family(1, 2, Set.of());
		when(parent.families()).thenReturn(List.of(family));

		// Create work queue
		queue = new Queue(new Handle(1), family);

		// Create logical device
		device = new LogicalDevice(new Pointer(1), parent, features, Map.of(family, List.of(queue, queue)));
	}

	@Test
	void constructor() {
		assertEquals(parent, device.parent());
		assertEquals(lib, device.library());
		assertEquals(parent.instance().factory(), device.factory());
		assertNotNull(device.features());
		assertNotNull(device.provider());
		assertEquals(false, device.isDestroyed());
	}

	@DisplayName("Query device for available queues")
	@Test
	void queues() {
		assertEquals(Map.of(family, List.of(queue, queue)), device.queues());
	}

	@DisplayName("Query device for the first queue of the given family")
	@Test
	void queue() {
		assertEquals(queue, device.queue(family));
	}

	@DisplayName("Wait for queue to complete execution")
	@Test
	void queueWaitIdle() {
		queue.waitIdle(lib);
		verify(lib).vkQueueWaitIdle(queue);
	}

	@DisplayName("Wait for all device queues to complete execution")
	@Test
	void waitIdle() {
		device.waitIdle();
		verify(lib).vkDeviceWaitIdle(device);
	}

	@Test
	void destroy() {
		device.destroy();
		verify(lib).vkDestroyDevice(device, null);
	}

	@Nested
	class RequiredQueueTests {
		@DisplayName("A required queue must have at least one queue priority")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new RequiredQueue(family, List.of()));
		}

		@DisplayName("The number of queues cannot exceed the available number of queues in the family")
		@Test
		void exceeds() {
			assertThrows(IllegalArgumentException.class, () -> new RequiredQueue(family, 3));
		}
	}

	@Nested
	class BuilderTests {
		private LogicalDevice.Builder builder;

		@BeforeEach
		void before() {
			builder = new LogicalDevice.Builder(parent);
		}

		@Test
		void build() {
			// Init a required device feature
			final var required = new VkPhysicalDeviceFeatures();
			required.samplerAnisotropy = VulkanBoolean.TRUE;

			// Create device
			device = builder
					.queue(new RequiredQueue(family, List.of(Percentile.HALF, Percentile.ONE)))
					.extension("ext")
					.layer(ValidationLayer.STANDARD_VALIDATION)
					.features(DeviceFeatures.of(required))
					.build();

			// Check device
			assertNotNull(device);
			assertEquals(new Handle(1), device.handle());
			assertEquals(false, device.isDestroyed());
			assertEquals(parent, device.parent());
			assertEquals(Map.of(family, List.of(queue, queue)), device.queues());

			// Init expected descriptor
			final var expected = new VkDeviceCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var actual = (VkDeviceCreateInfo) obj;
					assertEquals(0, actual.flags);

					// Check device features
					assertNotNull(actual.pEnabledFeatures);
					assertEquals(VulkanBoolean.TRUE, actual.pEnabledFeatures.samplerAnisotropy);

					// Check extensions
					assertEquals(1, actual.enabledExtensionCount);
					assertNotNull(actual.ppEnabledExtensionNames);

					// Check validation layers
					assertEquals(1, actual.enabledLayerCount);
					assertNotNull(actual.ppEnabledLayerNames);

					// Check required queues
					assertEquals(1, actual.queueCreateInfoCount);
					assertEquals(0, actual.pQueueCreateInfos.flags);
					assertEquals(2, actual.pQueueCreateInfos.queueCount);
					assertEquals(1, actual.pQueueCreateInfos.queueFamilyIndex);
					assertNotNull(actual.pQueueCreateInfos.pQueuePriorities);

					return true;
				}
			};

			// Check API
			verify(lib).vkCreateDevice(parent, expected, null, ref);
		}

		@DisplayName("Cannot request a queue from a different device")
		@Test
		void invalidQueueFamily() {
			final Family other = new Family(3, 4, Set.of());
			assertThrows(IllegalArgumentException.class, () -> builder.queue(new RequiredQueue(other)));
		}

		@DisplayName("Cannot request an extension that is not available")
		@Test
		void invalidExtension() {
			assertThrows(IllegalArgumentException.class, () -> builder.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS));
		}
	}
}
