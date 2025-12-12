package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;

class AttachmentReferenceTest {
	private AttachmentReference reference;
	private Attachment colour;

	@BeforeEach
	void before() {
		final var description = AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT);
		colour = new Attachment(AttachmentType.COLOUR, description, _ -> null);
		reference = new AttachmentReference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
	}

	@Test
	void descriptor() {
		final VkAttachmentReference descriptor = reference.descriptor(3);
		assertEquals(3, descriptor.attachment);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, descriptor.layout);
	}

	@Test
	void colour() {
		assertEquals(reference, AttachmentReference.of(colour));
	}

	@Test
	void depth() {
		final var description = AttachmentDescription.depth(VkFormat.D32_SFLOAT);
		final Attachment depth = new Attachment(AttachmentType.DEPTH, description, _ -> null);
		final var ref = AttachmentReference.of(depth);
		assertEquals(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL, ref.layout());
	}
}
