package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

public class MockShader extends Shader {
	public MockShader(LogicalDevice device) {
		super(new Handle(7), device);
	}
}
