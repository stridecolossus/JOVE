package org.sarge.jove.platform.vulkan.memory;

import java.lang.foreign.Arena;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockDeviceMemory extends DefaultDeviceMemory {
	private static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryProperty.HOST_VISIBLE));

	public MockDeviceMemory() {
		this(TYPE);
	}

	public MockDeviceMemory(MemoryType type) {
		final var allocator = Arena.ofAuto();
		final var memory = allocator.allocate(2);
		super(new Handle(memory), new MockLogicalDevice(), type, 2);
	}
}

//
//	@Override
//	public Region map(long offset, long size) {
//		return new Region() {
//			@Override
//			public long size() {
//				return size;
//			}
//
//			@Override
//			public MemorySegment segment(long offset, long size) {
//				return null;
//			}
//
////			@Override
////			public ByteBuffer buffer(long offset, long size) {
////				return BufferHelper.allocate((int) size);
////			}
//
//			@Override
//			public void unmap() {
//				// Whatever
//			}
//		};
//	}
//}
