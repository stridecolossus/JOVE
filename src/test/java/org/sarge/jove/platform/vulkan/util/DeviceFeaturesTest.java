package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class DeviceFeaturesTest {
	private static final String SUPPORTED = "samplerAnisotropy";
	private static final String OTHER = "wideLines";

	private DeviceFeatures features;
	private VkPhysicalDeviceFeatures struct;

	@BeforeEach
	void before() {
		struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = VulkanBoolean.TRUE;
		features = new DeviceFeatures(struct);
	}

	@Test
	void contains() {
		assertEquals(true, features.contains(SUPPORTED));
		assertEquals(false, features.contains(OTHER));
	}

	@Test
	void containsInvalidField() {
		assertThrows(IllegalArgumentException.class, () -> features.contains("cobblers"));
	}

	@Test
	void containSet() {
		assertEquals(true, features.contains(Set.of(SUPPORTED)));
		assertEquals(false, features.contains(Set.of(OTHER)));
		assertEquals(false, features.contains(Set.of(SUPPORTED, OTHER)));
	}

	@Test
	void missing() {
		assertEquals(Set.of(), features.missing(Set.of(SUPPORTED)));
		assertEquals(Set.of(OTHER), features.missing(Set.of(OTHER)));
		assertEquals(Set.of(OTHER), features.missing(Set.of(SUPPORTED, OTHER)));
	}

	@Test
	void equals() {
		assertEquals(true, features.equals(features));
		assertEquals(true, features.equals(new DeviceFeatures(struct)));
		assertEquals(false, features.equals(null));
		assertEquals(false, features.equals(new DeviceFeatures(new VkPhysicalDeviceFeatures())));
	}
}
