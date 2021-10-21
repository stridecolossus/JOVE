package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkAccess;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.render.Subpass.Reference;
import org.sarge.jove.platform.vulkan.render.Subpass.SubpassDependency;
import org.sarge.jove.platform.vulkan.render.Subpass.SubpassDependency.Dependency;

public class SubpassTest {
	private Subpass subpass;
	private Reference colour, depth;
	private SubpassDependency dependency;

	@BeforeEach
	void before() {
		// Create attachment references
		colour = new Reference(mock(Attachment.class), VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		depth = new Reference(mock(Attachment.class), VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

		// Create a sub-pass dependency
		final Dependency dep = new Dependency(Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of(VkAccess.SHADER_READ));
		dependency = new SubpassDependency(mock(Subpass.class), dep, dep);

		// Create sub-pass
		subpass = new Subpass(List.of(colour), depth, List.of(dependency));
	}

	@Test
	void constructor() {
		assertEquals(List.of(colour), subpass.colour());
		assertEquals(Optional.of(depth), subpass.depth());
		assertEquals(List.of(dependency), subpass.dependencies());
	}

	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(), null, List.of()));
	}

	@Test
	void invalidDepthStencil() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(colour), colour, List.of()));
	}

	@Test
	void emptyPipelineStages() {
		assertThrows(IllegalArgumentException.class, () -> new Dependency(Set.of(), Set.of()));
	}

	@Nested
	class BuilderTests {
		private Subpass.Builder builder;

		@BeforeEach
		void before() {
			builder = new Subpass.Builder();
		}

		@Test
		void build() {
			subpass = builder
					.colour(colour)
					.depth(depth)
					.dependency()
						.subpass(dependency.subpass())
						.source()
							.stage(VkPipelineStage.FRAGMENT_SHADER)
							.access(VkAccess.SHADER_READ)
							.build()
						.destination()
							.stage(VkPipelineStage.FRAGMENT_SHADER)
							.access(VkAccess.SHADER_READ)
							.build()
						.build()
					.build();

			assertNotNull(subpass);
			assertEquals(List.of(colour), subpass.colour());
			assertEquals(Optional.of(depth), subpass.depth());
			assertEquals(List.of(dependency), subpass.dependencies());
		}

		@Test
		void self() {
			subpass = builder
					.colour(colour)
					.dependency()
						.self()
						.source()
							.stage(VkPipelineStage.FRAGMENT_SHADER)
							.build()
						.destination()
							.stage(VkPipelineStage.FRAGMENT_SHADER)
							.build()
						.build()
					.build();

			assertNotNull(subpass);
			assertNotNull(subpass.dependencies());
			assertEquals(1, subpass.dependencies().size());

			final SubpassDependency self = subpass.dependencies().iterator().next();
			assertNotNull(self);
			assertEquals(Subpass.SELF, self.subpass());
		}

		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void duplicateDepthStencil() {
			builder.depth(depth);
			assertThrows(IllegalArgumentException.class, () -> builder.depth(depth));
		}
	}
}
