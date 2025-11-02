package org.sarge.jove.platform.vulkan.common;

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
		final var other = new DeviceFeatures(Set.of("wideLines", "depthClamp"));
		assertEquals(true, features.contains(features));
		assertEquals(true, other.contains(features));
		assertEquals(false, features.contains(other));
	}

	@Test
	void build() {
		final VkPhysicalDeviceFeatures structure = features.build();
		assertEquals(1, structure.wideLines);
		assertEquals(0, structure.depthClamp);
	}

	@Test
	void unknown() {
		final var invalid = new DeviceFeatures(Set.of("cobblers"));
		assertThrows(IllegalArgumentException.class, () -> invalid.build());
	}

	@Test
	void of() {
		final var structure = new VkPhysicalDeviceFeatures();
		structure.wideLines = 1;
		assertEquals(features, DeviceFeatures.of(structure));
	}
}
