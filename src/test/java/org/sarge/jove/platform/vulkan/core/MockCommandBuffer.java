package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;

public class MockCommandBuffer extends Command.Buffer {
	public MockCommandBuffer() {
		super(new Handle(1), new MockCommandPool(), true);
		begin();
		end();
	}
}
