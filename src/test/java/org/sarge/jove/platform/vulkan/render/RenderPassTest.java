package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass.Dependency;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Attachment one, two;

	@BeforeEach
	void before() {
		one = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		two = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
				.build();

		pass = new RenderPass(new Pointer(1), dev, List.of(one));
	}

	@Test
	void constructor() {
		assertEquals(dev, pass.device());
		assertEquals(false, pass.isDestroyed());
		assertEquals(List.of(one), pass.attachments());
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
						.colour(one)
						.build()
					.build(dev);

			// Check render-pass
			assertNotNull(pass);
			assertEquals(List.of(one), pass.attachments());

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
						.colour(one)
						.build()
					.subpass()
						.depth(two, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
						.build()
					.build(dev);

			assertEquals(List.of(one, two), pass.attachments());
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
				subpass.colour(one, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
			}

			@DisplayName("can contain a depth-stencil attachment")
			@Test
			void depth() {
				subpass.depth(two, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
			}

			@DisplayName("must contain at least one attachment")
			@Test
			void empty() {
				assertThrows(IllegalArgumentException.class, () -> subpass.build());
			}

			@DisplayName("cannot contain a duplicate colour attachment")
			@Test
			void duplicate() {
				subpass.colour(one);
				assertThrows(IllegalArgumentException.class, () -> subpass.colour(one));
			}

			@DisplayName("cannot contain the same colour and depth-stencil attachment")
			@Test
			void invalid() {
				subpass.colour(one);
				subpass.depth(one, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
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
				subpass.colour(one);

				// Create a second subpass with a dependency
				builder
						.subpass()
							.colour(one)
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
}
