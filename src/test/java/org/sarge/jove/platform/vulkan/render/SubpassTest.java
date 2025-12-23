package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.memory.MockAllocator;
import org.sarge.jove.platform.vulkan.present.MockSwapchain;
import org.sarge.jove.util.EnumMask;

class SubpassTest {
	private Subpass subpass;
	private ColourAttachment colour;
	private DepthStencilAttachment depth;

	@BeforeEach
	void before() {
		colour = new ColourAttachment(AttachmentDescription.colour(), () -> new MockSwapchain());
		depth = new DepthStencilAttachment(VkFormat.D32_SFLOAT, AttachmentDescription.depth(), new MockAllocator());
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
