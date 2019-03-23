package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily.Entry;

import com.sun.jna.Pointer;

public class PhysicalDeviceTest extends AbstractVulkanTest {
	private Pointer handle;
	private PhysicalDevice device;
	private VulkanInstance instance;
	private VkQueueFamilyProperties familyProps;

	@BeforeEach
	public void before() {
		// Create parent instance
		instance = mock(VulkanInstance.class);

		// Create device properties
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		props.deviceType = VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;

		// Create a queue family
		familyProps = new VkQueueFamilyProperties();
		familyProps.queueCount = 2;
		familyProps.queueFlags = IntegerEnumeration.mask(Set.of(VkQueueFlag.VK_QUEUE_COMPUTE_BIT));

		// Create device features
		final var features = new VkPhysicalDeviceFeatures();
		features.geometryShader = VulkanBoolean.TRUE;

		// Create device
		handle = mock(Pointer.class);
		device = new PhysicalDevice(handle, instance, props, features, List.of(familyProps), mock(Supported.class));
	}

	@Test
	public void constructor() {
		assertEquals(handle, device.handle());
		assertEquals(VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, device.type());
		assertNotNull(device.properties());
		assertNotNull(device.families());
		assertNotNull(device.supported());
		assertNotNull(device.families());
		assertEquals(1, device.families().size());
	}

	@Nested
	class FeatureTests {
		@Test
		public void enumerateUnsupportedFeaturesEmpty() {
			assertEquals(Set.of(), device.enumerateUnsupportedFeatures(new VkPhysicalDeviceFeatures()));
		}

		@Test
		public void enumerateUnsupportedFeaturesSame() {
			final VkPhysicalDeviceFeatures required = new VkPhysicalDeviceFeatures();
			required.geometryShader = VulkanBoolean.TRUE;
			assertEquals(Set.of(), device.enumerateUnsupportedFeatures(required));
		}

		@Test
		public void enumerateUnsupportedFeatures() {
			final VkPhysicalDeviceFeatures required = new VkPhysicalDeviceFeatures();
			required.geometryShader = VulkanBoolean.TRUE;
			required.tessellationShader = VulkanBoolean.TRUE;
			assertEquals(Set.of("tessellationShader"), device.enumerateUnsupportedFeatures(required));
		}
	}

	@Nested
	class QueueFamilyTests {
		private QueueFamily family;

		@BeforeEach
		public void before() {
			family = device.families().iterator().next();
		}

		@Test
		public void family() {
			assertEquals(2, family.count());
			assertEquals(Set.of(VkQueueFlag.VK_QUEUE_COMPUTE_BIT), family.flags());
		}

		@Test
		public void isPresentationSupported() {
			final Surface surface = mock(Surface.class);
			when(surface.handle()).thenReturn(mock(Pointer.class));
			family.isPresentationSupported(surface);
		}
	}

	@Nested
	class QueueEntryTests {
		private QueueFamily family;

		@BeforeEach
		public void before() {
			family = device.families().iterator().next();
		}

		@Test
		public void queue() {
			final Entry entry = family.queue();
			assertNotNull(entry);
			assertFloatArrayEquals(new float[]{1}, entry.priorities());
			assertEquals(family, entry.family());
		}

		@Test
		public void multiple() {
			final Entry entry = family.queues(2);
			assertNotNull(entry);
			assertFloatArrayEquals(new float[]{1, 1}, entry.priorities());
			assertEquals(family, entry.family());
		}

		@Test
		public void multiplePrioritised() {
			final Entry entry = family.queues(2, 0.5f);
			assertNotNull(entry);
			assertFloatArrayEquals(new float[]{0.5f, 0.5f}, entry.priorities());
			assertEquals(family, entry.family());
		}

		@Test
		public void priorities() {
			final Entry entry = family.queues(0.5f);
			assertNotNull(entry);
			assertFloatArrayEquals(new float[]{0.5f}, entry.priorities());
			assertEquals(family, entry.family());
		}

		@Test
		public void multipleTooMany() {
			assertThrows(IllegalArgumentException.class, () -> family.queues(3));
		}

		@Test
		public void invalidPriority() {
			assertThrows(IllegalArgumentException.class, () -> family.queues(1, 999));
		}
	}
}
