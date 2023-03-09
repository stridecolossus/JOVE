package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;

public class DeviceLimitsTest {
	private DeviceLimits limits;
	private VkPhysicalDeviceLimits struct;

	@BeforeEach
	void before() {
		struct = new VkPhysicalDeviceLimits();
		limits = new DeviceLimits(struct);
	}

	@DisplayName("A device limit can be queried by name")
	@Test
	void value() {
		struct.bufferImageGranularity = 123;
		struct.write();
		assertEquals(123L, (long) limits.value("bufferImageGranularity"));
	}

	@DisplayName("A quantisied device limit range can be queried by name")
	@Test
	void range() {
		struct.pointSizeRange = new float[]{1, 5};
		struct.pointSizeGranularity = 2;
		struct.write();
		assertArrayEquals(new float[]{1, 3, 5}, limits.range("pointSizeRange", "pointSizeGranularity"));
	}
}
