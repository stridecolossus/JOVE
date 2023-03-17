package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;

public class MockFence implements Fence {
	public boolean signalled;

	@Override
	public Handle handle() {
		return new Handle(1);
	}

	@Override
	public void waitReady() {
		signalled = false;
	}

	@Override
	public void reset() {
		signalled = false;
	}

	@Override
	public boolean signalled() {
		return signalled;
	}

	@Override
	public void destroy() {
		// Ignored
	}
}
