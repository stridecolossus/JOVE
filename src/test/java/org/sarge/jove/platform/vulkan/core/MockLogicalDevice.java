package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.Mockery;

public class MockLogicalDevice extends LogicalDevice {
	public final VkPhysicalDeviceLimits limits;

	private final Object library;

	public MockLogicalDevice() {
		this(null);
	}

	public MockLogicalDevice(Object library) {
		final var family = new Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(2), family);
		final LogicalDevice.Library logical = new Mockery(LogicalDevice.Library.class).proxy();

		final var limits = new VkPhysicalDeviceLimits();
		limits.bufferImageGranularity = 1024;
		limits.maxMemoryAllocationCount = Integer.MAX_VALUE;
		limits.maxDrawIndexedIndexValue = Integer.MAX_VALUE;
		limits.maxUniformBufferRange = Integer.MAX_VALUE;
		limits.maxPushConstantsSize = 256;

		super(new Handle(1), Map.of(family, List.of(queue)), new DeviceFeatures(Set.of()), new DeviceLimits(limits), logical);
		this.library = Objects.requireNonNullElse(library, logical);
		this.limits = limits;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T library() {
		return (T) library;
	}

	@Override
	public DeviceLimits limits() {
		return new DeviceLimits(limits);
	}
}
