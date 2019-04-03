package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;

import com.sun.jna.Pointer;

public class PhysicalDeviceTest extends AbstractVulkanTest {
	private Pointer handle;
	private PhysicalDevice dev;
	private VkQueueFamilyProperties familyProps;

	@BeforeEach
	public void before() {
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

		// Create memory type
		final VkMemoryType type = new VkMemoryType();
		type.propertyFlags = VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT.value();

		// Create device memory properties
		final VkPhysicalDeviceMemoryProperties mem = new VkPhysicalDeviceMemoryProperties();
		mem.memoryTypeCount = 1;
		mem.memoryTypes = new VkMemoryType[]{type};

		// Create device
		handle = mock(Pointer.class);
		dev = new PhysicalDevice(handle, vulkan, props, mem, features, List.of(familyProps), mock(Supported.class));
	}

	@Test
	public void constructor() {
		assertEquals(handle, dev.handle());
		assertEquals(VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU, dev.type());
		assertNotNull(dev.properties());
		assertNotNull(dev.families());
		assertNotNull(dev.supported());
		assertNotNull(dev.families());
		assertEquals(1, dev.families().size());
	}

	@Test
	public void findMemoryType() {
		assertEquals(0, dev.findMemoryType(Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT)));
	}

	@Test
	public void findMemoryTypeUnsupported() {
		assertThrows(ServiceException.class, () -> dev.findMemoryType(Set.of()));
		assertThrows(ServiceException.class, () -> dev.findMemoryType(Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT)));
		assertThrows(ServiceException.class, () -> dev.findMemoryType(Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT, VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)));
	}

	@Nested
	class FeatureTests {
		@Test
		public void enumerateUnsupportedFeaturesEmpty() {
			assertEquals(Set.of(), dev.enumerateUnsupportedFeatures(new VkPhysicalDeviceFeatures()));
		}

		@Test
		public void enumerateUnsupportedFeaturesSame() {
			final VkPhysicalDeviceFeatures required = new VkPhysicalDeviceFeatures();
			required.geometryShader = VulkanBoolean.TRUE;
			assertEquals(Set.of(), dev.enumerateUnsupportedFeatures(required));
		}

		@Test
		public void enumerateUnsupportedFeatures() {
			final VkPhysicalDeviceFeatures required = new VkPhysicalDeviceFeatures();
			required.geometryShader = VulkanBoolean.TRUE;
			required.tessellationShader = VulkanBoolean.TRUE;
			assertEquals(Set.of("tessellationShader"), dev.enumerateUnsupportedFeatures(required));
		}
	}

	@Nested
	class QueueFamilyTests {
		private QueueFamily family;

		@BeforeEach
		public void before() {
			family = dev.families().iterator().next();
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
