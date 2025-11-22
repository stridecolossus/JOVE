package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

public class MockLogicalDevice extends LogicalDevice {
	public MockLogicalDevice() {
		this(new MockVulkanLibrary());
	}

	public MockLogicalDevice(MockVulkanLibrary library) {
		final var family = new Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(2), family);

		final var limits = new VkPhysicalDeviceLimits();
		limits.maxMemoryAllocationCount = -1;
		limits.bufferImageGranularity = 1024;
		limits.maxPushConstantsSize = 256;

		super(new Handle(1), Map.of(family, List.of(queue)), limits, library);
	}
}
