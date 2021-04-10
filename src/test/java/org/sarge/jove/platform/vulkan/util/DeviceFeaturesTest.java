package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

public class DeviceFeaturesTest {
	private static final String SUPPORTED = "samplerAnisotropy";
	private static final String NOT_SUPPORTED = "wideLines";

	private DeviceFeatures features;

	@BeforeEach
	void before() {
		final var struct = new VkPhysicalDeviceFeatures();
		struct.samplerAnisotropy = VulkanBoolean.TRUE;
		features = new DeviceFeatures(struct);
	}

	@Test
	void isSupported() {
		assertEquals(true, features.isSupported(SUPPORTED));
		assertEquals(false, features.isSupported(NOT_SUPPORTED));
	}

	@Test
	void isSupportedInvalidField() {
		assertThrows(IllegalArgumentException.class, () -> features.isSupported("cobblers"));
	}

	@Test
	void check() {
		final var required = new VkPhysicalDeviceFeatures();
		required.samplerAnisotropy = VulkanBoolean.TRUE;
		features.check(required);
	}

	@Test
	void checkEmpty() {
		features.check(new VkPhysicalDeviceFeatures());
	}

	@Test
	void checkNotSupported() {
		final var required = new VkPhysicalDeviceFeatures();
		required.wideLines = VulkanBoolean.TRUE;
		assertThrows(IllegalArgumentException.class, () -> features.check(required));
	}

	@SuppressWarnings("static-method")
	@Test
	void of() {
		final DeviceFeatures other = DeviceFeatures.of(Set.of(SUPPORTED));
		assertEquals(true, other.isSupported(SUPPORTED));
		assertEquals(false, other.isSupported(NOT_SUPPORTED));
	}
}
