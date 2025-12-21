package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.core.DeviceLimits;

class DeviceLimitsTest {
	private DeviceLimits limits;

	@BeforeEach
	void before() {
		final var structure = new VkPhysicalDeviceLimits();
		structure.bufferImageGranularity = 1024L;
		limits = new DeviceLimits(structure);
	}

	@Test
	void get() {
		assertEquals(1024L, (long) limits.get("bufferImageGranularity"));
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> limits.get("cobblers"));
	}
}
