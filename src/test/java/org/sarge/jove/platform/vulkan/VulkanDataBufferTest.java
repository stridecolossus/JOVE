package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VulkanHandle.Destructor;

import com.sun.jna.Pointer;

public class VulkanDataBufferTest extends AbstractVulkanTest {
	private VulkanDataBuffer buffer;
	private Pointer mem;

	@BeforeEach
	public void before() {
		mem = mock(Pointer.class);
		buffer = new VulkanDataBuffer(new VulkanHandle(mock(Pointer.class), Destructor.NULL), 3, mem, device);
	}

//	@Test
//	public void bindVertexBuffer() {
//		final Pointer buffer = mock(Pointer.class);
//		final Command cmd = vbo.bind();
//		assertNotNull(cmd);
//		cmd.execute(library, buffer);
//		verify(library).vkCmdBindVertexBuffers(buffer, 0, 1, new Pointer[]{vbo.handle()}, new long[]{0});
//	}
//
//	@Test
//	public void bindIndexBuffer() {
//		final Pointer buffer = mock(Pointer.class);
//		final Command cmd = vbo.bindIndex();
//		assertNotNull(cmd);
//		cmd.execute(library, buffer);
//		verify(library).vkCmdBindVertexBuffers(buffer, 0, 1, new Pointer[]{vbo.handle()}, new long[]{0});
//	}

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
