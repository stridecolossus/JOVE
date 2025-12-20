package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.IntFunction;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.util.EnumMask;

class SubpassTest {
	private Subpass subpass;
	private Attachment colour, depth;

	@BeforeEach
	void before() {
		final IntFunction<View> views = _ -> new MockView();
		colour = new Attachment(AttachmentType.COLOUR, AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT), views);
		depth = new Attachment(AttachmentType.DEPTH, AttachmentDescription.depth(VkFormat.D32_SFLOAT), views);
		subpass = new Subpass(Set.of(), List.of(colour.reference(), depth.reference()));
	}

	@Test
	void description() {
		// Check subpass descriptor
		final VkSubpassDescription description = subpass.description(List.of(colour, depth));
		assertEquals(new EnumMask<>(), description.flags);
		assertEquals(VkPipelineBindPoint.GRAPHICS, description.pipelineBindPoint);
		assertEquals(1, description.colorAttachmentCount);
		assertEquals(1, description.pColorAttachments.length);

		// Check colour attachment descriptor
		final VkAttachmentReference colour = description.pColorAttachments[0];
		assertEquals(0, colour.attachment);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, colour.layout);

		// Check depth attachment descriptor
		final VkAttachmentReference depth = description.pDepthStencilAttachment;
		assertEquals(1, depth.attachment);
		assertEquals(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL, depth.layout);
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> subpass.description(List.of()));
	}
}
