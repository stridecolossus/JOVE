package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

class VulkanBufferTest {
	private static class MockVulkanBufferLibrary extends MockVulkanLibrary {
		private boolean bind;
		private boolean index;
		private boolean fill;

		@Override
		public VkResult vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pBuffer) {
			assertNotNull(device);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(new EnumMask<>(VkBufferUsageFlag.TRANSFER_DST), pCreateInfo.usage);
			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.sharingMode);
			assertEquals(3L, pCreateInfo.size);
			assertEquals(null, pAllocator);
			pBuffer.set(new Handle(3));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkDestroyBuffer(LogicalDevice device, VulkanBuffer pBuffer, Handle pAllocator) {
			assertNotNull(device);
			assertNotNull(pBuffer);
		}

		@Override
		public void vkGetBufferMemoryRequirements(LogicalDevice device, Handle pBuffer, VkMemoryRequirements pMemoryRequirements) {
			assertNotNull(device);
			assertEquals(new Handle(3), pBuffer);
			pMemoryRequirements.size = 3L;
			pMemoryRequirements.alignment = 0;
			pMemoryRequirements.memoryTypeBits = 1;
		}

		@Override
		public VkResult vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset) {
			assertNotNull(device);
			assertEquals(new Handle(3), pBuffer);
			assertEquals(3L, memory.size());
			assertEquals(0L, memoryOffset);
			return VkResult.SUCCESS;
		}

		@Override
		public void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets) {
			assertEquals(1, firstBinding);
			assertEquals(1, bindingCount);
			assertEquals(1, pBuffers.length);
			assertArrayEquals(new long[]{0L}, pOffsets);
			bind = true;
		}

		@Override
		public void vkCmdBindIndexBuffer(Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType) {
			assertNotNull(buffer);
			assertEquals(0L, offset);
			assertEquals(VkIndexType.UINT32, indexType);
			index = true;
		}

		@Override
		public void vkCmdCopyBuffer(Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions) {
			assertNotNull(srcBuffer);
			assertNotNull(dstBuffer);
			assertEquals(1, regionCount);
		}

		@Override
		public void vkCmdFillBuffer(Buffer commandBuffer, VulkanBuffer dstBuffer, long dstOffset, long size, int data) {
			assertEquals(0L, dstOffset);
			assertEquals(-1L, size);
			assertEquals(42, data);
			fill = true;
		}
	}

	private VulkanBuffer buffer;
	private LogicalDevice device;
	private MockVulkanBufferLibrary library;
	private DeviceMemory memory;

	@BeforeEach
	void before() {
		library = new MockVulkanBufferLibrary();
		device = new MockLogicalDevice(library);
		memory = new MockDeviceMemory(3L);
		buffer = new VulkanBuffer(new Handle(1), device, Set.of(VkBufferUsageFlag.TRANSFER_DST, VkBufferUsageFlag.VERTEX_BUFFER), memory, 3L);
	}

	@Test
	void require() {
		buffer.require(VkBufferUsageFlag.TRANSFER_DST);
	}

	@Test
	void copy() {
		final var src = new VulkanBuffer(new Handle(2), device, Set.of(VkBufferUsageFlag.TRANSFER_SRC), memory, memory.size());
		final Command copy = src.copy(buffer);
		copy.execute(null);
	}

	@Test
	void fill() {
		final Command fill = buffer.fill(0L, VulkanBuffer.VK_WHOLE_SIZE, 42);
		fill.execute(null);
		assertEquals(true, library.fill);
	}

	// TODO - offset tests

	@Test
	void create() {
	}

	@Test
	void staging() {
	}

	@Test
	void destroy() {
		buffer.destroy();
		assertEquals(true, buffer.isDestroyed());
		assertEquals(true, memory.isDestroyed());
	}
}
