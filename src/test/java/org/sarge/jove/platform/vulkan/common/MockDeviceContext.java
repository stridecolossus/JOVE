package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.Handle;

public class MockDeviceContext implements DeviceContext {
	private final Object library;

	public MockDeviceContext(Object library) {
		this.library = library;
	}

	@Override
	public Handle handle() {
		return new Handle(1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T library() {
		return (T) library;
	}
}
