package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.util.Percentile;

class AttachmentTest {
	private View view;

	@BeforeEach
	void before() {
		view = new MockView(new MockLogicalDevice());
	}

	@Nested
	class ColourAttachment {
		private Attachment colour;

		@BeforeEach
		void before() {
			final var description = AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT);
			colour = new Attachment(AttachmentType.COLOUR, description, _ -> view);
		}

		@Test
		void constructor() {
			assertEquals("colour", colour.name());
			assertEquals(AttachmentType.COLOUR, colour.type());
			assertEquals(new ColourClearValue(Colour.BLACK), colour.clear());
		}

		@Test
		void clear() {
			final var clear = new ColourClearValue(Colour.WHITE);
			colour.clear(clear);
			assertEquals(clear, colour.clear());
			assertThrows(IllegalArgumentException.class, () -> colour.clear(DepthClearValue.DEFAULT));
		}

		@Test
		void reference() {
			assertEquals(new AttachmentReference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL), colour.reference());
		}

		@Test
		void views() {
			assertEquals(view, colour.view(0));
		}

		@Test
		void name() {
			colour.name("whatever");
			assertEquals("whatever", colour.name());
		}

		@Test
		void equals() {
			assertEquals(colour, colour);
			assertNotEquals(colour, null);
			assertNotEquals(colour, new Attachment(AttachmentType.COLOUR, AttachmentDescription.colour(VkFormat.R16_SFLOAT), _ -> view));
		}
	}

	@Nested
	class DepthStencilAttachment {
		private Attachment depth;

		@BeforeEach
		void before() {
			final var description = AttachmentDescription.depth(VkFormat.D32_SFLOAT);
			depth = new Attachment(AttachmentType.DEPTH, description, _ -> null);
		}

		@Test
		void constructor() {
			assertEquals("depth", depth.name());
			assertEquals(AttachmentType.DEPTH, depth.type());
			assertEquals(new DepthClearValue(Percentile.ONE), depth.clear());
		}

		@Test
		void reference() {
			assertEquals(new AttachmentReference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL), depth.reference());
		}
	}
}
