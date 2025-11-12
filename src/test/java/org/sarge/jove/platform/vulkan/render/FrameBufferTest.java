package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.render.FrameBuffer.Group;

class FrameBufferTest {
	private static class MockFrameBufferLibrary extends MockVulkanLibrary {
		public boolean begin;
		public boolean end;
		public boolean destroyed;

		@Override
		public VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer) {
			assertEquals(0, pCreateInfo.flags);
			assertNotNull(pCreateInfo.renderPass);
			assertNotEquals(0, pCreateInfo.attachmentCount);
			assertNotEquals(0, pCreateInfo.pAttachments.length);
			assertEquals(640, pCreateInfo.width);
			assertEquals(480, pCreateInfo.height);
			assertEquals(1, pCreateInfo.layers);
			pFramebuffer.set(new Handle(3));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents) {
			assertNotNull(pRenderPassBegin.renderPass);
			assertEquals(new Handle(3), pRenderPassBegin.framebuffer);
			assertEquals(0, pRenderPassBegin.renderArea.offset.x);
			assertEquals(0, pRenderPassBegin.renderArea.offset.y);
			assertEquals(640, pRenderPassBegin.renderArea.extent.width);
			assertEquals(480, pRenderPassBegin.renderArea.extent.height);
			assertEquals(1, pRenderPassBegin.clearValueCount);
			assertEquals(1, pRenderPassBegin.pClearValues.length);
			assertArrayEquals(new float[]{0, 0, 0, 1}, pRenderPassBegin.pClearValues[0].color.float32);
			begin = true;
		}

		@Override
		public void vkCmdEndRenderPass(Buffer commandBuffer) {
			end = true;
		}

		@Override
		public void vkDestroyFramebuffer(LogicalDevice device, FrameBuffer framebuffer, Handle pAllocator) {
			destroyed = true;
		}
	}

	private FrameBuffer framebuffer;
	private LogicalDevice device;
	private MockFrameBufferLibrary library;

	@BeforeEach
	void before() {
		library = new MockFrameBufferLibrary();
		device = new MockLogicalDevice(library);

		final var pass = new MockRenderPass(device);

		final var view = new View(new Handle(2), device, new MockImage());
		view.clear(new ColourClearValue(Colour.BLACK));

		framebuffer = FrameBuffer.create(pass, new Rectangle(640, 480), List.of(view));
	}

	@Test
	void begin() {
		final Command begin = framebuffer.begin(VkSubpassContents.INLINE);
		begin.execute(null);
		assertEquals(true, library.begin);
	}

	@Test
	void end() {
		final Command end = framebuffer.end();
		end.execute(null);
		assertEquals(true, library.end);
	}

	@Test
	void destroy() {
		framebuffer.destroy();
		assertEquals(true, framebuffer.isDestroyed());
		assertEquals(true, library.destroyed);
	}

	@Nested
	class GroupTest {
		private Group group;

		@BeforeEach
		void before() {
			final var attachment = new View(new Handle(4), device, new MockImage());
			final var swapchain = new Swapchain(new Handle(5), device, library, VkFormat.R32G32B32A32_SFLOAT, new Dimensions(640, 480), List.of(attachment));
			final var pass = new MockRenderPass(device);
			final var other = new View(new Handle(6), device, new MockImage());
			group = new Group(swapchain, pass, List.of(other));
		}

		@Test
		void get() {
			final FrameBuffer buffer = group.get(0);
			assertEquals(false, buffer.isDestroyed());
			assertEquals(2, buffer.attachments().size());
		}

		@Test
		void create() {
			final FrameBuffer buffer = group.get(0);
			group.create();
			assertEquals(true, buffer.isDestroyed());
			assertNotNull(group.get(0));
		}

		@Test
		void destroy() {
			final FrameBuffer buffer = group.get(0);
			group.destroy();
			assertEquals(true, buffer.isDestroyed());
			assertThrows(IndexOutOfBoundsException.class, () -> group.get(0));
		}
	}
}
