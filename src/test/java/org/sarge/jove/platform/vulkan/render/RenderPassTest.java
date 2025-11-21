package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Dependency.Properties;
import org.sarge.jove.platform.vulkan.render.Subpass.AttachmentReference;

class RenderPassTest {
	private static class MockRenderPassLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass) {
			assertNotNull(device);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(1, pCreateInfo.attachmentCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.subpassCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.dependencyCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			pRenderPass.set(MemorySegment.ofAddress(2));
			return VkResult.SUCCESS;
		}
	}

	private RenderPass pass;
	private MockRenderPassLibrary library;

	@BeforeEach
	void before() {
		final var attachment = Attachment.colour(VkFormat.R32G32B32A32_SFLOAT);
		final var subpass = new Subpass(List.of(new AttachmentReference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)), null, Set.of());

		final var dependency = new Dependency(
				new Properties(subpass, Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of()),
				new Properties(Dependency.VK_SUBPASS_EXTERNAL, Set.of(VkPipelineStage.VERTEX_SHADER), Set.of()),
				Set.of()
		);

		library = new MockRenderPassLibrary();

		pass = new RenderPass.Builder()
				.add(subpass)
				.dependency(dependency)
				.build(new MockLogicalDevice(library));
	}

	@Test
	void granularity() {
	}

	@Test
	void destroy() {
		pass.destroy();
		assertEquals(true, pass.isDestroyed());
	}
}
