package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkAccess;
import org.sarge.jove.platform.vulkan.VkAttachmentDescription;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkRenderPassCreateInfo;
import org.sarge.jove.platform.vulkan.render.Subpass.Reference;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Subpass subpass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		// Create an attachment
		attachment = mock(Attachment.class);

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

	@DisplayName("Check the API and descriptor")
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
		verify(attachment).populate(info.pAttachments);

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

	@DisplayName("Create a render-pass with multiple attachments in different sub-passes")
	@Test
	void createMultiple() {
		// Create another attachment
		final Attachment other = mock(Attachment.class);

		// Create a second sub-pass
		final Subpass depth = new Subpass.Builder()
				.depth(new Reference(other, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL))
				.build();

		// Create render-pass with two sub-passes
		pass = RenderPass.create(dev, List.of(subpass, depth));

		// Check attachments
		assertEquals(List.of(attachment, other), pass.attachments());

		// Check attachments populated
		verify(attachment, times(2)).populate(isA(VkAttachmentDescription.class));
	}

	@DisplayName("Create a render-pass with a self-referential sub-pass")
	@Test
	void createSelfDependency() {
		subpass = new Subpass.Builder()
				.colour(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.self()
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
	void close() {
		pass.close();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}
}
