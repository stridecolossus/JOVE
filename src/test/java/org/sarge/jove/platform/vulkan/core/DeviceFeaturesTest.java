package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

class DeviceFeaturesTest {
	private DeviceFeatures features;

	@BeforeEach
	void before() {
		features = new DeviceFeatures(Set.of("wideLines"));
	}

	@Test
	void contains() {
		assertTrue(features.contains("wideLines"));
		assertFalse(features.contains("depthClamp"));
	}

	@Test
	void other() {
		final var other = new DeviceFeatures(Set.of("wideLines", "depthClamp"));
		assertEquals(true, features.contains(features));
		assertEquals(true, other.contains(features));
		assertEquals(false, features.contains(other));
	}

	@Test
	void build() {
		final VkPhysicalDeviceFeatures structure = features.build();
		assertEquals(true, structure.wideLines);
		assertEquals(false, structure.depthClamp);
	}

	@Test
	void unknown() {
		final var invalid = new DeviceFeatures(Set.of("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> invalid.build());
	}

	@Test
	void of() {
		final var structure = new VkPhysicalDeviceFeatures();
		structure.wideLines = true;
		assertEquals(features, DeviceFeatures.of(structure));
	}

	@Test
	void filter() {
		final var device = new MockPhysicalDevice() {
			@Override
			public DeviceFeatures features() {
				return features;
			}
		};
		assertTrue(features.test(device));
		assertFalse(features.test(new MockPhysicalDevice()));
	}
}
