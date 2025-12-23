package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.present.MockSwapchain;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;

class ColourAttachmentTest {
	private ColourAttachment attachment;

	@BeforeEach
	void before() {
		attachment = new ColourAttachment(AttachmentDescription.colour(), () -> new MockSwapchain()) {
			@Override
			protected List<View> views(LogicalDevice device, Dimensions extents) {
				return List.of(new MockView());
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(AttachmentType.COLOUR, attachment.type());
		assertEquals(AttachmentDescription.colour(), attachment.description());
	}

	@Test
	void reference() {
		final Attachment.Reference ref = attachment.reference();
		assertEquals(attachment, ref.attachment());
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, ref.layout());
	}

	@Test
	void views() {
		attachment.recreate(new MockLogicalDevice(), new Dimensions(640, 480));
		assertNotNull(attachment.view(0));
	}

	@Test
	void clear() {
		attachment.clear(Colour.WHITE);
		assertEquals(new ColourClearValue(Colour.WHITE), attachment.clear());
	}

	@Test
	void destroy() {
		attachment.recreate(new MockLogicalDevice(), new Dimensions(640, 480));
		final var view = attachment.view(0);
		attachment.destroy();
		assertEquals(true, view.isDestroyed());
	}
}
