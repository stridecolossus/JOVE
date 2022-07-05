package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.*;

@SuppressWarnings("static-method")
class SubpassTest {
	private Subpass subpass;
	private Attachment attachment;
	private Reference ref;

	@BeforeEach
	void before() {
		attachment = mock(Attachment.class);
		ref = new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		subpass = new Subpass(List.of(ref), null, List.of());
	}

	@Test
	void constructor() {
		assertEquals(List.of(ref), subpass.colour());
		assertEquals(Optional.empty(), subpass.depth());
		assertEquals(List.of(), subpass.dependencies());
	}

	@DisplayName("The subpass aggregates the attachments used in that subpass")
	@Test
	void attachments() {
		assertNotNull(subpass.attachments());
		assertEquals(List.of(attachment), subpass.attachments().toList());
	}

	@DisplayName("A subpass can contain a depth-stencil attachment")
	@Test
	void depth() {
		subpass = new Subpass(List.of(), ref, List.of());
		assertEquals(List.of(), subpass.colour());
		assertEquals(Optional.of(ref), subpass.depth());
		assertEquals(List.of(attachment), subpass.attachments().toList());
	}

	@DisplayName("A subpass can have dependencies to previous subpass stages")
	@Test
	void dependency() {
		final var props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of());
		final Dependency dependency = new Dependency(subpass, props, props);
		final Subpass other = new Subpass(List.of(ref), null, List.of(dependency));
		assertEquals(List.of(dependency), other.dependencies());
	}

	@DisplayName("A sub-pass must contain at least one attachment")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(), null, List.of()));
	}

	@DisplayName("A sub-pass connot contain a duplicate colour attachment")
	@Test
	void duplicate() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(ref, ref), null, List.of()));
	}

	@DisplayName("The depth-stencil attachment cannot duplicate a colour attachment")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(ref), ref, List.of()));
	}

	@Test
	void equals() {
		assertEquals(subpass, subpass);
		assertEquals(subpass, new Subpass(List.of(ref), null, List.of()));
		assertNotEquals(subpass, null);
		assertNotEquals(subpass, new Subpass(List.of(), ref, List.of()));
	}

	@Nested
	class ReferenceTests {
		@Test
		void constructor() {
			assertEquals(attachment, ref.attachment());
			assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, ref.layout());
		}

		@Test
		void equals() {
			assertEquals(ref, ref);
			assertEquals(ref, new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
			assertNotEquals(ref, null);
			assertNotEquals(ref, new Reference(attachment, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
		}
	}

	@Nested
	class PropertiesTests {
		private Subpass.Properties props;

		@BeforeEach
		void before() {
			props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_READ));
		}

		@Test
		void constructor() {
			assertEquals(Set.of(VkPipelineStage.VERTEX_SHADER), props.stages());
			assertEquals(Set.of(VkAccess.SHADER_READ), props.access());
		}

		@DisplayName("The properties of a subpass dependency must contain at least one pipeline stage")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new Subpass.Properties(Set.of(), Set.of()));
		}

		@Test
		void equals() {
			assertEquals(props, props);
			assertEquals(props, new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_READ)));
			assertNotEquals(props, null);
			assertNotEquals(props, new Subpass.Properties(Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of()));
		}
	}

	@Nested
	class DependencyTests {
		private Dependency dependency;
		private Subpass.Properties props;

		@BeforeEach
		void before() {
			props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of());
			dependency = new Dependency(subpass, props, props);
		}

		@Test
		void constructor() {
			assertEquals(subpass, dependency.subpass());
			assertEquals(props, dependency.source());
			assertEquals(props, dependency.destination());
		}

		@DisplayName("A subpass can have a self-referential dependency")
		@Test
		void self() {
			dependency = new Dependency(Subpass.SELF, props, props);
			assertEquals(Subpass.SELF, dependency.subpass());
		}

		@DisplayName("A subpass can have a dependency to the implicit external subpass")
		@Test
		void external() {
			dependency = new Dependency(Subpass.EXTERNAL, props, props);
			assertEquals(Subpass.EXTERNAL, dependency.subpass());
		}

		@Test
		void equals() {
			assertEquals(dependency, dependency);
			assertEquals(dependency, new Dependency(subpass, props, props));
			assertNotEquals(dependency, null);
			assertNotEquals(dependency, new Dependency(subpass, props, new Subpass.Properties(Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of())));
		}
	}

	@Nested
	class BuilderTests {
		private Subpass.Builder builder;

		@BeforeEach
		void before() {
			builder = new Subpass.Builder();
		}

		@DisplayName("The builder can construct a subpass with a colour attachment")
		@Test
		void colour() {
			final Subpass subpass = builder.colour(ref).build();
			assertNotNull(subpass);
			assertEquals(List.of(ref), subpass.colour());
			assertEquals(Optional.empty(), subpass.depth());
			assertEquals(List.of(attachment), subpass.attachments().toList());
			assertEquals(List.of(), subpass.dependencies());
		}

		@DisplayName("The builder can construct a subpass with a depth-stencil attachment")
		@Test
		void depth() {
			final Reference ref = new Reference(attachment, VkImageLayout.DEPTH_ATTACHMENT_STENCIL_READ_ONLY_OPTIMAL);
			final Subpass subpass = builder.depth(ref).build();
			assertNotNull(subpass);
			assertEquals(List.of(), subpass.colour());
			assertEquals(Optional.of(ref), subpass.depth());
			assertEquals(List.of(attachment), subpass.attachments().toList());
			assertEquals(List.of(), subpass.dependencies());
		}

		@DisplayName("The builder can construct a subpass dependency")
		@Test
		void dependency() {
			final Subpass subpass = builder
					.colour(ref)
					.dependency()
						.subpass(Subpass.EXTERNAL)
						.source()
							.stage(VkPipelineStage.VERTEX_SHADER)
							.access(VkAccess.SHADER_READ)
							.build()
						.destination()
							.stage(VkPipelineStage.VERTEX_SHADER)
							.access(VkAccess.SHADER_READ)
							.build()
						.build()
					.build();

			final var props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_READ));
			final Dependency dependency = new Dependency(Subpass.EXTERNAL, props, props);
			assertEquals(List.of(dependency), subpass.dependencies());
		}
	}
}
