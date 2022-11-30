package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Attachment col, depth;

	@BeforeEach
	void before() {
		col = Attachment.colour(FORMAT);
		depth = Attachment.depth(FORMAT);
		pass = new RenderPass(new Handle(1), dev, List.of(col, depth));
	}

	@Test
	void constructor() {
		assertEquals(dev, pass.device());
		assertEquals(false, pass.isDestroyed());
		assertEquals(List.of(col, depth), pass.attachments());
	}

	@DisplayName("A render pass can be advanced to the next subpass")
	@Test
	void next() {
		final var buffer = mock(Command.Buffer.class);
		final Command next = Subpass.next();
		next.record(lib, buffer);
		verify(lib).vkCmdNextSubpass(buffer, VkSubpassContents.INLINE);
	}

	@Test
	void granularity() {
		final VkExtent2D area = pass.granularity();
		verify(lib).vkGetRenderAreaGranularity(dev, pass, area);
	}

	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}

	@Nested
	class BuilderTests {
		private RenderPass.Builder builder;

		@BeforeEach
		void before() {
			builder = new RenderPass.Builder();
		}

		@DisplayName("A render-pass must contain at least one subpass")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@DisplayName("A subpass can be added to a render-pass")
		@Test
		void build() {
			// Builder render-pass with a single subpass
			pass = builder
					.subpass()
						.colour(col)
						.build()
					.build(dev);

			// Check render-pass
			assertNotNull(pass);
			assertEquals(List.of(col), pass.attachments());

			// Check API
			final var expected = new VkRenderPassCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var actual = (VkRenderPassCreateInfo) obj;
					assertEquals(0, actual.flags);
					assertEquals(1, actual.attachmentCount);
					assertEquals(1, actual.subpassCount);
					assertEquals(0, actual.dependencyCount);
					return true;
				}
			};
			verify(lib).vkCreateRenderPass(dev, expected, null, factory.pointer());
		}

		@DisplayName("A render-pass contains the aggregated set of attachments for its sub-passes")
		@Test
		void attachments() {
			pass = builder
					.subpass()
						.colour(col)
						.build()
					.subpass()
						.depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
						.build()
					.build(dev);

			assertEquals(List.of(col, depth), pass.attachments());
		}
	}
}
