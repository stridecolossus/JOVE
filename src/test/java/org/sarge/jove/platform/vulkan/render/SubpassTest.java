package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.render.Subpass.*;
import org.sarge.jove.util.BitMask;

@Nested
class SubpassTest {
	private Subpass subpass;
	private Attachment col, depth;

	@BeforeEach
	void before() {
		final VkFormat format = VkFormat.R32G32B32A32_SFLOAT;
		col = Attachment.colour(format);
		depth = Attachment.depth(format);
		subpass = new Subpass();
	}

	@Test
	void populate() {
		final Reference refColour = new Reference(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		refColour.init(1);
		subpass.colour(refColour);

		final Reference refDepth = new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		refDepth.init(2);
		subpass.depth(refDepth);

		final var desc = new VkSubpassDescription();
		subpass.populate(desc);
		assertEquals(VkPipelineBindPoint.GRAPHICS, desc.pipelineBindPoint);
		assertEquals(1, desc.colorAttachmentCount);
		assertNotNull(desc.pColorAttachments);
		assertNotNull(desc.pDepthStencilAttachment);
	}

	@DisplayName("A render pass can be conveniently created from a single subpass")
	@Test
	void create() {
		final RenderPass pass = subpass.colour(col).create(new MockDeviceContext());
		assertNotNull(pass);
	}

	@DisplayName("A subpass...")
	@Nested
    	class AttachmentTests {
    	@DisplayName("can contain a colour attachment")
    	@Test
    	void colour() {
    		final Reference ref = new Reference(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
    		subpass.colour(ref);
    		assertEquals(List.of(ref), subpass.attachments().toList());
    	}

    	@DisplayName("can contain a depth-stencil attachment")
    	@Test
    	void depth() {
    		final Reference ref = new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
    		subpass.depth(ref);
    		assertEquals(List.of(ref), subpass.attachments().toList());
    	}

    	@DisplayName("cannot contain a duplicate colour attachment")
    	@Test
    	void duplicate() {
    		subpass.colour(col);
    		assertThrows(IllegalArgumentException.class, () -> subpass.colour(col));
    	}

    	@DisplayName("can contain both colour and depth-stencil attachments")
    	@Test
    	void both() {
    		subpass.colour(col);
    		subpass.depth(new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
    		assertEquals(2, subpass.attachments().count());
    	}

    	@DisplayName("cannot contain the same colour and depth-stencil attachment")
    	@Test
    	void invalid() {
    		subpass.colour(col);
    		subpass.depth(new Reference(col, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL));
    		assertThrows(IllegalArgumentException.class, () -> subpass.attachments());
    	}

    	@DisplayName("must contain at least one attachment")
    	@Test
    	void empty() {
    		assertThrows(IllegalArgumentException.class, () -> subpass.attachments());
    	}

    	@Test
    	void reference() {
    		final var info = new VkAttachmentReference();
    		final Reference ref = new Reference(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
    		ref.init(2);
    		ref.populate(info);
    		assertEquals(2, info.attachment);
    		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, info.layout);
    	}
	}

	@DisplayName("A subpass dependency...")
	@Nested
	class DependencyTests {
		private Subpass other;
		private Dependency dependency;
		private VkSubpassDependency desc;

		@BeforeEach
		void before() {
			desc = new VkSubpassDependency();
			other = new Subpass();
			dependency = subpass.dependency();
		}

		@Test
		void constructor() {
			assertNotNull(dependency);
			assertEquals(List.of(dependency), subpass.dependencies().toList());
		}

		@DisplayName("can be configured by a builder")
		@Test
		void build() {
			// Configure a dependency
			dependency
					.subpass(other)
    				.flag(VkDependencyFlag.VIEW_LOCAL)
            		.source()
            			.stage(VkPipelineStage.VERTEX_SHADER)
            			.build()
            		.destination()
            			.stage(VkPipelineStage.FRAGMENT_SHADER)
            			.access(VkAccess.SHADER_READ)
            			.build()
            		.build();

			// Patch subpass indices
			other.init(1);
			subpass.init(2);

			// Check descriptor
			dependency.populate(desc);
			assertEquals(BitMask.reduce(VkDependencyFlag.VIEW_LOCAL), desc.dependencyFlags);
			assertEquals(1, desc.srcSubpass);
			assertEquals(2, desc.dstSubpass);
			assertEquals(BitMask.reduce(VkPipelineStage.VERTEX_SHADER), desc.srcStageMask);
			assertEquals(BitMask.reduce(), desc.srcAccessMask);
			assertEquals(BitMask.reduce(VkPipelineStage.FRAGMENT_SHADER), desc.dstStageMask);
			assertEquals(BitMask.reduce(VkAccess.SHADER_READ), desc.dstAccessMask);
		}

		@DisplayName("can depend on the implicit subpass before or after the render pass")
		@Test
		void implicit() {
			subpass.init(2);
			dependency
					.external()
					.source()
						.stage(VkPipelineStage.VERTEX_SHADER)
						.build()
					.destination()
						.stage(VkPipelineStage.FRAGMENT_SHADER)
						.build()
					.build();
			dependency.populate(desc);
			assertEquals(-1, desc.srcSubpass);
			assertEquals(2, desc.dstSubpass);
		}

		@DisplayName("can be self referential")
		@Test
		void self() {
			subpass.init(2);
			dependency
					.subpass(subpass)
        			.flag(VkDependencyFlag.VIEW_LOCAL)
            		.source()
            			.stage(VkPipelineStage.VERTEX_SHADER)
            			.build()
            		.destination()
            			.stage(VkPipelineStage.FRAGMENT_SHADER)
            			.access(VkAccess.SHADER_READ)
            			.build()
            		.build();
			dependency.populate(desc);
			assertEquals(2, desc.srcSubpass);
			assertEquals(2, desc.dstSubpass);
		}

		@DisplayName("must contain source and destination pipeline stages")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> dependency.build());
		}
	}
}
