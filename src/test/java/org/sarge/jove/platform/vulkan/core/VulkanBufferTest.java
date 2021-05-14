package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkDescriptorBufferInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
	private static final long SIZE = 3;

	private VulkanBuffer buffer;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));
		when(mem.size()).thenReturn(SIZE);

		// Create buffer
		buffer = new VulkanBuffer(new Pointer(2), dev, FLAGS, mem);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(2)), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(FLAGS, buffer.usage());
		assertEquals(mem, buffer.memory());
	}

	@Nested
	class CommandTests {
		private Handle cmdHandle;
		private VulkanBuffer dest;
		private VulkanBuffer index;

		@BeforeEach
		void before() {
			final var flags = Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT);
			dest = new VulkanBuffer(new Pointer(2), dev, flags, mem);
			index = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT), mem);
			cmdHandle = new Handle(new Pointer(42));
		}

		@Test
		void bind() {
			final Command cmd = buffer.bindVertexBuffer();
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);
			verify(lib).vkCmdBindVertexBuffers(eq(cmdHandle), eq(0), eq(1), isA(Pointer.class), eq(new long[]{0}));
		}

		@Test
		void bindIndexBuffer() {
			final Command cmd = index.bindIndexBuffer();
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);
			verify(lib).vkCmdBindIndexBuffer(cmdHandle, index.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
		}

		@Test
		void copy() {
			// Execute copy command
			final Command cmd = buffer.copy(dest);
			assertNotNull(cmd);
			cmd.execute(lib, cmdHandle);

			// Check API
			final ArgumentCaptor<VkBufferCopy[]> captor = ArgumentCaptor.forClass(VkBufferCopy[].class);
			verify(lib).vkCmdCopyBuffer(eq(cmdHandle), eq(buffer.handle()), eq(dest.handle()), eq(1), captor.capture());

			// Check region
			final VkBufferCopy[] array = captor.getValue();
			assertNotNull(array);
			assertEquals(1, array.length);
			assertEquals(SIZE, array[0].size);
		}

		@Test
		void copyTooSmall() {
			final DeviceMemory small = mock(DeviceMemory.class);
			final VulkanBuffer dest = mock(VulkanBuffer.class);
			when(dest.memory()).thenReturn(small);
			when(small.size()).thenReturn(SIZE - 1);
			assertThrows(IllegalStateException.class, "Destination buffer is too small", () -> buffer.copy(dest));
		}

		@Test
		void copyInvalidDirection() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> dest.copy(buffer));
		}

		@Test
		void copySelf() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.copy(buffer));
		}

		@Test
		void copyInvalidUsage() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.copy(index));
		}
	}

	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyBuffer(dev.handle(), buffer.handle(), null);
		verify(mem).destroy();
	}

	@Nested
	class UniformBufferResourceTests {
		private DescriptorSet.Resource uniform;

		@BeforeEach
		void before() {
			final VulkanBuffer vbo = new VulkanBuffer(new Pointer(2), dev, Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT), mem);
			uniform = vbo.uniform();
		}

		@Test
		void constructor() {
			assertNotNull(uniform);
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, uniform.type());
		}

		@Test
		void populate() {
			// Populate write descriptor
			final var write = new VkWriteDescriptorSet();
			uniform.populate(write);

			// Check descriptor
			final VkDescriptorBufferInfo info = write.pBufferInfo;
			assertNotNull(info);
			assertEquals(buffer.handle(), info.buffer);
			assertEquals(buffer.memory().size(), info.range);
			assertEquals(0, info.offset);
		}

		@Test
		void uniformInvalidBuffer() {
			assertThrows(IllegalStateException.class, "Invalid usage", () -> buffer.uniform());
		}
	}

	@Test
	void create() {
		final MemoryProperties<VkBufferUsageFlag> props = new MemoryProperties<>(FLAGS, VkSharingMode.VK_SHARING_MODE_EXCLUSIVE, Set.of(), Set.of());
		when(dev.allocate(any(VkMemoryRequirements.class), eq(props))).thenReturn(mem);
		buffer = VulkanBuffer.create(dev, SIZE, props);
		assertNotNull(buffer);
		assertEquals(FLAGS, buffer.usage());
	}

	@Test
	void staging() {
		when(dev.allocate(any(VkMemoryRequirements.class), any(MemoryProperties.class))).thenReturn(mem);
		final VulkanBuffer staging = VulkanBuffer.staging(dev, SIZE);
		assertNotNull(staging);
		assertEquals(Set.of(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT), staging.usage());
	}
}
