package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.util.MockReferenceFactory;

import com.sun.jna.Pointer;

public class PhysicalDeviceTest {
	private PhysicalDevice dev;
	private VulkanLibrary lib;
	private Instance instance;

	@BeforeEach
	void before() {
		// Create Vulkan
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create an instance
		instance = mock(Instance.class);
		when(instance.library()).thenReturn(lib);

		// Create a queue family
		final VkQueueFamilyProperties family = new VkQueueFamilyProperties();
		family.queueCount = 1;
		family.queueFlags = IntegerEnumeration.mask(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT);

		// Create device
		dev = new PhysicalDevice(new Pointer(42), instance, new VkQueueFamilyProperties[]{family});
	}

	@Test
	void constructor() {
		assertNotNull(dev.handle());
		assertEquals(instance, dev.instance());
		assertNotNull(dev.families());
		assertEquals(1, dev.families().size());
	}

	@Test
	void support() {
		assertNotNull(dev.extensions());
		assertNotNull(dev.layers());
	}

	@Test
	void properties() {
		final var props = dev.properties();
		verify(lib).vkGetPhysicalDeviceProperties(dev.handle(), props);
	}

	@Test
	void features() {
		final var features = dev.features();
		verify(lib).vkGetPhysicalDeviceFeatures(dev.handle(), features);
	}

	@Test
	void memory() {
		final var mem = dev.memory();
		verify(lib).vkGetPhysicalDeviceMemoryProperties(dev.handle(), mem);
	}

	@Test
	void findMemoryType() {
		// Create a memory type
		final Set<VkMemoryPropertyFlag> flags = Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		final VkMemoryType type = new VkMemoryType();
		type.propertyFlags = IntegerEnumeration.mask(flags);

		// Create device memory properties
		final var props = new VkPhysicalDeviceMemoryProperties();
		props.memoryTypeCount = 2;
		props.memoryTypes = new VkMemoryType[]{new VkMemoryType(), type};

		// Mock memory properties
		final PhysicalDevice spy = spy(dev);
		doReturn(props).when(spy).memory();

		// Check memory type matched
		assertEquals(1, spy.findMemoryType(0b11, flags));

		// Check bit-wise filter
		assertThrows(ServiceException.class, () -> dev.findMemoryType(0b01, flags));

		// Check property matching
		assertThrows(ServiceException.class, () -> dev.findMemoryType(0b11, Set.of(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)));
		assertThrows(ServiceException.class, () -> dev.findMemoryType(0b11, Set.of()));
	}

	@Nested
	class FamilyTests {
		private QueueFamily family;

		@BeforeEach
		void before() {
			family = dev.families().get(0);
		}

		@Test
		void constructor() {
			assertNotNull(family);
			assertEquals(1, family.count());
			assertEquals(Set.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT), family.flags());
			assertEquals(0, family.index());
			assertEquals(dev, family.device());
		}

		@Test
		void equals() {
			assertEquals(true, family.equals(family));
			assertEquals(false, family.equals(null));
			assertEquals(false, family.equals(mock(QueueFamily.class)));
		}

		@Test
		void isPresentationSupported() {
			final Handle surface = new Handle(new Pointer(42));
			assertEquals(true, family.isPresentationSupported(surface));
			final var ref = lib.factory().integer(); // TODO - fails with NPE unless we do it this way! why?
			verify(lib).vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), 0, surface, ref);
		}

		@Test
		void presentationPredicate() {
			final Handle surface = new Handle(new Pointer(42));
			final var predicate = PhysicalDevice.predicatePresentationSupported(surface);
			assertTrue(predicate.test(dev));
		}

		@Test
		void flagsPredicate() {
			assertTrue(PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT, VkQueueFlag.VK_QUEUE_COMPUTE_BIT).test(family));
			assertFalse(PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT).test(family));
		}

		@SuppressWarnings("unchecked")
		@Test
		void devicePredicate() {
			// Create a device predicate from a family delegate predicate
			final Predicate<QueueFamily> delegate = mock(Predicate.class);
			final var predicate = PhysicalDevice.predicate(delegate);
			assertNotNull(predicate);
			assertEquals(false, predicate.test(dev));

			// Check delegate
			when(delegate.test(family)).thenReturn(true);
			assertEquals(true, predicate.test(dev));
		}

		@Test
		void find() {
			final Predicate<QueueFamily> predicate = queue -> queue == family;
			assertEquals(family, dev.find(predicate, null));
		}

		@SuppressWarnings("unchecked")
		@Test
		void findThrows() {
			assertThrows(ServiceException.class, () -> dev.find(mock(Predicate.class), "doh"));
		}
	}
}
