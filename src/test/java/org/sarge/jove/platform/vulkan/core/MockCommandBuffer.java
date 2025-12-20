package org.sarge.jove.platform.vulkan.core;

import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

public class MockCommandBuffer extends Command.Buffer {
	public MockCommandBuffer() {
		super(new Handle(1), new MockCommandPool(), true);
	}

	@Override
	public Buffer begin(VkCommandBufferInheritanceInfo inheritance, Set<VkCommandBufferUsageFlags> flags) {
		stage(Stage.RECORDING);
		return this;
	}

	@Override
	public Buffer end() {
		stage(Stage.EXECUTABLE);
		return this;
	}
}
