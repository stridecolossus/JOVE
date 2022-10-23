package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;

public class DeviceLimitsTest {
	private DeviceLimits limits;
	private VkPhysicalDeviceLimits struct;
	private DeviceFeatures features;

	@BeforeEach
	void before() {
		struct = new VkPhysicalDeviceLimits();
		features = DeviceFeatures.required(Set.of("feature"));
		limits = new DeviceLimits(struct, features);
	}

	@DisplayName("An optional device feature can be required by the application")
	@Test
	void require() {
		limits.require("feature");
	}

	@DisplayName("An unsupported device feature fails if required by the application")
	@Test
	void unsupported() {
		assertThrows(IllegalStateException.class, () -> limits.require("unsupported"));
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
