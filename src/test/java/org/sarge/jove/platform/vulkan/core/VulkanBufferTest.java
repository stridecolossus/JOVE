package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer.Factory;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.*;

class VulkanBufferTest {
	@SuppressWarnings("unused")
	private static class MockVulkanBufferLibrary extends MockLibrary {
		public VkResult vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pBuffer) {
			assertEquals(VkStructureType.BUFFER_CREATE_INFO, pCreateInfo.sType);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(VkSharingMode.EXCLUSIVE, pCreateInfo.sharingMode);
			assertEquals(42L, pCreateInfo.size);
			init(pBuffer);
			return VkResult.VK_SUCCESS;
		}

		public void vkGetBufferMemoryRequirements(LogicalDevice device, Handle pBuffer, VkMemoryRequirements pMemoryRequirements) {
			pMemoryRequirements.size = 42L;
			pMemoryRequirements.alignment = 0;
			pMemoryRequirements.memoryTypeBits = 1;
		}

		public VkResult vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset) {
			assertEquals(42L, memory.size());
			assertEquals(0L, memoryOffset);
			return VkResult.VK_SUCCESS;
		}

		public void vkCmdCopyBuffer(Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions) {
			assertEquals(regionCount, pRegions.length);
		}

		public void vkCmdFillBuffer(Buffer commandBuffer, VulkanBuffer dstBuffer, long dstOffset, long size, int data) {
			assertEquals(0L, dstOffset);
			assertEquals(-1L, size);
			assertEquals(3, data);
		}
	}

	private Factory factory;
	private VulkanBuffer buffer;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(VulkanBuffer.Library.class);
		mockery.implement(new MockVulkanBufferLibrary());

		final var properties = new MemoryProperties.Builder<VkBufferUsageFlags>()
				.usage(VkBufferUsageFlags.TRANSFER_DST)
				.usage(VkBufferUsageFlags.VERTEX_BUFFER)
				.build();

		factory = new Factory(new MockAllocator(new MockLogicalDevice(mockery.proxy())));
		buffer = factory.create(42L, properties);
	}

	@Test
	void constructor() {
		assertFalse(buffer.isDestroyed());
		assertEquals(42L, buffer.length());
	}

	@Test
	void require() {
		buffer.require(VkBufferUsageFlags.TRANSFER_DST);
	}

	@Test
	void copy() {
		final var properties = new MemoryProperties<>(VkBufferUsageFlags.TRANSFER_SRC);
		final var source = factory.create(42L, properties);
		final Command copy = source.copy(buffer);
		copy.execute(null);
		assertEquals(1, mockery.mock("vkCmdCopyBuffer").count());
	}

	@Test
	void fill() {
		final Command fill = buffer.fill(0L, VulkanBuffer.VK_WHOLE_SIZE, 3);
		fill.execute(null);
		assertEquals(1, mockery.mock("vkCmdFillBuffer").count());
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
	void staging() {
		// Create some data
		final byte[] data = new byte[42];
		data[0] = 3;

		// Create and populate staging buffer
		final var staging = factory.staging(MemorySegment.ofArray(data));
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
		assertTrue(buffer.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyBuffer").count());
	}
}
