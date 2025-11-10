package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Attachment.LoadStore;

class AttachmentTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = new Attachment(
				FORMAT,
				VkSampleCount.COUNT_1,
				new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE),
				new LoadStore(VkAttachmentLoadOp.DONT_CARE, VkAttachmentStoreOp.DONT_CARE),
				VkImageLayout.UNDEFINED,
				VkImageLayout.PRESENT_SRC_KHR
		);
	}

	@Test
	void constructor() {
		assertEquals(FORMAT, attachment.format());
	}

	@Test
	void populate() {
		final var descriptor = attachment.populate();
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
		assertEquals(attachment, attachment);
		assertNotEquals(attachment, null);
	}

	@Nested
	class BuilderTests {
		private Attachment.Builder builder;

		@BeforeEach
		void before() {
			builder = new Attachment.Builder().format(FORMAT);
		}

		@Test
		void build() {
			builder.samples(1);
			builder.attachment(new LoadStore(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE));
			builder.finalLayout(VkImageLayout.PRESENT_SRC_KHR);
			assertEquals(attachment, builder.build());
		}

		@DisplayName("The image format of an attachment cannot be undefined")
		@Test
		void undefined() {
			assertThrows(NullPointerException.class, () -> new Attachment.Builder().build());
			assertThrows(NullPointerException.class, () -> new Attachment.Builder().format(VkFormat.UNDEFINED).build());
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
			final LoadStore load = new LoadStore(VkAttachmentLoadOp.LOAD, VkAttachmentStoreOp.DONT_CARE);
			assertThrows(NullPointerException.class, () -> builder.attachment(load).build());
			assertThrows(NullPointerException.class, () -> builder.stencil(load).build());
		}
	}
}
