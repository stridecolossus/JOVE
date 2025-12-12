package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.nio.ByteBuffer;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

class VulkanBufferTest {
	private static class MockVulkanBufferLibrary extends MockVulkanLibrary { // MockMemoryLibrary {
		private boolean bind;
		private boolean index;
		private boolean fill;

		@Override
		public VkResult vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pBuffer) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
//			assertEquals(new EnumMask<>(VkBufferUsageFlags.TRANSFER_DST), pCreateInfo.usage);
			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.sharingMode);
			assertEquals(42L, pCreateInfo.size);
			assertEquals(null, pAllocator);
			pBuffer.set(MemorySegment.ofAddress(3));
			return VkResult.VK_SUCCESS;
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
			pMemoryRequirements.size = 42L;
			pMemoryRequirements.alignment = 0;
			pMemoryRequirements.memoryTypeBits = 1;
		}

		@Override
		public VkResult vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset) {
			assertNotNull(device);
			assertEquals(new Handle(3), pBuffer);
			assertEquals(42L, memory.size());
			assertEquals(0L, memoryOffset);
			return VkResult.VK_SUCCESS;
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
			assertEquals(3, data);
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
		memory = new MockDeviceMemory(42L);
		buffer = new VulkanBuffer(new Handle(1), device, Set.of(VkBufferUsageFlags.TRANSFER_DST, VkBufferUsageFlags.VERTEX_BUFFER), memory, 42L);
	}

	@Test
	void require() {
		buffer.require(VkBufferUsageFlags.TRANSFER_DST);
	}

	@Test
	void copy() {
		final var src = new VulkanBuffer(new Handle(2), device, Set.of(VkBufferUsageFlags.TRANSFER_SRC), memory, 42L);
		final Command copy = src.copy(buffer);
		copy.execute(null);
	}

	@Test
	void fill() {
		final Command fill = buffer.fill(0L, VulkanBuffer.VK_WHOLE_SIZE, 3);
		fill.execute(null);
		assertEquals(true, library.fill);
	}

	@Test
	void map() {
		final MemorySegment mapped = buffer.map();
		assertEquals(42L, mapped.byteSize());
		assertEquals(true, buffer.memory().region().isPresent());
	}

	@Test
	void write() {
		buffer.write(new byte[]{42});
		assertEquals((byte) 42, buffer.memory().region().get().memory().get(ValueLayout.JAVA_BYTE, 0L));
	}

	@Test
	void buffer() {
		final ByteBuffer bb = buffer.buffer();
		bb.put((byte) 42);
		assertEquals((byte) 42, buffer.memory().region().get().memory().get(ValueLayout.JAVA_BYTE, 0L));
	}

	// TODO - offset tests

	@Test
	void create() {
		final var properties = new MemoryProperties.Builder<VkBufferUsageFlags>()
				.usage(VkBufferUsageFlags.TRANSFER_DST)
				.required(VkMemoryPropertyFlags.HOST_VISIBLE)
				.build();

		final var allocator = new MockAllocator(device);
		final var buffer = VulkanBuffer.create(allocator, 42L, properties);
		assertEquals(false, buffer.isDestroyed());
		assertEquals(42L, buffer.length());
	}

	@Test
	void staging() {
		// Create some data
		final byte[] data = new byte[42];
		data[0] = 3;

		// Create and populate staging buffer
		final var allocator = new MockAllocator(device);
		final var staging = VulkanBuffer.staging(allocator, MemorySegment.ofArray(data));
		assertEquals(false, staging.isDestroyed());
		assertEquals(42L, staging.length());
		staging.require(VkBufferUsageFlags.TRANSFER_SRC);

		// Check staging buffer memory
		final ByteBuffer bb = staging.map().asByteBuffer();
		assertEquals(42, bb.capacity());
		assertEquals((byte) 3, bb.get());
	}

	@Test
	void destroy() {
		buffer.destroy();
		assertEquals(true, buffer.isDestroyed());
		assertEquals(true, memory.isDestroyed());
	}
}
