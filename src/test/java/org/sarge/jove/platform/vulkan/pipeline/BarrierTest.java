package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.platform.vulkan.VkAccessFlags.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStageFlags.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.Barrier.BarrierType.MemoryBarrier;
import org.sarge.jove.util.EnumMask;

class BarrierTest {
	private static class MockPipelineLibrary extends MockVulkanLibrary {
		boolean executed;

		@Override
		public void vkCmdPipelineBarrier(
				Buffer commandBuffer,
				EnumMask<VkPipelineStageFlags> srcStageMask, EnumMask<VkPipelineStageFlags> dstStageMask,
				EnumMask<VkDependencyFlags> dependencyFlags,
				int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers,
				int bufferMemoryBarrierCount,VkBufferMemoryBarrier[] pBufferMemoryBarriers,
				int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers )
		{
			assertEquals(new EnumMask<>(TOP_OF_PIPE), srcStageMask);
			assertEquals(new EnumMask<>(TRANSFER), dstStageMask);
			assertEquals(new EnumMask<>(VkDependencyFlags.DEVICE_GROUP), dependencyFlags);
			assertEquals(1, memoryBarrierCount);
			assertEquals(1, pMemoryBarriers.length);
			executed = true;
		}
	}

	@Test
	void builder() {
		final var library = new MockPipelineLibrary();
		final var device = new MockLogicalDevice(library);

		final Command barrier = new Barrier.Builder()
        		.source(TOP_OF_PIPE)
        		.destination(TRANSFER)
        		.flag(VkDependencyFlags.DEVICE_GROUP)
        		.add(Set.of(MEMORY_READ), Set.of(MEMORY_WRITE), new MemoryBarrier())
				.build(device);

		barrier.execute(null);

		assertEquals(true, library.executed);
	}

	@Test
	void memory() {
		final var memory = new MemoryBarrier();
		final VkMemoryBarrier descriptor = memory.populate(new EnumMask<>(MEMORY_READ), new EnumMask<>(MEMORY_WRITE));
		assertEquals(new EnumMask<>(MEMORY_READ), descriptor.srcAccessMask);
		assertEquals(new EnumMask<>(MEMORY_WRITE), descriptor.dstAccessMask);
	}

	@Test
	void buffer() {

	}

	@Test
	void image() {

	}
}
