package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Attachment.Operations;

class AttachmentTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	private Attachment attachment;
	private Operations op;

	@BeforeEach
	void before() {
		op = new Operations(VkAttachmentLoadOp.CLEAR, VkAttachmentStoreOp.STORE);
		attachment = new Attachment(FORMAT, VkSampleCount.COUNT_1, op, op, VkImageLayout.UNDEFINED, VkImageLayout.PRESENT_SRC_KHR);
	}

	@Test
	void constructor() {
		assertEquals(FORMAT, attachment.format());
		assertEquals(VkSampleCount.COUNT_1, attachment.samples());
		assertEquals(op, attachment.colour());
		assertEquals(op, attachment.stencil());
		assertEquals(VkImageLayout.UNDEFINED, attachment.before());
		assertEquals(VkImageLayout.PRESENT_SRC_KHR, attachment.after());
	}

	@Test
	void populate() {
		final var descriptor = new VkAttachmentDescription();
		attachment.populate(descriptor);
		assertEquals(FORMAT, descriptor.format);
		assertEquals(VkSampleCount.COUNT_1, descriptor.samples);
		assertEquals(VkAttachmentLoadOp.CLEAR, descriptor.loadOp);
		assertEquals(VkAttachmentStoreOp.STORE, descriptor.storeOp);
		assertEquals(VkAttachmentLoadOp.CLEAR, descriptor.stencilLoadOp);
		assertEquals(VkAttachmentStoreOp.STORE, descriptor.stencilStoreOp);
		assertEquals(VkImageLayout.UNDEFINED, descriptor.initialLayout);
		assertEquals(VkImageLayout.PRESENT_SRC_KHR, descriptor.finalLayout);
	}

	@Test
	void finalLayout() {
		assertThrows(IllegalArgumentException.class, () -> Attachment.of(FORMAT, null));
		assertThrows(IllegalArgumentException.class, () -> Attachment.of(FORMAT, VkImageLayout.UNDEFINED));
		assertThrows(IllegalArgumentException.class, () -> Attachment.of(FORMAT, VkImageLayout.PREINITIALIZED));
	}

	@Test
	void equals() {
		assertEquals(attachment, attachment);
		assertNotEquals(attachment, null);
		assertNotEquals(attachment, Attachment.of(FORMAT, VkImageLayout.PRESENT_SRC_KHR));
	}

	@Test
	void builder() {
		final var builder = new Attachment.Builder()
				.format(FORMAT)
				.samples(1)
				.load(VkAttachmentLoadOp.CLEAR)
				.store(VkAttachmentStoreOp.STORE)
				.stencilLoad(VkAttachmentLoadOp.CLEAR)
				.stencilStore(VkAttachmentStoreOp.STORE)
				.initialLayout(VkImageLayout.UNDEFINED)
				.finalLayout(VkImageLayout.PRESENT_SRC_KHR);

		assertEquals(attachment, builder.build());
	}
}
