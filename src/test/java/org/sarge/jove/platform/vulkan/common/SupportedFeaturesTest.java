package org.sarge.jove.platform.vulkan.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class SupportedFeaturesTest {
	private SupportedFeatures features;

	@BeforeEach
	void before() {
		final var struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = true;
		features = new SupportedFeatures(struct);
	}

	@Test
	void features() {
		assertEquals(Set.of("samplerAnisotropy"), features.features());
	}

	@Test
	void isEnabled() {
		assertEquals(true, features.isEnabled("samplerAnisotropy"));
		assertEquals(false, features.isEnabled("wideLines"));
	}

	@Test
	void contains() {
		final var required = new DeviceFeatures(Set.of("samplerAnisotropy"));
		assertEquals(true, features.contains(required));
		assertEquals(true, features.contains(new DeviceFeatures(Set.of())));
		assertEquals(false, features.contains(new DeviceFeatures(Set.of("cobblers"))));
	}
}
