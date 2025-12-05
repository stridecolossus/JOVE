package org.sarge.jove.platform.vulkan.memory;

import java.lang.foreign.Arena;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MockDeviceMemory extends DefaultDeviceMemory {
	private static final MemoryType TYPE = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryPropertyFlags.HOST_VISIBLE));

	public MockDeviceMemory(long size) {
		final var allocator = Arena.ofAuto();
		final var memory = allocator.allocate(size);
		super(new Handle(memory), new MockLogicalDevice(new MockMemoryLibrary()), TYPE, size);
	}
}
