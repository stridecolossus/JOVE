package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.core.Command;

import com.sun.jna.Pointer;

public class VulkanDataBufferTest extends AbstractVulkanTest {
	private VulkanDataBuffer buffer;
	private Pointer mem;

	@BeforeEach
	public void before() {
		mem = mock(Pointer.class);
		buffer = new VulkanDataBuffer(mock(Pointer.class), device, 3, mem);
	}

	@Test
	public void bindVertexBuffer() {
		final Pointer cb = mock(Pointer.class);
		final Command cmd = buffer.bindVertexBuffer();
		assertNotNull(cmd);
		cmd.execute(library, cb);
		verify(library).vkCmdBindVertexBuffers(cb, 0, 1, new Pointer[]{buffer.handle()}, new long[]{0});
	}

	@Test
	public void bindIndexBuffer() {
		final Pointer cb = mock(Pointer.class);
		final Command cmd = buffer.bindIndexBuffer(Short.BYTES);
		assertNotNull(cmd);
		cmd.execute(library, cb);
		verify(library).vkCmdBindIndexBuffer(cb, buffer.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT16);
	}

	@Test
	public void push() {
		// TODO - how to mock returned memory buffer
		//vbo.push(bb);
	}

	@Test
	public void pushInvalidBuffer() {
		final ByteBuffer bb = ByteBuffer.allocate(999);
		assertThrows(IllegalArgumentException.class, () -> buffer.push(bb));
	}

	@Nested
	class BuilderTests {
		private VulkanDataBuffer.Builder builder;

		@BeforeEach
		public void before() {
			builder = new VulkanDataBuffer.Builder(device);
		}

		@Disabled("how to mock selector, etc") // TODO
		@Test
		public void build() {
			buffer = builder
				.length(1)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.build();
		}

		@Test
		public void buildEmptyUsageFlags() {
			assertThrows(IllegalArgumentException.class, () -> builder.length(42).build());
		}

		@Test
		public void buildZeroSize() {
			assertThrows(IllegalArgumentException.class, () -> builder.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT).build());
		}
	}
}
