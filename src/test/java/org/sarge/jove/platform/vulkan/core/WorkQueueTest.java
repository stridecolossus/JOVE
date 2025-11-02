package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.EnumMask;

class WorkQueueTest {
	private Family family;

	@BeforeEach
	void before() {
		family = new Family(1, 2, Set.of(VkQueueFlag.GRAPHICS));
	}

	@Test
	void of() {
		final var properties = new VkQueueFamilyProperties();
		properties.queueCount = 2;
		properties.queueFlags = new EnumMask<>(VkQueueFlag.GRAPHICS);
		assertEquals(family, Family.of(1, properties));
	}
}
