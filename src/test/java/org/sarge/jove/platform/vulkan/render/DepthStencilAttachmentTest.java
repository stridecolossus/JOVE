package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.DepthClearValue;
import org.sarge.jove.platform.vulkan.memory.MockAllocator;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.util.*;

class DepthStencilAttachmentTest {
	private DepthStencilAttachment attachment;

	@BeforeEach
	void before() {
		attachment = new DepthStencilAttachment(VkFormat.D32_SFLOAT, AttachmentDescription.depth(), new MockAllocator()) {
			@Override
			protected List<View> views(LogicalDevice device, Dimensions extents) {
				return List.of(new MockView());
			}
		};
	}

	@Test
	void constructor() {
		assertEquals(AttachmentType.DEPTH, attachment.type());
		assertEquals(AttachmentDescription.depth(), attachment.description());
	}

	@Test
	void reference() {
		final Attachment.Reference ref = attachment.reference();
		assertEquals(attachment, ref.attachment());
		assertEquals(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL, ref.layout());
	}

	@Test
	void views() {
		attachment.recreate(new MockLogicalDevice(), new Dimensions(640, 480));
		assertNotNull(attachment.view(0));
		assertNotNull(attachment.view(42));
	}

	@Test
	void clear() {
		attachment.clear(Percentile.HALF);
		assertEquals(new DepthClearValue(Percentile.HALF), attachment.clear());
	}

	@Test
	void select() {
		final var properties = new VkFormatProperties();
		properties.optimalTilingFeatures = new EnumMask<>(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT);
		assertEquals(VkFormat.D32_SFLOAT, DepthStencilAttachment.format(_ -> properties, DepthStencilAttachment.IMAGE_FORMATS));
	}

	@Test
	void destroy() {
		attachment.recreate(new MockLogicalDevice(), new Dimensions(640, 480));
		final var view = attachment.view(0);
		attachment.destroy();
		assertEquals(true, view.isDestroyed());
	}
}
