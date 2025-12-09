package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

public class MockPipelineLayout extends PipelineLayout {
	public MockPipelineLayout(LogicalDevice device) {
		super(new Handle(3), device, null);
	}
}
