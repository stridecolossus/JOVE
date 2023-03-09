package org.sarge.jove.platform.vulkan.common;

import static org.mockito.Mockito.mock;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.*;

public class MockDeviceContext implements DeviceContext {
	private final VulkanLibrary lib = mock(VulkanLibrary.class);
	private final ReferenceFactory factory = new MockReferenceFactory();
	private final RequiredFeatures features = mock(RequiredFeatures.class);
	private final DeviceLimits limits = mock(DeviceLimits.class);

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
	public RequiredFeatures features() {
		return features;
	}

	@Override
	public DeviceLimits limits() {
		return limits;
	}
}
