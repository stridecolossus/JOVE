package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass.Builder.AttachmentBuilder;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass.Builder.DependencyBuilder;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass.Builder.SubPassBuilder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class RenderPassTest extends AbstractVulkanTest {
	private static final VkFormat FORMAT = VkFormat.VK_FORMAT_R8G8B8A8_UNORM;

	private RenderPass.Builder renderPassBuilder;

	@BeforeEach
	void before() {
		renderPassBuilder = new RenderPass.Builder(dev);
	}

	/**
	 * Adds a colour attachment.
	 */
	private void colour() {
		renderPassBuilder
				.attachment()
					.format(FORMAT)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
				.build();
	}

	/**
	 * Adds a depth attachment.
	 */
	private void depth() {
		renderPassBuilder
				.attachment()
					.format(VkFormat.VK_FORMAT_D32_SFLOAT)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();
	}

	@Nested
	class RenderPassTests {
		private RenderPass pass;
		private Handle handle;

		@BeforeEach
		void before() {
			depth();
			pass = renderPassBuilder.subpass().depth(0).build().build();
			handle = new Handle(new Pointer(2));
		}

		@Test
		void constructor() {
			assertNotNull(pass);
			assertNotNull(pass.handle());
			assertEquals(dev, pass.device());
		}

		@DisplayName("Create command to begin a render pass")
		@Test
		void begin() {
			// Create frame-buffer attachment
			final View view = mock(View.class);
			when(view.clear()).thenReturn(ClearValue.DEPTH);

			// Create frame-buffer
			final FrameBuffer buffer = mock(FrameBuffer.class);
			when(buffer.extents()).thenReturn(new Image.Extents(3, 4));
			when(buffer.attachments()).thenReturn(List.of(view));

			// Create begin command
			final Command cmd = pass.begin(buffer);
			assertNotNull(cmd);

			// Execute begin command
			cmd.execute(lib, handle);

			// Check API
			final ArgumentCaptor<VkRenderPassBeginInfo> captor = ArgumentCaptor.forClass(VkRenderPassBeginInfo.class);
			verify(lib).vkCmdBeginRenderPass(eq(handle), captor.capture(), eq(VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE));

			// Check descriptor
			final VkRenderPassBeginInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pass.handle(), info.renderPass);
			assertEquals(buffer.handle(), info.framebuffer);
			assertTrue(buffer.extents().toRect2D().dataEquals(info.renderArea));
			assertEquals(1, info.clearValueCount);
			assertNotNull(info.pClearValues);
		}

		@DisplayName("Create command to end a render pass")
		@Test
		void end() {
			RenderPass.END_COMMAND.execute(lib, handle);
			verify(lib).vkCmdEndRenderPass(handle);
		}

		@Test
		void destroy() {
			pass.destroy();
			verify(lib).vkDestroyRenderPass(dev.handle(), pass.handle(), null);
		}
	}

	@Nested
	class RenderPassBuilderTests {
		@Test
		void build() {
			// Build render pass
			final RenderPass pass = renderPassBuilder
				.attachment()
					.format(FORMAT)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.build()
				.dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0)
					.source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
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
			assertEquals(1, info.attachmentCount);
			assertEquals(1, info.subpassCount);
			assertEquals(1, info.dependencyCount);
			assertNotNull(info.pAttachments);
			assertNotNull(info.pSubpasses);
			assertNotNull(info.pDependencies);
		}

		@DisplayName("At least one attachment must be configured")
		@Test
		void buildEmptyAttachments() {
			assertThrows(IllegalArgumentException.class, "At least one attachment must be specified", () -> renderPassBuilder.build());
		}

		@DisplayName("At least one sub-pass must be configured")
		@Test
		void buildEmptySubPass() {
			colour();
			assertThrows(IllegalArgumentException.class, "At least one sub-pass must be specified", () -> renderPassBuilder.build());
		}

		@DisplayName("No format specified for attachment")
		@Test
		void attachmentRequiresFormat() {
			assertThrows(IllegalArgumentException.class, "No format specified", () -> renderPassBuilder.attachment().build());
		}

		@DisplayName("No final layout specified for attachment")
		@Test
		void attachmentRequiresFinalLayout() {
			assertThrows(IllegalArgumentException.class, "No final layout specified", () -> renderPassBuilder.attachment().format(VkFormat.VK_FORMAT_D32_SFLOAT).build());
		}

		@DisplayName("Invalid final layout specified for attachment")
		@Test
		void attachmentInvalidFinalLayout() {
			final var attachment = renderPassBuilder.attachment().format(VkFormat.VK_FORMAT_D32_SFLOAT);
			assertThrows(IllegalArgumentException.class, "Invalid final layout", () -> attachment.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PREINITIALIZED));
		}
	}

	@Nested
	class AttachmentBuilderTests {
		private AttachmentBuilder attachmentBuilder;

		@BeforeEach
		void before() {
			attachmentBuilder = renderPassBuilder.attachment();
		}

		@Test
		void constructor() {
			assertNotNull(attachmentBuilder);
		}

		@Test
		void invalidFinalLayout() {
			assertThrows(IllegalArgumentException.class, () -> attachmentBuilder.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED));
			assertThrows(IllegalArgumentException.class, () -> attachmentBuilder.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PREINITIALIZED));
		}

		@Test
		void build() {
			// Configure attachment
			attachmentBuilder
					.format(FORMAT)
					.samples(VkSampleCountFlag.VK_SAMPLE_COUNT_16_BIT)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

			// Populate descriptor
			final var desc = new VkAttachmentDescription();
			attachmentBuilder.populate(desc);

			// Check descriptor
			assertEquals(0, desc.flags);
			assertEquals(FORMAT, desc.format);
			assertEquals(VkSampleCountFlag.VK_SAMPLE_COUNT_16_BIT, desc.samples);
			assertEquals(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR, desc.loadOp);
			assertEquals(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE, desc.storeOp);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, desc.initialLayout);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, desc.finalLayout);

			// Build attachment
			assertEquals(renderPassBuilder, attachmentBuilder.build());
		}

		@DisplayName("Format must be specified for the attachment")
		@Test
		void buildMissingFormat() {
			assertThrows(IllegalArgumentException.class, "No format specified", () -> attachmentBuilder.build());
		}

		@DisplayName("Final layout must be specified for attachment")
		@Test
		void buildMissingFinalLayout() {
			attachmentBuilder.format(FORMAT);
			assertThrows(IllegalArgumentException.class, "No final layout", () -> attachmentBuilder.build());
		}
	}

	@Nested
	class SubPassBuilderTests {
		private SubPassBuilder subPassBuilder;

		@BeforeEach
		void before() {
			subPassBuilder = renderPassBuilder.subpass();
		}

		@Test
		void constructor() {
			assertNotNull(subPassBuilder);
		}

		@DisplayName("Cannot create sub-pass without any attachments")
		@Test
		void subpassEmpty() {
			assertThrows(IllegalArgumentException.class, "No attachments specified", () -> subPassBuilder.build());
		}

		@DisplayName("Invalid attachment index for sub-pass")
		@Test
		void subpassInvalidIndex() {
			assertThrows(IllegalArgumentException.class, "Invalid attachment index", () -> subPassBuilder.depth(0));
		}

		@DisplayName("Invalid colour attachment layout")
		@Test
		void subpassInvalidLayout() {
			colour();
			assertThrows(IllegalArgumentException.class, "Invalid attachment layout", () -> subPassBuilder.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED));
		}

		@DisplayName("Duplicate depth attachment reference")
		@Test
		void subpassDuplicateDepthAttachment() {
			depth();
			subPassBuilder.depth(0).build();
			assertThrows(IllegalArgumentException.class, "Depth buffer already configured", () -> subPassBuilder.depth(0));
		}

		@Test
		void build() {
			// Configure sub-pass
			colour();
			depth();

			// Populate descriptor
			final var desc = new VkSubpassDescription();
			subPassBuilder
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.depth(1)
					.populate(desc);

			// Check descriptor
			assertEquals(VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, desc.pipelineBindPoint);

			// Check colour attachment reference
			assertNotNull(desc.pColorAttachments);
			assertEquals(1, desc.colorAttachmentCount);
			assertEquals(0, desc.pColorAttachments.attachment);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, desc.pColorAttachments.layout);

			// Check depth attachment reference
			assertNotNull(desc.pDepthStencilAttachment);
			assertEquals(1, desc.pDepthStencilAttachment.attachment);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, desc.pDepthStencilAttachment.layout);

			// Build sub-pass
			assertEquals(renderPassBuilder, subPassBuilder.build());
		}
	}

	@Nested
	class DependencyBuilderTests {
		private DependencyBuilder dependencyBuilder;

		@BeforeEach
		void before() {
			colour();
			renderPassBuilder.subpass().colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL).build();
			dependencyBuilder = renderPassBuilder.dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0);
		}

		@Test
		void constructor() {
			assertNotNull(dependencyBuilder);
			assertNotNull(dependencyBuilder.source());
			assertNotNull(dependencyBuilder.destination());
		}

		@DisplayName("Invalid sub-pass index")
		@Test
		void constructorInvalidIndex() {
			assertThrows(IllegalArgumentException.class, "Invalid sub-pass index", () -> renderPassBuilder.dependency(0, 1));
		}

		@DisplayName("Source index cannot be > destination")
		@Test
		void constructorCyclicDependency() {
			depth();
			renderPassBuilder.subpass().depth(1).build();
			assertThrows(IllegalArgumentException.class, "cannot be greater-than", () -> renderPassBuilder.dependency(1, 0));
		}

		// TODO
		@Disabled
		@DisplayName("Invalid implicit subpass dependency")
		@Test
		void constructorInvalid() {
			assertThrows(IllegalArgumentException.class, "cannot be greater-than", () -> renderPassBuilder.dependency(RenderPass.VK_SUBPASS_EXTERNAL, RenderPass.VK_SUBPASS_EXTERNAL));
		}

		@DisplayName("Build valid dependency")
		@Test
		void build() {
			// Configure dependency
			dependencyBuilder
				.source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_VERTEX_INPUT_BIT)
				.source().access(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT)
				.destination().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
				.destination().access(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);

			// Populate descriptor
			final var info = new VkSubpassDependency();
			dependencyBuilder.populate(info);
			assertEquals(0, info.dependencyFlags);

			// Check source dependency
			assertEquals(RenderPass.VK_SUBPASS_EXTERNAL, info.srcSubpass);
			assertEquals(VkPipelineStageFlag.VK_PIPELINE_STAGE_VERTEX_INPUT_BIT.value(), info.srcStageMask);
			assertEquals(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT.value(), info.srcAccessMask);

			// Check destination dependency
			assertEquals(0, info.dstSubpass);
			assertEquals(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT.value(), info.dstStageMask);
			assertEquals(VkAccessFlag.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT.value(), info.dstAccessMask);

			// Build dependency
			assertEquals(renderPassBuilder, dependencyBuilder.build());
		}

		@Test
		void populateEmptyPipelineStages() {
			assertThrows(IllegalArgumentException.class, "No pipeline stage(s)", () -> dependencyBuilder.populate(new VkSubpassDependency()));
		}
	}
}
