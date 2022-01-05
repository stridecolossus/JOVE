package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class DeviceFeaturesTest {
	private static final String SUPPORTED = "samplerAnisotropy";

	private VkPhysicalDeviceFeatures struct;

	@BeforeEach
	void before() {
		struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = VulkanBoolean.TRUE;
	}

	@Nested
	class EmptyFeaturesTest {
		@Test
		void features() {
			assertEquals(Set.of(), DeviceFeatures.EMPTY.features());
		}

		@Test
		void descriptor() {
			assertNull(DeviceFeatures.EMPTY.descriptor());
		}

		@Test
		void contains() {
			assertEquals(true, DeviceFeatures.EMPTY.contains(DeviceFeatures.EMPTY));
			assertEquals(false, DeviceFeatures.EMPTY.contains(mock(DeviceFeatures.class)));
		}
	}

	@Nested
	class RequiredFeaturesTest {
		private DeviceFeatures required;

		@BeforeEach
		void before() {
			required = DeviceFeatures.of(Set.of(SUPPORTED));
		}

		@Test
		void features() {
			assertEquals(Set.of(SUPPORTED), required.features());
		}

		@Test
		void descriptor() {
			final VkPhysicalDeviceFeatures descriptor = required.descriptor();
			assertNotNull(descriptor);
			assertEquals(VulkanBoolean.TRUE, descriptor.samplerAnisotropy);
		}

		@Test
		void contains() {
			assertEquals(true, required.contains(required));
			assertEquals(true, required.contains(DeviceFeatures.of(Set.of(SUPPORTED))));
			assertEquals(true, required.contains(DeviceFeatures.of(struct)));
			assertEquals(true, required.contains(DeviceFeatures.of(Set.of())));
			assertEquals(false, required.contains(DeviceFeatures.of(Set.of("wideLines"))));
		}
	}

	@Nested
	class SupportedFeaturesTest {
		private DeviceFeatures supported;
		private VkPhysicalDeviceFeatures struct;

		@BeforeEach
		void before() {
			struct = new VkPhysicalDeviceFeatures();
			struct.samplerAnisotropy = VulkanBoolean.TRUE;
			supported = DeviceFeatures.of(struct);
		}

		@Test
		void features() {
			assertEquals(Set.of(SUPPORTED), supported.features());
		}

		@Test
		void descriptor() {
			final VkPhysicalDeviceFeatures descriptor = supported.descriptor();
			assertNotNull(descriptor);
			assertEquals(VulkanBoolean.TRUE, descriptor.samplerAnisotropy);
		}

		@Test
		void contains() {
			assertEquals(true, supported.contains(supported));
			assertEquals(true, supported.contains(DeviceFeatures.of(struct)));
			assertEquals(true, supported.contains(DeviceFeatures.of(Set.of(SUPPORTED))));
			assertEquals(true, supported.contains(DeviceFeatures.of(new VkPhysicalDeviceFeatures())));
			assertEquals(false, supported.contains(DeviceFeatures.of(Set.of("wideLines"))));
		}
	}
}
