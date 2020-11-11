package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

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
			when(buffer.extents()).thenReturn(new Image.Extents(3, 4));
			when(buffer.attachments()).thenReturn(List.of(view));

			// Create command
			final Command cmd = pass.begin(buffer);
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

			// Check extents
			assertEquals(0, info.renderArea.offset.x);
			assertEquals(0, info.renderArea.offset.y);
			assertEquals(3, info.renderArea.extent.width);
			assertEquals(4, info.renderArea.extent.height);

			// Check clear values
			assertEquals(1, info.clearValueCount);
			assertNotNull(info.pClearValues);
			assertArrayEquals(Colour.BLACK.toArray(), info.pClearValues.color.float32);
		}

		@Test
		void end() {
			final Handle handle = new Handle(new Pointer(2));
			RenderPass.END_COMMAND.execute(lib, handle);
			verify(lib).vkCmdEndRenderPass(handle);
		}
	}

	// TODO - split this up

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
				.attachment()
					.format(VkFormat.VK_FORMAT_D32_SFLOAT)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.depth(1)
					.build()
				.dependency()
					.source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().index(0)
					.destination().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destination().access(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
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
			assertEquals(0, info.flags);
			assertEquals(2, info.attachmentCount);
			assertEquals(1, info.subpassCount);
			assertEquals(1, info.dependencyCount);

			// Check attachment descriptor
			assertNotNull(info.pAttachments);
			assertEquals(0, info.pAttachments.flags);
			assertEquals(VkFormat.VK_FORMAT_R8G8B8A8_UNORM, info.pAttachments.format);
			assertEquals(VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT, info.pAttachments.samples);
			assertEquals(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR, info.pAttachments.loadOp);
			assertEquals(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE, info.pAttachments.storeOp);
			assertEquals(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE, info.pAttachments.stencilLoadOp);
			assertEquals(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE, info.pAttachments.stencilStoreOp);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, info.pAttachments.initialLayout);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, info.pAttachments.finalLayout);

			// Check sub-pass descriptor
			assertNotNull(info.pSubpasses);
			assertEquals(0, info.pSubpasses.flags);
			assertEquals(VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, info.pSubpasses.pipelineBindPoint);
			assertEquals(1, info.pSubpasses.colorAttachmentCount);

			// Check colour attachment descriptor
			assertNotNull(info.pSubpasses.pColorAttachments);
			assertEquals(0, info.pSubpasses.pColorAttachments.attachment);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, info.pSubpasses.pColorAttachments.layout);

			// Check depth attachment descriptor
			assertNotNull(info.pSubpasses.pDepthStencilAttachment);
			assertEquals(1, info.pSubpasses.pDepthStencilAttachment.attachment);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, info.pSubpasses.pDepthStencilAttachment.layout);

			// Check dependencies
			assertNotNull(info.pDependencies);
			assertEquals(0, info.pDependencies.dependencyFlags);

			// Check source dependency
			assertEquals(RenderPass.VK_SUBPASS_EXTERNAL, info.pDependencies.srcSubpass);
			assertEquals(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value(), info.pDependencies.srcStageMask);
			assertEquals(0, info.pDependencies.srcAccessMask);

			// Check destination dependency
			assertEquals(0, info.pDependencies.dstSubpass);
			assertEquals(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT.value(), info.pDependencies.dstStageMask);
			assertEquals(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT.value(), info.pDependencies.dstAccessMask);
		}

		private void add() {
			builder
				.attachment()
				.format(VkFormat.VK_FORMAT_D32_SFLOAT)
				.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();
		}

		// TODO - split this up

		@Test
		void buildEmptyAttachments() {
			assertThrows(IllegalArgumentException.class, "At least one attachment must be specified", () -> builder.build());
		}

		@Test
		void buildEmptySubPass() {
			add();
			assertThrows(IllegalArgumentException.class, "At least one sub-pass must be specified", () -> builder.build());
		}

		// TODO - split this up

		@Test
		void attachmentRequiresFormat() {
			assertThrows(IllegalArgumentException.class, "No format specified", () -> builder.attachment().build());
		}

		@Test
		void attachmentRequiresFinalLayout() {
			assertThrows(IllegalArgumentException.class, "No final layout specified", () -> builder.attachment().format(VkFormat.VK_FORMAT_D32_SFLOAT).build());
		}

		@Test
		void attachmentInvalidFinalLayout() {
			final var attachment = builder.attachment().format(VkFormat.VK_FORMAT_D32_SFLOAT);
			assertThrows(IllegalArgumentException.class, "Invalid final layout", () -> attachment.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PREINITIALIZED));
		}

		// TODO - split this up

		@Test
		void subpassEmpty() {
			assertThrows(IllegalArgumentException.class, "No attachments specified", () -> builder.subpass().build());
		}

		@Test
		void subpassInvalidIndex() {
			assertThrows(IllegalArgumentException.class, "Invalid attachment index", () -> builder.subpass().depth(0).build());
		}

		@Test
		void subpassInvalidLayout() {
			add();
			assertThrows(IllegalArgumentException.class, "Invalid attachment layout", () -> builder.subpass().colour(0, VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED).build());
		}

		@Test
		void subpassDuplicateDepthAttachment() {
			add();
			assertThrows(IllegalArgumentException.class, "Depth buffer already configured", () -> builder.subpass().depth(0).depth(0));
		}

		// TODO - split this up

		@Test
		void dependencyInvalidSubpass() {
			add();
			assertThrows(IllegalArgumentException.class, "Invalid sub-pass", () -> builder.dependency().source().index(0));
		}
	}
}
