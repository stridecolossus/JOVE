package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.util.Mockery;

public class MockFence extends Fence {
	public int reset;
	public int wait;

	public MockFence() {
		final var device = new MockLogicalDevice(new Mockery(Fence.Library.class).proxy());
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
