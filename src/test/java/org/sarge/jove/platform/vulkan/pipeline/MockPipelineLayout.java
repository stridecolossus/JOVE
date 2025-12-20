package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayoutTest.MockPipelineLayoutLibrary;

public class MockPipelineLayout extends PipelineLayout {
	public MockPipelineLayout() {
		super(new Handle(3), new MockLogicalDevice(new MockPipelineLayoutLibrary()), null);
	}
}
