package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkAttachmentDescription;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.VkAttachmentStoreOp;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkSampleCount;
import org.sarge.jove.platform.vulkan.render.Attachment.Builder;

public class AttachmentTest {
	private static final VkFormat COLOUR = VkFormat.R32G32B32A32_SFLOAT;
	private static final VkFormat DEPTH = VkFormat.D32_SFLOAT;

	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = new Builder()
				.format(COLOUR)
				.load(VkAttachmentLoadOp.CLEAR)
				.store(VkAttachmentStoreOp.STORE)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();
	}

	@Test
	void constructor() {
		assertEquals(COLOUR, attachment.format());
	}

	@Test
	void equals() {
		assertEquals(true, attachment.equals(attachment));
		assertEquals(false, attachment.equals(null));
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@DisplayName("Build a colour attachment")
		@Test
		void colour() {
			final Attachment attachment = builder
					.format(COLOUR)
					.load(VkAttachmentLoadOp.CLEAR)
					.store(VkAttachmentStoreOp.STORE)
					.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
					.build();

			final var desc = new VkAttachmentDescription();
			assertNotNull(attachment);
			attachment.populate(desc);

			assertEquals(COLOUR, desc.format);
			assertEquals(VkSampleCount.COUNT_1, desc.samples);
			assertEquals(VkAttachmentLoadOp.CLEAR, desc.loadOp);
			assertEquals(VkAttachmentStoreOp.STORE, desc.storeOp);
			assertEquals(VkAttachmentLoadOp.DONT_CARE, desc.stencilLoadOp);
			assertEquals(VkAttachmentStoreOp.DONT_CARE, desc.stencilStoreOp);
			assertEquals(VkImageLayout.UNDEFINED, desc.initialLayout);
			assertEquals(VkImageLayout.PRESENT_SRC_KHR, desc.finalLayout);
		}

		@DisplayName("Build a depth-stencil attachment")
		@Test
		void depth() {
			final Attachment attachment = builder
					.format(DEPTH)
					.load(VkAttachmentLoadOp.CLEAR)
					.stencilLoad(VkAttachmentLoadOp.CLEAR)
					.finalLayout(VkImageLayout.DEPTH_STENCIL_READ_ONLY_OPTIMAL)
					.build();

			final var desc = new VkAttachmentDescription();
			assertNotNull(attachment);
			attachment.populate(desc);

			assertEquals(DEPTH, desc.format);
			assertEquals(VkSampleCount.COUNT_1, desc.samples);
			assertEquals(VkAttachmentLoadOp.CLEAR, desc.loadOp);
			assertEquals(VkAttachmentStoreOp.DONT_CARE, desc.storeOp);
			assertEquals(VkAttachmentLoadOp.CLEAR, desc.stencilLoadOp);
			assertEquals(VkAttachmentStoreOp.DONT_CARE, desc.stencilStoreOp);
			assertEquals(VkImageLayout.UNDEFINED, desc.initialLayout);
			assertEquals(VkImageLayout.DEPTH_STENCIL_READ_ONLY_OPTIMAL, desc.finalLayout);
		}

		@DisplayName("An attachment must have an image format")
		@Test
		void buildRequiresFormat() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("An attachment must have a final layout")
		@Test
		void buildRequiresFinalLayout() {
			builder.format(COLOUR);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		// TODO
		// - format matches usage, e.g. stencil load op only if stencil format => VkFormatFeatures?
	}
}
