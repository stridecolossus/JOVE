package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;

class AttachmentTest {
	private Attachment attachment;
	private AttachmentDescription description;
	private View view;

	@BeforeEach
	void before() {
		description = new AttachmentDescription.Builder()
				.format(VkFormat.R32G32B32A32_SFLOAT)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();
		view = new MockView(new MockLogicalDevice());
		attachment = new Attachment(AttachmentType.COLOUR, description, _ -> view);
	}

	@Test
	void constructor() {
		assertEquals("colour", attachment.name());
		assertEquals(AttachmentType.COLOUR, attachment.type());
		assertEquals(description, attachment.description());
		assertEquals(new ClearValue.None(), attachment.clear());
	}

	@Test
	void views() {
		assertEquals(view, attachment.view(0));
	}

	@Test
	void name() {
		attachment.name("name");
		assertEquals("name", attachment.name());
	}

	@Test
	void clear() {
		final var clear = new ColourClearValue(Colour.BLACK);
		attachment.clear(clear);
		assertEquals(clear, attachment.clear());
	}

	@Test
	void clearInvalidType() {
		assertThrows(IllegalArgumentException.class, () -> attachment.clear(DepthClearValue.DEFAULT));
	}
}
