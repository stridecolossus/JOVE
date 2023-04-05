package org.sarge.jove.platform.vulkan.common;

import static org.mockito.Mockito.mock;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.*;

public class MockDeviceContext implements DeviceContext {
	private final VulkanLibrary lib = mock(VulkanLibrary.class);
	private final ReferenceFactory factory = new MockReferenceFactory();
	private final DeviceFeatures features = mock(DeviceFeatures.class);
	private final VkPhysicalDeviceLimits limits = new VkPhysicalDeviceLimits();

	@Override
	public Handle handle() {
		return new Handle(1);
	}

	@Override
	public VulkanLibrary library() {
		return lib;
	}

	@Override
	public ReferenceFactory factory() {
		return factory;
	}

	@Override
	public DeviceFeatures features() {
		return features;
	}

	@Override
	public VkPhysicalDeviceLimits limits() {
		return limits;
	}

	@Override
	public void waitIdle() {
		// Does nowt
	}
}
