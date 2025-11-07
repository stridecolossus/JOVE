package org.sarge.jove.platform.vulkan.memory;

import java.nio.ByteBuffer;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockDeviceMemory extends DefaultDeviceMemory {
	private static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryProperty.HOST_VISIBLE));

	public MockDeviceMemory() {
		this(TYPE);
	}

	public MockDeviceMemory(MemoryType type) {
		super(new Handle(1), new MockLogicalDevice(), type, 2);
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
				// Whatever
			}
		};
	}
}
