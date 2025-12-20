package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.ShaderTest.MockShaderLibrary;

public class MockShader extends Shader {
	public MockShader() {
		super(new Handle(7), new MockLogicalDevice(new MockShaderLibrary()));
	}
}
