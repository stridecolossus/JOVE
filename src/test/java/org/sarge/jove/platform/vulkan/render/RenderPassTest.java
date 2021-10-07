package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.sarge.jove.platform.vulkan.VkImageLayout.COLOR_ATTACHMENT_OPTIMAL;
import static org.sarge.jove.platform.vulkan.VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.sarge.jove.platform.vulkan.render.RenderPass.VK_SUBPASS_EXTERNAL;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.DependencyBuilder;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.SubPassBuilder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class RenderPassTest extends AbstractVulkanTest {
	private static final VkFormat COLOUR = VkFormat.R32G32B32A32_SFLOAT;
	private static final VkFormat DEPTH = VkFormat.D32_SFLOAT;

	private RenderPass.Builder builder;
	private Attachment attachment;
	private Attachment depth;

	@BeforeEach
	void before() {
		// Create a colour attachment
		attachment = new Attachment.Builder()
				.format(COLOUR)
				.load(VkAttachmentLoadOp.CLEAR)
				.store(VkAttachmentStoreOp.STORE)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();

		// Create a depth attachment
		depth = new Attachment.Builder()
				.format(DEPTH)
				.finalLayout(DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();

		// Init builder
		builder = new RenderPass.Builder();
	}

	/**
	 * Adds a sub-pass with a single colour attachment.
	 */
	private void subpass() {
		builder.subpass().colour(attachment, COLOR_ATTACHMENT_OPTIMAL).build();
	}

	/**
	 * Builds a render pass and verifies the API call.
	 */
	private VkRenderPassCreateInfo build() {
		// Construct render pass
		final RenderPass pass = builder.build(dev);

		// Check render pass
		assertNotNull(pass);
		assertEquals(List.of(attachment), pass.attachments());

		// Check API
		final ArgumentCaptor<VkRenderPassCreateInfo> captor = ArgumentCaptor.forClass(VkRenderPassCreateInfo.class);
		verify(lib).vkCreateRenderPass(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check create descriptor
		final VkRenderPassCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);

		return info;
	}

	@Nested
	class RenderPassTests {
		private RenderPass pass;

		@BeforeEach
		void before() {
			subpass();
			pass = builder.build(dev);
		}

		@Test
		void constructor() {
			assertNotNull(pass);
			assertEquals(List.of(attachment), pass.attachments());
			assertEquals(dev, pass.device());
			assertEquals(false, pass.isDestroyed());
			assertNotNull(pass.destructor(lib));
		}

		@Test
		void close() {
			pass.close();
			verify(lib).vkDestroyRenderPass(dev, pass, null);
		}
	}

	@Nested
	class BuilderTests {
		@Test
		void simple() {
			// Create a render pass with a single sub-pass
			subpass();
			final VkRenderPassCreateInfo info = build();

			// Check attachment descriptor
			assertEquals(1, info.attachmentCount);
//			assertEquals(true, attachment.descriptor().dataEquals(info.pAttachments));
			assertEquals(0, info.flags);

			// Check sub-pass
			final VkSubpassDescription sub = info.pSubpasses;
			assertEquals(1, info.subpassCount);
			assertNotNull(sub);
			assertEquals(VkPipelineBindPoint.GRAPHICS, sub.pipelineBindPoint);
			assertEquals(1, sub.colorAttachmentCount);
			assertEquals(0, sub.inputAttachmentCount);
			assertEquals(0, sub.preserveAttachmentCount);
			assertEquals(0, sub.inputAttachmentCount);
			assertEquals(null, sub.pInputAttachments);
			assertEquals(null, sub.pDepthStencilAttachment);
			assertEquals(null, sub.pPreserveAttachments);

			// Check colour attachment reference
			final VkAttachmentReference ref = info.pSubpasses.pColorAttachments;
			assertNotNull(ref);
			assertEquals(0, ref.attachment);
			assertEquals(COLOR_ATTACHMENT_OPTIMAL, ref.layout);

			// Check dependencies
			assertEquals(0, info.dependencyCount);
			assertEquals(null, info.pDependencies);
		}

		@Test
		void buildEmptySubPasses() {
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}

	@Nested
	class SubPassBuilderTests {
		private SubPassBuilder sub;

		@BeforeEach
		void before() {
			sub = builder.subpass();
			assertNotNull(sub);
		}

		@Test
		void build() {
			// Build sub-pass
			sub
					.colour(attachment, COLOR_ATTACHMENT_OPTIMAL)
					.depth(depth, DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.build();

			// Populate descriptor
			final var desc = new VkSubpassDescription();
			sub.populate(desc);

			// Check depth-stencil attachment
			final VkAttachmentReference ref = desc.pDepthStencilAttachment;
			assertNotNull(ref);
			assertEquals(1, ref.attachment);
			assertEquals(DEPTH_STENCIL_ATTACHMENT_OPTIMAL, ref.layout);
		}

		@Test
		void buildEmptyAttachments() {
			assertThrows(IllegalArgumentException.class, "No attachments specified", () -> sub.build());
		}

		@Test
		void colourDuplicateAttachment() {
			sub.colour(attachment, COLOR_ATTACHMENT_OPTIMAL);
			assertThrows(IllegalArgumentException.class, "Duplicate colour attachment", () -> sub.colour(attachment, COLOR_ATTACHMENT_OPTIMAL));
		}

		@Test
		void depthDuplicateAttachment() {
			sub.depth(depth, DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			assertThrows(IllegalArgumentException.class, "Depth attachment already configured", () -> sub.depth(depth, DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
		}
	}

	@Nested
	class DependencyTests {
		/**
		 * Adds a sub-pass dependency.
		 */
		private void dependency(int src, int dest) {
			builder
				.dependency(src, dest)
					.source()
						.stage(VkPipelineStage.FRAGMENT_SHADER)
						.build()
					.destination()
						.stage(VkPipelineStage.FRAGMENT_SHADER)
						.build()
					.build();
		}

		@BeforeEach
		void before() {
			subpass();
		}

		@Test
		void dependency() {
			// Add a dependency
			subpass();
			dependency(0, 1);

			// Check dependencies
			final VkRenderPassCreateInfo info = build();
			assertEquals(1, info.dependencyCount);

			// Check dependency descriptor
			final VkSubpassDependency dep = info.pDependencies;
			assertNotNull(dep);
			assertEquals(0, dep.dependencyFlags);
			// TODO
		}

		@Test
		void dependencyImplicitBefore() {
			dependency(VK_SUBPASS_EXTERNAL, 0);
		}

		@Test
		void dependencyImplicitAfter() {
			dependency(0, VK_SUBPASS_EXTERNAL);
		}

		@Test
		void buildDependencyRequiresPipelineStage() {
			// Check source requires pipeline stage
			final String error = "No pipeline stage(s)";
			final DependencyBuilder dep = builder.dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0);
			assertThrows(IllegalArgumentException.class, error, () -> dep.build());
			assertThrows(IllegalArgumentException.class, error, () -> builder.build(dev));

			// Check destination requires pipeline stage
			dep.source().stage(VkPipelineStage.FRAGMENT_SHADER);
			assertThrows(IllegalArgumentException.class, error, () -> dep.build());
			assertThrows(IllegalArgumentException.class, error, () -> builder.build(dev));
		}

		@Test
		void dependencyInvalidSubPassIndex() {
			assertThrows(IllegalArgumentException.class, "Invalid sub-pass index", () -> builder.dependency(0, 1));
		}

		@Test
		void dependencyEqualIndex() {
			assertThrows(IllegalArgumentException.class, "Invalid dependency indices", () -> builder.dependency(0, 0));
			assertThrows(IllegalArgumentException.class, "Invalid implicit indices", () -> builder.dependency(RenderPass.VK_SUBPASS_EXTERNAL, RenderPass.VK_SUBPASS_EXTERNAL));
		}
	}
}
