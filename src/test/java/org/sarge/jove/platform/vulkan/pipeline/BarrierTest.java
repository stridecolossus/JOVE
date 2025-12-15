package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkAccessFlags.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStageFlags.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.MockImage;
import org.sarge.jove.platform.vulkan.pipeline.Barrier.BarrierType.*;
import org.sarge.jove.util.EnumMask;

class BarrierTest {
	private static class MockPipelineLibrary extends MockVulkanLibrary {
		boolean executed;

		@Override
		public void vkCmdPipelineBarrier(
				Buffer commandBuffer,
				EnumMask<VkPipelineStageFlags> srcStageMask, EnumMask<VkPipelineStageFlags> dstStageMask,
				EnumMask<VkDependencyFlags> dependencyFlags,
				int memoryBarrierCount,
				VkMemoryBarrier[] pMemoryBarriers,
				int bufferMemoryBarrierCount,
				VkBufferMemoryBarrier[] pBufferMemoryBarriers,
				int imageMemoryBarrierCount,
				VkImageMemoryBarrier[] pImageMemoryBarriers
		)
		{
			assertEquals(new EnumMask<>(TOP_OF_PIPE), srcStageMask);
			assertEquals(new EnumMask<>(TRANSFER), dstStageMask);
			assertEquals(new EnumMask<>(VkDependencyFlags.DEVICE_GROUP), dependencyFlags);
			assertEquals(memoryBarrierCount, pMemoryBarriers.length);
			assertEquals(bufferMemoryBarrierCount, pBufferMemoryBarriers.length);
			assertEquals(imageMemoryBarrierCount, pImageMemoryBarriers.length);
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
		assertEquals(VkStructureType.MEMORY_BARRIER, descriptor.sType);
		assertEquals(new EnumMask<>(MEMORY_READ), descriptor.srcAccessMask);
		assertEquals(new EnumMask<>(MEMORY_WRITE), descriptor.dstAccessMask);
	}

	@Test
	void buffer() {
		// TODO
	}

	@Test
	void image() {
		final var image = new MockImage();
		final var barrier = new ImageBarrier(
				image,
				image.descriptor(),
				VkImageLayout.UNDEFINED,
				VkImageLayout.COLOR_ATTACHMENT_OPTIMAL,
				Family.IGNORED,
				Family.IGNORED
		);
		final VkImageMemoryBarrier descriptor = barrier.populate(new EnumMask<>(), new EnumMask<>());
		assertEquals(VkStructureType.IMAGE_MEMORY_BARRIER, descriptor.sType);
		assertEquals(image.handle(), descriptor.image);
		assertNotNull(descriptor.subresourceRange);
		assertEquals(VkImageLayout.UNDEFINED, descriptor.oldLayout);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, descriptor.newLayout);
		assertEquals(-1, descriptor.srcQueueFamilyIndex);
		assertEquals(-1, descriptor.dstQueueFamilyIndex);
	}
}
