package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemorySegment;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.platform.vulkan.render.Dependency.Properties;
import org.sarge.jove.util.EnumMask;

class RenderPassTest {
	private static class MockRenderPassLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.attachmentCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.subpassCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			assertEquals(1, pCreateInfo.dependencyCount);
			assertEquals(1, pCreateInfo.pAttachments.length);
			pRenderPass.set(MemorySegment.ofAddress(2));
			return VkResult.VK_SUCCESS;
		}
	}

	private RenderPass pass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		// Create an attachment
		final var description = AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT);
		attachment = new Attachment(AttachmentType.COLOUR, description, _ -> null);

		// Create subpass
		final var subpass = new Subpass(Set.of(), List.of(AttachmentReference.of(attachment)));

		// Specify a dependency on the start of the render pass
		final var dependency = new Dependency(
				new Properties(subpass, Set.of(VkPipelineStageFlags.FRAGMENT_SHADER), Set.of()),
				new Properties(Dependency.VK_SUBPASS_EXTERNAL, Set.of(VkPipelineStageFlags.VERTEX_SHADER), Set.of()),
				Set.of()
		);

		// Create render pass
		pass = new RenderPass.Builder()
				.add(subpass)
				.dependency(dependency)
				.build(new MockLogicalDevice(new MockRenderPassLibrary()));
	}

	@Test
	void attachments() {
		assertEquals(List.of(attachment), pass.attachments());
	}

	@Test
	void granularity() {
	}

	@Test
	void empty() {
		final var builder = new RenderPass.Builder();
		assertThrows(IllegalArgumentException.class, () -> builder.build(new MockLogicalDevice()));
	}

	@Test
	void destroy() {
		pass.destroy();
		assertEquals(true, pass.isDestroyed());
	}
}
