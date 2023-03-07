package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Handle;
import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;

public class MockDeviceMemory extends DefaultDeviceMemory {
	public MockDeviceMemory() {
		super(new Handle(1), new MockDeviceContext(), 2);
	}

	@Override
	public Region map(long offset, long size) {
		return new Region() {
			@Override
			public long size() {
				return size;
			}

			@Override
			public ByteBuffer buffer(long offset, long size) {
				return BufferHelper.allocate((int) size);
			}

			@Override
			public void unmap() {
			}
		};
	}
}
