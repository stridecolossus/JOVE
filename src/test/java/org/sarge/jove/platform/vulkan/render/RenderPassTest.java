package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.Reference;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Subpass subpass;
	private Attachment attachment;
	private Reference ref;

	@BeforeEach
	void before() {
		// Create attachment
		attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		// Create sub-pass
		ref = new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		subpass = new Subpass(List.of(ref), null);

		// Add a dependency
		subpass
				.dependency()
				.subpass(Subpass.EXTERNAL)
				.source()
					.stage(VkPipelineStage.VERTEX_SHADER)
					.access(VkAccess.SHADER_READ)
					.build()
				.destination()
					.stage(VkPipelineStage.VERTEX_SHADER)
					.access(VkAccess.SHADER_READ)
					.build()
				.build();

		// Create render pass
		pass = RenderPass.create(dev, List.of(subpass));
	}

	@Test
	void constructor() {
		assertNotNull(pass);
		assertEquals(List.of(attachment), pass.attachments());
	}

	@Test
	void create() {
		final var expected = new VkRenderPassCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				// Check descriptor
				final var info = (VkRenderPassCreateInfo) obj;
				assertEquals(0, info.flags);

				// Check attachments
				assertEquals(1, info.attachmentCount);
				assertEquals(FORMAT, info.pAttachments.format);
				assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, info.pAttachments.finalLayout);

				// Check sub-passes
				assertEquals(1, info.subpassCount);
				assertEquals(1, info.pSubpasses.colorAttachmentCount);
				assertEquals(null, info.pSubpasses.pDepthStencilAttachment);

				// Check dependencies
				assertEquals(1, info.dependencyCount);
				assertEquals(0, info.pDependencies.dependencyFlags);

				// Check source dependency
				assertEquals(-1, info.pDependencies.srcSubpass);
				assertEquals(VkPipelineStage.VERTEX_SHADER.value(), info.pDependencies.srcStageMask);
				assertEquals(VkAccess.SHADER_READ.value(), info.pDependencies.srcAccessMask);

				// Check destination dependency
				assertEquals(0, info.pDependencies.dstSubpass);
				assertEquals(VkPipelineStage.VERTEX_SHADER.value(), info.pDependencies.dstStageMask);
				assertEquals(VkAccess.SHADER_READ.value(), info.pDependencies.dstAccessMask);

				return true;
			}
		};

		verify(lib).vkCreateRenderPass(dev, expected, null, factory.pointer());
	}

	@DisplayName("The attachments for a render pass is the aggregate of the sub-passes")
	@Test
	void dependencies() {
		final Subpass other = new Subpass(List.of(ref), null);
		final RenderPass pass = RenderPass.create(dev, List.of(subpass, other));
		assertEquals(List.of(attachment), pass.attachments());
	}

	@DisplayName("Cannot create a render-pass without any sub-passes")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of()));
	}

	@DisplayName("A render pass cannot contain an explicit EXTERNAL or SELF sub-pass")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(Subpass.EXTERNAL)));
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(Subpass.SELF)));
	}

	@DisplayName("A render pass cannot contain a dependency to a sub-pass that is not a member of the render pass")
	@Test
	void missing() {
		final Subpass missing = new Subpass(List.of(), ref);
		subpass
				.dependency()
				.subpass(missing)
				.source()
					.stage(VkPipelineStage.VERTEX_SHADER)
					.build()
				.destination()
					.stage(VkPipelineStage.VERTEX_SHADER)
					.build()
				.build();
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(subpass)));
	}

	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}
}
