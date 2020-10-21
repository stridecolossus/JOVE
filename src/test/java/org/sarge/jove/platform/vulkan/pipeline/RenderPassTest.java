package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkRenderPassBeginInfo;
import org.sarge.jove.platform.vulkan.VkRenderPassCreateInfo;
import org.sarge.jove.platform.vulkan.VkSubpassContents;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.ExtentHelper;

import com.sun.jna.Pointer;

public class RenderPassTest extends AbstractVulkanTest {
	@Nested
	class RenderPassTests {
		private RenderPass pass;

		@BeforeEach
		void before() {
			pass = new RenderPass(new Pointer(1), dev);
		}

		@Test
		void constructor() {
			assertNotNull(pass.handle());
			assertEquals(dev, pass.device());
		}

		@Test
		void destroy() {
			final Handle handle = pass.handle();
			pass.destroy();
			verify(lib).vkDestroyRenderPass(dev.handle(), handle, null);
		}

		@Test
		void begin() {
			// Create a frame buffer with a single colour attachment
			final FrameBuffer buffer = mock(FrameBuffer.class);
			final View view = mock(View.class);
			when(view.clear()).thenReturn(ClearValue.COLOUR);
			when(buffer.attachments()).thenReturn(List.of(view));

			// Create command
			final Rectangle extent = new Rectangle(1, 2, 3, 4);
			final Command cmd = pass.begin(buffer, extent);
			assertNotNull(cmd);

			// Invoke command
			final Handle handle = new Handle(new Pointer(2));
			cmd.execute(lib, handle);

			// Check API
			final ArgumentCaptor<VkRenderPassBeginInfo> captor = ArgumentCaptor.forClass(VkRenderPassBeginInfo.class);
			verify(lib).vkCmdBeginRenderPass(eq(handle), captor.capture(), eq(VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE));

			// Check descriptor
			final VkRenderPassBeginInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pass.handle(), info.renderPass);
			assertEquals(buffer.handle(), info.framebuffer);
			assertEquals(1, info.clearValueCount);
			assertNotNull(info.pClearValues);
			assertTrue(ExtentHelper.of(extent).dataEquals(info.renderArea));
			assertArrayEquals(Colour.BLACK.toArray(), info.pClearValues.color.float32);
		}

		@Test
		void end() {
			final Handle handle = new Handle(new Pointer(2));
			RenderPass.END_COMMAND.execute(lib, handle);
			verify(lib).vkCmdEndRenderPass(handle);
		}
	}

	@Nested
	class BuilderTests {
		private RenderPass.Builder builder;

		@BeforeEach
		void before() {
			builder = new RenderPass.Builder(dev);
		}

		@Test
		void build() {
			// Build render pass
			final RenderPass pass = builder
				.attachment()
					.format(VkFormat.VK_FORMAT_R8G8B8A8_UNORM)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.subpass()
					.bind(VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS)
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.build()
				.build();

			// Check render pass
			assertNotNull(pass);
			assertNotNull(pass.handle());
			assertEquals(dev, pass.device());

			// Check allocation
			final ArgumentCaptor<VkRenderPassCreateInfo> captor = ArgumentCaptor.forClass(VkRenderPassCreateInfo.class);
			verify(lib).vkCreateRenderPass(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

			// Check descriptor
			final var info = captor.getValue();
			assertEquals(1, info.attachmentCount);
			assertNotNull(info.pAttachments);
			assertEquals(1, info.subpassCount);
			assertNotNull(info.pSubpasses);
			assertEquals(0, info.dependencyCount);
//			assertNotNull(info.pDependencies);
			assertEquals(0, info.flags);
		}
	}
}
