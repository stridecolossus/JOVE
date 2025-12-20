package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.platform.vulkan.render.Dependency.Properties;
import org.sarge.jove.util.*;

class RenderPassTest {
	@SuppressWarnings("unused")
	private static class MockRenderPassLibrary extends MockLibrary {
		public VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass) {
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.attachmentCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.subpassCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.dependencyCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			init(pRenderPass);
			return VkResult.VK_SUCCESS;
		}
	}

	private RenderPass pass;
	private Attachment attachment;
	private Mockery mockery;

	@BeforeEach
	void before() {
		// Create an attachment
		final var description = AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT);
		attachment = new Attachment(AttachmentType.COLOUR, description, _ -> null);

		// Create subpass
		final var subpass = new Subpass(Set.of(), List.of(attachment.reference()));

		// Specify a dependency on the start of the render pass
		final var dependency = new Dependency(
				new Properties(subpass, Set.of(VkPipelineStageFlags.FRAGMENT_SHADER), Set.of()),
				new Properties(Dependency.VK_SUBPASS_EXTERNAL, Set.of(VkPipelineStageFlags.VERTEX_SHADER), Set.of()),
				Set.of()
		);

		// Init library
		mockery = new Mockery(new MockRenderPassLibrary(), RenderPass.Library.class);

		// Create render pass
		pass = new RenderPass.Builder()
				.add(subpass)
				.dependency(dependency)
				.build(new MockLogicalDevice(mockery.proxy()));
	}

	@Test
	void constructor() {
		assertFalse(pass.isDestroyed());
		assertEquals(List.of(attachment), pass.attachments());
	}

	@Test
	void granularity() {
	}

	@Test
	void empty() {
		final var builder = new RenderPass.Builder();
		assertThrows(IllegalArgumentException.class, () -> builder.build(new MockLogicalDevice(mockery.proxy())));
	}

	@Test
	void destroy() {
		pass.destroy();
		assertTrue(pass.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyRenderPass").count());
	}
}
