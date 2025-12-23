package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.memory.MockAllocator;
import org.sarge.jove.platform.vulkan.render.AttachmentDescription.LoadStore;

class AbstractAttachmentTest {
	private static class MockAttachment extends AbstractAttachment {
		public MockAttachment(AttachmentDescription description) {
			super(AttachmentType.COLOUR, description);
		}

		@Override
		public VkFormat format() {
			return VkFormat.B8G8R8_UNORM;
		}

		@Override
		public Reference reference() {
			return null;
		}

		@Override
		protected List<View> views(LogicalDevice device, Dimensions extents) {
			return List.of(new MockView());
		}
	}

	private AbstractAttachment attachment;

	@BeforeEach
	void before() {
		attachment = new MockAttachment(AttachmentDescription.colour());
	}

	@Nested
	class ViewTest {
		@BeforeEach
		void before() {
			attachment.recreate(new MockLogicalDevice(), new Dimensions(640, 480));
		}

		@Test
		void view() {
			assertNotNull(attachment.view(0));
		}

		@Test
		void destroy() {
			final var view = attachment.view(0);
			attachment.destroy();
			assertTrue(view.isDestroyed());
		}
	}

	@Nested
	class ClearValueTest {
		@Test
		void none() {
			assertEquals(new ClearValue.None(), attachment.clear());
		}

		@Test
		void set() {
			final var clear = new ColourClearValue(Colour.WHITE);
			attachment.clear(clear);
			assertEquals(clear, attachment.clear());
		}

		@Test
		void invalid() {
			final var description = new AttachmentDescription.Builder()
					.operation(LoadStore.DONT_CARE)
					.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
					.build();

			assertThrows(IllegalStateException.class, () -> new MockAttachment(description).clear(new ColourClearValue(Colour.WHITE)));
		}
	}

	@Test
	void equals() {
		assertEquals(attachment, attachment);
		assertEquals(attachment, new MockAttachment(AttachmentDescription.colour()));
		assertNotEquals(attachment, null);
		assertNotEquals(attachment, new DepthStencilAttachment(VkFormat.D32_SFLOAT, AttachmentDescription.depth(), new MockAllocator()));
	}
}
