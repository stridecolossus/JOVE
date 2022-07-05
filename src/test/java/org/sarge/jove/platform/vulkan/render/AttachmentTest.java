package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Attachment.Builder;

class AttachmentTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = new Builder()
				.format(FORMAT)
				.load(VkAttachmentLoadOp.CLEAR)
				.store(VkAttachmentStoreOp.STORE)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
				.build();
	}

	@Test
	void constructor() {
		assertEquals(FORMAT, attachment.format());
	}

	@Test
	void populate() {
		final var descriptor = new VkAttachmentDescription();
		attachment.populate(descriptor);
		assertEquals(FORMAT, descriptor.format);
		assertEquals(VkSampleCount.COUNT_1, descriptor.samples);
		assertEquals(VkAttachmentLoadOp.CLEAR, descriptor.loadOp);
		assertEquals(VkAttachmentStoreOp.STORE, descriptor.storeOp);
		assertEquals(VkAttachmentLoadOp.DONT_CARE, descriptor.stencilLoadOp);
		assertEquals(VkAttachmentStoreOp.DONT_CARE, descriptor.stencilStoreOp);
		assertEquals(VkImageLayout.UNDEFINED, descriptor.initialLayout);
		assertEquals(VkImageLayout.PRESENT_SRC_KHR, descriptor.finalLayout);
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

		@Test
		void view() {
			// Init image descriptor
			final var descriptor = new ImageDescriptor.Builder()
					.format(FORMAT)
					.extents(new ImageData.Extents(new Dimensions(2, 3)))
					.aspect(VkImageAspect.COLOR)
					.build();

			// Create image
			final Image image = mock(Image.class);
			final View view = mock(View.class);
			when(image.descriptor()).thenReturn(descriptor);
			when(view.image()).thenReturn(image);

			// Build attachment from image view
			builder
					.format(view)
					.load(VkAttachmentLoadOp.CLEAR)
					.store(VkAttachmentStoreOp.STORE)
					.finalLayout(VkImageLayout.PRESENT_SRC_KHR);

			assertEquals(attachment, builder.build());
		}

		@DisplayName("An attachment must have an image format")
		@Test
		void buildRequiresFormat() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@DisplayName("An attachment must have a final layout")
		@Test
		void buildRequiresFinalLayout() {
			builder.format(FORMAT);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		// TODO
		// - format matches usage, e.g. stencil load op only if stencil format => VkFormatFeatures?
	}
}
