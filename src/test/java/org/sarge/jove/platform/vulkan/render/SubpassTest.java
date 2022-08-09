package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.*;

class SubpassTest {
	private Subpass subpass;
	private Attachment attachment;
	private Reference ref;
	private Subpass.Properties props;

	@BeforeEach
	void before() {
		attachment = mock(Attachment.class);
		ref = new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_READ));
		subpass = new Subpass(List.of(ref), null);
	}

	@Test
	void constructor() {
		assertEquals(List.of(ref), subpass.references().toList());
		assertEquals(List.of(), subpass.dependencies().toList());
	}

	@DisplayName("A subpass can contain a depth-stencil attachment")
	@Test
	void depth() {
		subpass = new Subpass(List.of(), ref);
		assertEquals(List.of(ref), subpass.references().toList());
	}

	@DisplayName("A sub-pass must contain at least one attachment")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(), null));
	}

	@DisplayName("A sub-pass connot contain a duplicate colour attachment")
	@Test
	void duplicate() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(ref, ref), null));
	}

	@DisplayName("The depth-stencil attachment cannot duplicate a colour attachment")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new Subpass(List.of(ref), ref));
	}

	@Nested
	class ReferenceTests {
		@Test
		void constructor() {
			assertEquals(attachment, ref.attachment());
		}

		@Test
		void populate() {
			final var descriptor = new VkAttachmentReference();
			ref.index(3);
			ref.populate(descriptor);
			assertEquals(3, descriptor.attachment);
			assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, descriptor.layout);
		}
	}

	@Nested
	class PropertiesTests {
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
	}

	@Nested
	class DependencyTests {
		private Dependency dependency;

		@BeforeEach
		void before() {
			dependency = new Dependency(Pair.of(Subpass.EXTERNAL, props), Pair.of(subpass, props));
		}

		@Test
		void constructor() {
			assertEquals(Pair.of(Subpass.EXTERNAL, props), dependency.source());
			assertEquals(Pair.of(subpass, props), dependency.destination());
		}

		@SuppressWarnings("unused")
		@DisplayName("A subpass can have a self-referential dependency")
		@Test
		void self() {
			final var self = Pair.of(subpass, props);
			new Dependency(self, self);
		}

		@Test
		void populate() {
			final var descriptor = new VkSubpassDependency();
			subpass.index(1);
			dependency.populate(descriptor);
			assertEquals(0, descriptor.dependencyFlags);
			assertEquals(-1, descriptor.srcSubpass);
			assertEquals(1, descriptor.dstSubpass);
			assertEquals(VkPipelineStage.VERTEX_SHADER.value(), descriptor.srcStageMask);
			assertEquals(VkPipelineStage.VERTEX_SHADER.value(), descriptor.dstStageMask);
			assertEquals(VkAccess.SHADER_READ.value(), descriptor.srcAccessMask);
			assertEquals(VkAccess.SHADER_READ.value(), descriptor.dstAccessMask);
		}
	}
}
