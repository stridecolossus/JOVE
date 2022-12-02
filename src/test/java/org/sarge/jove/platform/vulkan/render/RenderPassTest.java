package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.render.Subpass.Dependency;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		attachment = Attachment.colour(FORMAT);
		pass = new RenderPass(new Handle(1), dev, List.of(attachment));
	}

	@Test
	void constructor() {
		assertEquals(dev, pass.device());
		assertEquals(false, pass.isDestroyed());
		assertEquals(List.of(attachment), pass.attachments());
	}

	@DisplayName("A render pass can be advanced to the next subpass")
	@Test
	void next() {
		final var buffer = mock(Command.Buffer.class);
		final Command next = RenderPass.next();
		next.record(lib, buffer);
		verify(lib).vkCmdNextSubpass(buffer, VkSubpassContents.INLINE);
	}

	@DisplayName("The optimal granularity of the render area can be queried from a render pass")
	@Test
	void granularity() {
		final VkExtent2D area = pass.granularity();
		verify(lib).vkGetRenderAreaGranularity(dev, pass, area);
	}

	@DisplayName("A render pass can be destroyed")
	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}

	@DisplayName("A render pass...")
	@Nested
	class CreateTests {
		private Subpass subpass, other;

		@BeforeEach
		void before() {
			subpass = new Subpass();
			other = new Subpass();
		}

		@DisplayName("is created from a list of subpasses")
    	@Test
    	void create() {
			// Create subpass with a dependency
			final Dependency dep = subpass.dependency(other);
			dep.source().stage(VkPipelineStage.VERTEX_SHADER);
			dep.destination().stage(VkPipelineStage.FRAGMENT_SHADER);
			subpass.colour(attachment);
			other.colour(attachment);

			// Create render pass
			RenderPass.create(dev, List.of(subpass, other));

			// Check API
			final var expected = new VkRenderPassCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkRenderPassCreateInfo) obj;
					assertEquals(0, info.flags);
					assertEquals(1, info.attachmentCount);
					assertEquals(2, info.subpassCount);
					assertEquals(1, info.dependencyCount);
					assertNotNull(info.pAttachments);
					assertNotNull(info.pSubpasses);
					assertNotNull(info.pDependencies);
					return true;
				}
			};
			verify(lib).vkCreateRenderPass(dev, expected, null, factory.pointer());
    	}

		@DisplayName("contains the aggregated attachments of its subpasses")
    	@Test
    	void attachments() {
			subpass.colour(attachment);
			other.colour(attachment);
			pass = RenderPass.create(dev, List.of(subpass, other));
			assertEquals(List.of(attachment), pass.attachments());
    	}

		@DisplayName("must contain at least one subpass")
    	@Test
    	void empty() {
			assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of()));
    	}

		@DisplayName("cannot contain a subpass without any attachments")
    	@Test
    	void incomplete() {
			assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(subpass)));
		}

		@DisplayName("must include all dependant subpasses")
    	@Test
    	void dependencies() {
			subpass.colour(attachment);
			other.colour(attachment);
			subpass.dependency(other);
			assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(subpass)));
    	}

		@DisplayName("cannot contain a duplicate subpass")
    	@Test
    	void duplicate() {
			subpass.colour(attachment);
			assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(subpass, subpass)));
    	}
    }
}
