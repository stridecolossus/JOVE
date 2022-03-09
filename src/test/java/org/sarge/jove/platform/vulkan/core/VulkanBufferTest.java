package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlag.INDEX_BUFFER;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlag.TRANSFER_SRC;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlag.VERTEX_BUFFER;

import java.nio.ByteBuffer;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(TRANSFER_SRC, VERTEX_BUFFER);
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private Region region;
	private ByteBuffer bb;
	private AllocationService allocator;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));
		when(mem.size()).thenReturn(SIZE);

		// Init mapped region
		bb = mock(ByteBuffer.class);
		region = mock(Region.class);
		when(mem.map()).thenReturn(region);
		when(region.buffer()).thenReturn(bb);

		// Init memory allocator
		allocator = mock(AllocationService.class);
		when(allocator.allocate(isA(VkMemoryRequirements.class), isA(MemoryProperties.class))).thenReturn(mem);

		// Create buffer
		buffer = new VulkanBuffer(new Handle(2), dev, FLAGS, mem, SIZE);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(2), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(FLAGS, buffer.usage());
		assertEquals(mem, buffer.memory());
		assertEquals(SIZE, buffer.length());
	}

	@Test
	void copyConstructor() {
		final VulkanBuffer copy = new VulkanBuffer(buffer);
		assertEquals(buffer, copy);
	}

	@Test
	void validate() {
		buffer.validate(0);
		buffer.validate(2);
	}

	@Test
	void validateInvalidOffset() {
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(SIZE));
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(-1));
	}

	@Test
	void require() {
		buffer.require();
		buffer.require(TRANSFER_SRC);
		buffer.require(VERTEX_BUFFER);
		buffer.require(TRANSFER_SRC, VERTEX_BUFFER);
	}

	@Test
	void requireNotSupported() {
		assertThrows(IllegalStateException.class, () -> buffer.require(INDEX_BUFFER));
		assertThrows(IllegalStateException.class, () -> buffer.require(TRANSFER_SRC, INDEX_BUFFER));
	}

	@Test
	void buffer() {
		assertEquals(bb, buffer.buffer());
	}

	@Test
	void create() {
		final MemoryProperties<VkBufferUsageFlag> props = new MemoryProperties<>(FLAGS, VkSharingMode.EXCLUSIVE, Set.of(), Set.of());
		buffer = VulkanBuffer.create(dev, allocator, SIZE, props);
		assertNotNull(buffer);
		assertEquals(FLAGS, buffer.usage());
	}

	@Test
	void staging() {
		// Create data
		final Bufferable data = mock(Bufferable.class);
		when(data.length()).thenReturn((int) SIZE);

		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);
		assertNotNull(staging);
		assertEquals(Set.of(TRANSFER_SRC), staging.usage());
		assertEquals(SIZE, staging.length());

		// Check data is copied to buffer
		verify(region).buffer();
		verify(data).buffer(bb);
	}

	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyBuffer(dev, buffer, null);
		verify(mem).destroy();
	}

	@Test
	void copy() {
		// Create destination
		final VulkanBuffer dest = mock(VulkanBuffer.class);

		// Create copy command
		final Command copy = buffer.copy(dest);
		assertNotNull(copy);
		verify(dest).require(VkBufferUsageFlag.TRANSFER_DST);

		// Init expected copy region descriptor
		final var region = new VkBufferCopy() {
			@Override
			public boolean equals(Object obj) {
				final VkBufferCopy actual = (VkBufferCopy) obj;
				assertEquals(0, actual.srcOffset);
				assertEquals(0, actual.dstOffset);
				assertEquals(SIZE, actual.size);
				return true;
			}
		};

		// Copy buffer
		final Command.Buffer cmd = mock(Command.Buffer.class);
		copy.execute(lib, cmd);
		verify(lib).vkCmdCopyBuffer(cmd, buffer, dest, 1, new VkBufferCopy[]{region});
	}
}
