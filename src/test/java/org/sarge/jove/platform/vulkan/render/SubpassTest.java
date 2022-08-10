package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.*;

class SubpassTest {
	private Subpass subpass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = new Attachment(VkFormat.UNDEFINED, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

		subpass = new Subpass(1) {
			@Override
			protected int allocate() {
				return 2;
			}
		};
	}

	@Test
	void constructor() {
		assertNotNull(subpass.references());
		assertNotNull(subpass.dependencies());
	}

	@DisplayName("A subpass can contain a colour attachment")
	@Test
	void colour() {
		final Reference ref = new Reference(2, attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		subpass.colour(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		assertEquals(List.of(ref), subpass.references().toList());
	}

	@DisplayName("A subpass can contain a depth-stencil attachment")
	@Test
	void depth() {
		final Reference ref = new Reference(2, attachment, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		subpass.depth(attachment, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		assertEquals(List.of(ref), subpass.references().toList());
	}

	@DisplayName("A sub-pass must contain at least one attachment")
	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> subpass.build());
	}

	@DisplayName("A sub-pass connot contain a duplicate colour attachment")
	@Test
	void duplicate() {
		subpass.colour(attachment);
		assertThrows(IllegalArgumentException.class, () -> subpass.colour(attachment));
	}

	@DisplayName("The depth-stencil attachment cannot duplicate a colour attachment")
	@Test
	void invalid() {
		subpass.colour(attachment);
		subpass.depth(attachment, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		assertThrows(IllegalArgumentException.class, () -> subpass.build());
	}

	@Test
	void populate() {
		// Add attachments
		subpass.colour(attachment);
		subpass.depth(new Attachment(VkFormat.UNDEFINED, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL), VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

		// Populate subpass descriptor
		final var description = new VkSubpassDescription();
		subpass.build();
		subpass.populate(description);

		// Check descriptor
		assertEquals(0, description.flags);
		assertEquals(VkPipelineBindPoint.GRAPHICS, description.pipelineBindPoint);

		// Check colour attachment
		assertEquals(1, description.colorAttachmentCount);
		assertEquals(2, description.pColorAttachments.attachment);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, description.pColorAttachments.layout);

		// Check depth-stencil attachment
		assertEquals(2, description.pDepthStencilAttachment.attachment);
		assertEquals(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL, description.pDepthStencilAttachment.layout);
	}

	@Nested
	class ReferenceTests {
		private Reference ref;

		@BeforeEach
		void before() {
			ref = new Reference(2, attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		}

		@Test
		void constructor() {
			assertEquals(attachment, ref.attachment());
		}

		@Test
		void populate() {
			final var descriptor = new VkAttachmentReference();
			ref.populate(descriptor);
			assertEquals(2, descriptor.attachment);
			assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, descriptor.layout);
		}
	}

	@DisplayName("A subpass dependency...")
	@Nested
	class DependencyTests {
		private Dependency dependency;
		private Subpass other;
		private VkSubpassDependency info;

		@BeforeEach
		void before() {
			info = new VkSubpassDependency();
			other = new Subpass(3);
			dependency = subpass.dependency();
			assertNotNull(dependency);
		}

		private void init() {
			dependency
					.source()
						.stage(VkPipelineStage.VERTEX_SHADER)
						.access(VkAccess.SHADER_READ)
						.build()
					.destination()
						.stage(VkPipelineStage.FRAGMENT_SHADER)
						.access(VkAccess.SHADER_WRITE)
						.build();
		}

		@Test
		void build() {
			init();
			assertEquals(subpass, dependency.dependency(other).build());
			dependency.populate(info);
			assertEquals(3, info.srcSubpass);
			assertEquals(1, info.dstSubpass);
			assertEquals(VkPipelineStage.VERTEX_SHADER.value(), info.srcStageMask);
			assertEquals(VkAccess.SHADER_READ.value(), info.srcAccessMask);
			assertEquals(VkPipelineStage.FRAGMENT_SHADER.value(), info.dstStageMask);
			assertEquals(VkAccess.SHADER_WRITE.value(), info.dstAccessMask);
		}

		@DisplayName("can refer to the implicit external subpass")
		@Test
		void external() {
			init();
			dependency.external().build();
			dependency.populate(info);
			assertEquals(-1, info.srcSubpass);
		}

		@DisplayName("can be self-referential")
		@Test
		void self() {
			init();
			dependency.self().build();
			dependency.populate(info);
			assertEquals(1, info.srcSubpass);
		}

		@DisplayName("must refer to a dependant subpass")
		@Test
		void empty() {
			init();
			assertThrows(IllegalArgumentException.class, () -> dependency.build());
		}

		@DisplayName("must have at least one source pipeline stage")
		@Test
		void source() {
			dependency.dependency(other);
			dependency.destination().stage(VkPipelineStage.FRAGMENT_SHADER);
			assertThrows(IllegalArgumentException.class, () -> dependency.build());
		}

		@DisplayName("must have at least one destination pipeline stage")
		@Test
		void destination() {
			dependency.dependency(other);
			dependency.source().stage(VkPipelineStage.VERTEX_SHADER);
			assertThrows(IllegalArgumentException.class, () -> dependency.build());
		}
	}
}
