package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		pass = new RenderPass(new Pointer(1), dev, List.of(attachment));
	}

	@Test
	void constructor() {
		assertEquals(dev, pass.device());
		assertEquals(false, pass.isDestroyed());
		assertEquals(List.of(attachment), pass.attachments());
	}

	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}

	@Nested
	class BuilderTests {
		private RenderPass.Builder builder;
		private Attachment depth;

		@BeforeEach
		void before() {
			depth = new Attachment.Builder()
					.format(FORMAT)
					.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
					.build();

			builder = new RenderPass.Builder();
		}

		@DisplayName("Reference indices are allocated by the subpass")
		@Test
		void allocate() {
			final Subpass subpass = builder.subpass();
			assertEquals(0, subpass.allocate());
			assertEquals(1, subpass.allocate());
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
						.colour(attachment)
						.build()
					.build(dev);

			// Check render-pass
			assertNotNull(pass);
			assertEquals(List.of(attachment), pass.attachments());

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
						.colour(attachment)
						.build()
					.subpass()
						.depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
						.build()
					.build(dev);

			assertEquals(List.of(attachment, depth), pass.attachments());
		}

		@DisplayName("A subpass dependency can be configured between two subpasses")
		@Test
		void dependencies() {
			// Create a dependant subpass
			final Subpass other = builder
					.subpass()
					.depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

			// Create a subpass dependency
			builder
					.subpass()
					.colour(attachment)
					.dependency()
						.dependency(other)
						.source()
							.stage(VkPipelineStage.TRANSFER)
							.build()
						.destination()
							.stage(VkPipelineStage.TRANSFER)
							.build()
						.build()
					.build();

			// Build render-pass
			assertNotNull(builder.build(dev));

			// Check API
			final var expected = new VkRenderPassCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var actual = (VkRenderPassCreateInfo) obj;
					assertEquals(0, actual.flags);
					assertEquals(2, actual.attachmentCount);
					assertEquals(2, actual.subpassCount);
					assertEquals(1, actual.dependencyCount);
					return true;
				}
			};
			verify(lib).vkCreateRenderPass(dev, expected, null, factory.pointer());
		}
	}
}
