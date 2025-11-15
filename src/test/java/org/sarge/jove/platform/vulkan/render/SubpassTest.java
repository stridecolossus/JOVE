package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.AttachmentReference;
import org.sarge.jove.util.EnumMask;

class SubpassTest {
	private Subpass subpass;
	private AttachmentReference colour, depth;

	@BeforeEach
	void before() {
		colour = new AttachmentReference(Attachment.colour(VkFormat.R32G32B32A32_SFLOAT), COLOR_ATTACHMENT_OPTIMAL);
		depth = new AttachmentReference(Attachment.depth(VkFormat.D32_SFLOAT), DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		subpass = new Subpass.Builder()
				.colour(colour)
				.depth(depth)
				.build();
	}

	@Test
	void attachments() {
		assertEquals(List.of(colour, depth), subpass.attachments().toList());
	}

	@Test
	void description() {
		final VkSubpassDescription description = subpass.description(List.of(colour.attachment(), depth.attachment()));
		assertEquals(new EnumMask<>(), description.flags);
		assertEquals(VkPipelineBindPoint.GRAPHICS, description.pipelineBindPoint);
		assertEquals(1, description.colorAttachmentCount);
		assertEquals(1, description.pColorAttachments.length);
		assertNotNull(description.pColorAttachments[0]);
		assertNotNull(description.pDepthStencilAttachment);
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> subpass.description(List.of()));
		assertThrows(IllegalArgumentException.class, () -> subpass.description(List.of(colour.attachment())));
		assertThrows(IllegalArgumentException.class, () -> subpass.description(List.of(depth.attachment())));
	}
}
