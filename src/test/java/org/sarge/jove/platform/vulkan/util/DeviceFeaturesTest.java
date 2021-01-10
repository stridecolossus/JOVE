package org.sarge.jove.platform.vulkan.util;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;

public class DeviceFeaturesTest {
	private static final String SUPPORTED = "samplerAnisotropy";
	private static final String NOT_SUPPORTED = "wideLines";

	private DeviceFeatures features;

	@BeforeEach
	void before() {
		features = DeviceFeatures.of(Set.of(SUPPORTED));
	}

	@Test
	void test() {

		System.out.println("features="+features);

		features.check(new DeviceFeatures(new VkPhysicalDeviceFeatures()));


	}

	/*
	@Test
	void constructor() {
		assertNotNull(features.get());
		assertEquals(VulkanBoolean.TRUE, features.get().samplerAnisotropy);
	}

	@Test
	void isSupported() {
		assertEquals(true, features.isSupported(SUPPORTED));
		assertEquals(false, features.isSupported(NOT_SUPPORTED));
	}

	@Test
	void isSupportedUnknown() {
		assertThrows(IllegalArgumentException.class, () -> features.isSupported("cobblers"));
	}

	@Test
	void check() {
//		features.check(SUPPORTED);
//		features.check(features);
//		features.check(new DeviceFeatures(new VkPhysicalDeviceFeatures()));
	}

	@Test
	void checkNotSupported() {
		assertThrows(IllegalStateException.class, "Unsupported feature: " + NOT_SUPPORTED, () -> features.check(NOT_SUPPORTED));
	}

	@Test
	void checkMismatch() {
		final var required = new VkPhysicalDeviceFeatures();
		required.wideLines = VulkanBoolean.TRUE;
		assertThrows(IllegalStateException.class, "Unsupported feature(s): [wideLines]", () -> features.check(new DeviceFeatures(required)));
	}
	*/
}
