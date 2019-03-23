package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

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
}
