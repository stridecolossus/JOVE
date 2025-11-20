package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;

public class MockFence extends Fence {
	public int reset;
	public int wait;

	public MockFence() {
		final var device = new MockLogicalDevice();
		super(new Handle(3), device);
	}

	@Override
	public void reset() {
		++reset;
	}

	@Override
	public void waitReady() {
		++wait;
	}
}
