package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.Mockery;

public class MockPhysicalDevice extends PhysicalDevice {
	public static final Family FAMILY = new Family(0, 1, Set.of());

	public MockPhysicalDevice() {
		final var mock = new Mockery(PhysicalDevice.Library.class);
		super(new Handle(1), List.of(FAMILY), mock.proxy());
	}

	@Override
	public VkPhysicalDeviceProperties properties() {
		final var properties = new VkPhysicalDeviceProperties();
		properties.limits = new VkPhysicalDeviceLimits();
		properties.limits.maxSamplerAnisotropy = 8;
		// TODO - others?
		return properties;
	}
}
