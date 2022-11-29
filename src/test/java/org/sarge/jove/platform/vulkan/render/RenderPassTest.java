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
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass.Dependency;
import org.sarge.jove.platform.vulkan.render.RenderPass.ClearAttachmentCommandBuilder;
import org.sarge.jove.platform.vulkan.render.RenderPass.ClearAttachmentCommandBuilder.Region;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BitMask;

class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Attachment col, depth;

	@BeforeEach
	void before() {
		col = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		depth = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();

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

		@DisplayName("A subpass...")
		@Nested
		class SubpassTests {
			private Subpass subpass;

			@BeforeEach
			void before() {
				subpass = builder.subpass();
				assertNotNull(subpass);
			}

			@DisplayName("can contain a colour attachment")
			@Test
			void colour() {
				subpass.colour(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
			}

			@DisplayName("can contain a depth-stencil attachment")
			@Test
			void depth() {
				subpass.depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			}

			@DisplayName("must contain at least one attachment")
			@Test
			void empty() {
				assertThrows(IllegalArgumentException.class, () -> subpass.build());
			}

			@DisplayName("cannot contain a duplicate colour attachment")
			@Test
			void duplicate() {
				subpass.colour(col);
				assertThrows(IllegalArgumentException.class, () -> subpass.colour(col));
			}

			@DisplayName("cannot contain the same colour and depth-stencil attachment")
			@Test
			void invalid() {
				subpass.colour(col);
				subpass.depth(col, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
				assertThrows(IllegalArgumentException.class, () -> subpass.build());
			}
		}

		@DisplayName("A subpass dependency...")
		@Nested
		class DependencyTests {
			private Dependency dependency;
			private Subpass subpass;

			@BeforeEach
			void before() {
				subpass = builder.subpass();
				dependency = subpass.dependency();
				assertNotNull(dependency);
			}

			@DisplayName("can be configured between two subpasses")
			@Test
			void dependencies() {
				// Init previous subpass
				subpass.colour(col);

				// Create a second subpass with a dependency
				builder
						.subpass()
							.colour(col)
							.dependency()
								.dependency(subpass)
								.source()
									.stage(VkPipelineStage.TRANSFER)
									.build()
								.destination()
									.stage(VkPipelineStage.TRANSFER)
									.build()
								.build()
							.build()
						.build(dev);

				// Check API
				final var expected = new VkRenderPassCreateInfo() {
					@Override
					public boolean equals(Object obj) {
						final var actual = (VkRenderPassCreateInfo) obj;
						assertEquals(1, actual.dependencyCount);
						return true;
					}
				};
				verify(lib).vkCreateRenderPass(dev, expected, null, factory.pointer());
			}

			@DisplayName("must refer to a previous subpass")
			@Test
			void empty() {
				assertThrows(IllegalArgumentException.class, () -> dependency.build());
			}

			@DisplayName("can refer to the implicit external subpass")
			@Test
			void external() {
				dependency.external();
			}

			@DisplayName("can be self-referential")
			@Test
			void self() {
				dependency.self();
			}

			@DisplayName("must have at least one source pipeline stage")
			@Test
			void source() {
				dependency.dependency(subpass);
				dependency.destination().stage(VkPipelineStage.FRAGMENT_SHADER);
				assertThrows(IllegalArgumentException.class, () -> dependency.build());
			}

			@DisplayName("must have at least one destination pipeline stage")
			@Test
			void destination() {
				dependency.dependency(subpass);
				dependency.source().stage(VkPipelineStage.FRAGMENT_SHADER);
				assertThrows(IllegalArgumentException.class, () -> dependency.build());
			}
		}
	}

	@DisplayName("A clear attachment command...")
	@Nested
	class ClearAttachmentCommandBuilderTests {
		private ClearAttachmentCommandBuilder builder;
		private ColourClearValue white;
		private Rectangle rect;

		@BeforeEach
		void before() {
			builder = pass.new ClearAttachmentCommandBuilder();
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
    			final Attachment other = new Attachment.Builder()
    					.format(FORMAT)
    					.finalLayout(VkImageLayout.GENERAL)
    					.build();

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
}
