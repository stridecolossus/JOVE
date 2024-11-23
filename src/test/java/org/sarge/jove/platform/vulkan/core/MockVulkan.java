package org.sarge.jove.platform.vulkan.core;

import static org.mockito.Mockito.mock;

import org.sarge.jove.foreign.*;

public class MockVulkan extends Vulkan {
	public MockVulkan() {
		super(
				mock(VulkanLibrary.class),
				NativeMapperRegistry.create(),
				new MockReferenceFactory()
		);
	}

	@Override
	public MockReferenceFactory factory() {
		return (MockReferenceFactory) super.factory();
	}
}
