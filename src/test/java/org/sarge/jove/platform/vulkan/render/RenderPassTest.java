package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.Reference;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Subpass subpass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		// Create an attachment
		attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		// Create a sub-pass with a dependency on the start of the render-pass
		subpass = new Subpass.Builder()
				.colour(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(Subpass.EXTERNAL)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.access(VkAccess.COLOR_ATTACHMENT_WRITE)
						.build()
					.build()
				.build();

		// Create render-pass
		pass = RenderPass.create(dev, List.of(subpass));
	}

	@Test
	void constructor() {
		assertNotNull(pass);
		assertEquals(List.of(attachment), pass.attachments());
	}

	// TODO - refactor to new approach
	@DisplayName("A render pass can be created from a group of sub-passes")
	@Test
	void create() {
		// Check API
		final ArgumentCaptor<VkRenderPassCreateInfo> captor = ArgumentCaptor.forClass(VkRenderPassCreateInfo.class);
		verify(lib).vkCreateRenderPass(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check create descriptor
		final VkRenderPassCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);

		// Check attachments
		assertEquals(1, info.attachmentCount);
		assertNotNull(info.pAttachments);
		assertEquals(FORMAT, info.pAttachments.format);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, info.pAttachments.finalLayout);

		// Check sub-passes
		assertEquals(1, info.subpassCount);
		assertNotNull(info.pSubpasses);
		assertEquals(1, info.pSubpasses.colorAttachmentCount);
		assertEquals(null, info.pSubpasses.pDepthStencilAttachment);

		// Check dependencies
		assertEquals(1, info.dependencyCount);
		assertNotNull(info.pDependencies);
		assertEquals(0, info.pDependencies.dependencyFlags);

		// Check source dependency
		assertEquals(-1, info.pDependencies.srcSubpass);
		assertEquals(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT.value(), info.pDependencies.srcStageMask);
		assertEquals(0, info.pDependencies.srcAccessMask);

		// Check destination dependency
		assertEquals(0, info.pDependencies.dstSubpass);
		assertEquals(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT.value(), info.pDependencies.dstStageMask);
		assertEquals(VkAccess.COLOR_ATTACHMENT_WRITE.value(), info.pDependencies.dstAccessMask);
	}

	@DisplayName("Create a render-pass with a self-referential sub-pass")
	@Test
	void createSelfDependency() {
		subpass = new Subpass.Builder()
				.colour(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(Subpass.SELF)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.build()
				.build();

		pass = RenderPass.create(dev, List.of(subpass));
	}

	@DisplayName("Cannot create a render-pass without any sub-passes")
	@Test
	void createEmpty() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of()));
	}

	@DisplayName("Cannot explicitly use the EXTERNAL sub-pass")
	@Test
	void createExternal() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(Subpass.EXTERNAL)));
	}

	@DisplayName("Cannot have a dependency on a sub-pass that is not a member of the render-pass")
	@Test
	void createInvalidDependency() {
		final Subpass invalid = new Subpass.Builder()
				.depth(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(subpass)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.build()
				.build();

		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(invalid)));
	}

	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}
}
