package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.AttachmentDescription.LoadStore;
import org.sarge.jove.util.EnumMask;

class AttachmentDescriptionTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	private AttachmentDescription attachment;

	@BeforeEach
	void before() {
		attachment = new AttachmentDescription(
				FORMAT,
				VkSampleCountFlags.COUNT_1,
				new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE),
				LoadStore.DONT_CARE,
				VkImageLayout.UNDEFINED,
				VkImageLayout.PRESENT_SRC_KHR
		);
	}

	@Nested
	class BuilderTest {
		private AttachmentDescription.Builder builder;

		@BeforeEach
		void before() {
			builder = new AttachmentDescription.Builder();
			builder.format(FORMAT);
		}

		@DisplayName("The image format of an attachment cannot be undefined")
		@Test
		void undefined() {
			assertThrows(NullPointerException.class, () -> builder.format(VkFormat.UNDEFINED).build());
		}

		@DisplayName("The final image layout of an attachment cannot be undefined")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> builder.finalLayout(VkImageLayout.UNDEFINED).build());
			assertThrows(IllegalArgumentException.class, () -> builder.finalLayout(VkImageLayout.PREINITIALIZED).build());
		}

		@DisplayName("The initial image layout of an attachment cannot be undefined if the image is loaded")
		@Test
		void load() {
			final LoadStore op = new LoadStore(VkAttachmentLoadOp.LOAD, VkAttachmentStoreOp.DONT_CARE);
			assertThrows(NullPointerException.class, () -> new AttachmentDescription.Builder().operation(op).build());
			assertThrows(NullPointerException.class, () -> new AttachmentDescription.Builder().stencil(op).build());
		}
	}

	@Test
	void populate() {
		final var descriptor = attachment.populate();
		assertEquals(FORMAT, descriptor.format);
		assertEquals(new EnumMask<>(VkSampleCountFlags.COUNT_1), descriptor.samples);
		assertEquals(VkAttachmentLoadOp.CLEAR, descriptor.loadOp);
		assertEquals(VkAttachmentStoreOp.STORE, descriptor.storeOp);
		assertEquals(VkAttachmentLoadOp.DONT_CARE, descriptor.stencilLoadOp);
		assertEquals(VkAttachmentStoreOp.DONT_CARE, descriptor.stencilStoreOp);
		assertEquals(VkImageLayout.UNDEFINED, descriptor.initialLayout);
		assertEquals(VkImageLayout.PRESENT_SRC_KHR, descriptor.finalLayout);
	}

	@Test
	void colour() {
		assertEquals(attachment, AttachmentDescription.colour(FORMAT));
	}

	@Test
	void depth() {
		final var expected = new AttachmentDescription.Builder()
				.format(VkFormat.D32_SFLOAT)
				.operation(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.DONT_CARE))
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();

		assertEquals(expected, AttachmentDescription.depth(VkFormat.D32_SFLOAT));
	}

	@Test
	void equals() {
		assertEquals(attachment, attachment);
		assertNotEquals(attachment, null);
	}
}
