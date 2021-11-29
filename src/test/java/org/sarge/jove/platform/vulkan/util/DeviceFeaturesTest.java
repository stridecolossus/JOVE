package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class DeviceFeaturesTest {
	private static final String SUPPORTED = "samplerAnisotropy";

	@Test
	void populate() {
		final DeviceFeatures required = DeviceFeatures.of(List.of(SUPPORTED));
		final VkPhysicalDeviceFeatures struct = DeviceFeatures.populate(required);
		assertNotNull(struct);
		assertEquals(VulkanBoolean.TRUE, struct.samplerAnisotropy);
	}

	@Test
	void populateEmpty() {
		assertEquals(null, DeviceFeatures.populate(null));
	}

	@Nested
	class RequiredFeaturesTest {
		private DeviceFeatures required;

		@BeforeEach
		void before() {
			required = DeviceFeatures.of(List.of(SUPPORTED));
		}

		@Test
		void features() {
			assertEquals(List.of(SUPPORTED), required.features());
		}

		@Test
		void contains() {
			assertEquals(true, required.contains(required));
			assertEquals(false, required.contains(DeviceFeatures.of(List.of("other"))));
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
		void contains() {
			final var other = new VkPhysicalDeviceFeatures();
			other.wideLines = VulkanBoolean.TRUE;
			assertEquals(true, supported.contains(supported));
			assertEquals(true, supported.contains(DeviceFeatures.of(struct)));
			assertEquals(false, supported.contains(DeviceFeatures.of(other)));
		}
	}
}
