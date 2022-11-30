package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.platform.vulkan.render.ClearAttachmentCommandBuilder.Region;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BitMask;

@DisplayName("A clear attachment command...")
class ClearAttachmentCommandBuilderTest extends AbstractVulkanTest {
	private ClearAttachmentCommandBuilder builder;
	private RenderPass pass;
	private Attachment col, depth;
	private ColourClearValue white;
	private Rectangle rect;

	@BeforeEach
	void before() {
		col = Attachment.colour(FORMAT);
		depth = Attachment.depth(FORMAT);
		pass = new RenderPass(new Handle(1), dev, List.of(col, depth));
		builder = new ClearAttachmentCommandBuilder(pass);
		white = new ColourClearValue(Colour.WHITE);
		rect = new Rectangle(1, 2, 3, 4);
	}

	@Nested
	class AttachmentTests {
		@DisplayName("can clear a colour attachment")
		@Test
		void colour() {
			final var clear = builder.new ClearAttachment(col, Set.of(VkImageAspect.COLOR), white);
			builder.attachment(clear);
		}

		@DisplayName("can clear the depth-stencil attachment")
		@Test
		void depth() {
			final var clear = builder.new ClearAttachment(depth, Set.of(VkImageAspect.DEPTH), DepthClearValue.DEFAULT);
			builder.attachment(clear);
		}

		@DisplayName("cannot clear an attachment that does not belong to the render pass")
		@Test
		void invalid() {
			final Attachment other = new Attachment.Builder(FORMAT).finalLayout(VkImageLayout.GENERAL).build();
			assertThrows(IllegalArgumentException.class, () -> builder.new ClearAttachment(other, Set.of(VkImageAspect.COLOR), white));
		}

		@DisplayName("must specify image aspects that are a subset of the attachment")
		@Test
		void aspects() {
			assertThrows(IllegalArgumentException.class, () -> builder.new ClearAttachment(col, Set.of(), DepthClearValue.DEFAULT));
			// TODO - check colour, depth and/or stencil
		}

		@DisplayName("must specify a clear value that matches the type of attachment")
		@Test
		void type() {
			assertThrows(IllegalArgumentException.class, () -> builder.new ClearAttachment(col, Set.of(VkImageAspect.COLOR), DepthClearValue.DEFAULT));
			assertThrows(IllegalArgumentException.class, () -> builder.new ClearAttachment(depth, Set.of(VkImageAspect.DEPTH), white));
		}

		@Test
		void populate() {
			final var clear = builder.new ClearAttachment(depth, Set.of(VkImageAspect.DEPTH), DepthClearValue.DEFAULT);
			final var info = new VkClearAttachment();
			clear.populate(info);
			assertEquals(BitMask.reduce(VkImageAspect.DEPTH), info.aspectMask);
			assertNotNull(info.clearValue);
			assertEquals(1, info.colorAttachment);			// Note this index is actually unused for the depth-stencil
		}
	}

	@Nested
	class RegionTests {
		@DisplayName("can clear a region of the attachments")
		@Test
		void region() {
			builder.region(new Region(rect, 0, 1));
		}

		@Test
		void populate() {
			final Region region = new Region(rect, 0, 1);
			final var info = new VkClearRect();
			region.populate(info);
			assertEquals(1, info.rect.offset.x);
			assertEquals(2, info.rect.offset.y);
			assertEquals(3, info.rect.extent.width);
			assertEquals(4, info.rect.extent.height);
			assertEquals(0, info.baseArrayLayer);
			assertEquals(1, info.layerCount);
		}
	}

	@DisplayName("can be executed")
	@Test
	void build() {
		final Command cmd = builder
				.attachment(builder.new ClearAttachment(col, Set.of(VkImageAspect.COLOR), white))
				.attachment(builder.new ClearAttachment(depth, Set.of(VkImageAspect.DEPTH), DepthClearValue.DEFAULT))
				.region(new Region(rect, 0, 1))
				.build();

		final var buffer = mock(Command.Buffer.class);
		cmd.record(lib, buffer);
		verify(lib).vkCmdClearAttachments(eq(buffer), eq(2), isA(VkClearAttachment.class), eq(1), isA(VkClearRect.class));
	}
}
