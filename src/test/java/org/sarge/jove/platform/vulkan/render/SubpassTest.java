package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Subpass.Dependency;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

@DisplayName("A subpass...")
@Nested
class SubpassTest extends AbstractVulkanTest {
	private RenderPass.Builder parent;
	private Subpass subpass;
	private Attachment col, depth;

	@BeforeEach
	void before() {
		col = Attachment.colour(FORMAT);
		depth = Attachment.depth(FORMAT);
		parent = new RenderPass.Builder();
		subpass = new Subpass(parent, 0);
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

    @DisplayName("A subpass dependency...")
    @Nested
    class DependencyTests {
    	private Dependency dependency;

    	@BeforeEach
    	void before() {
    		dependency = subpass.dependency();
    		assertNotNull(dependency);
    	}

    	@DisplayName("can be configured between two subpasses")
    	@Test
    	void dependencies() {
    		// Init previous subpass
    		subpass.colour(col);

    		// Create a second subpass with a dependency
    		parent
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
