package org.sarge.jove.platform.vulkan.core;

import static org.mockito.Mockito.mock;

import org.sarge.jove.lib.*;

public class MockVulkan extends Vulkan {
	public MockVulkan() {
		super(
				mock(VulkanLibraryTEMP.class),
				NativeMapperRegistry.create(),
				new MockReferenceFactory()
		);
	}

	@Override
	public MockReferenceFactory factory() {
		return (MockReferenceFactory) super.factory();
	}
}
