package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.RenderPass.Group;
import org.sarge.jove.platform.vulkan.render.Subpass.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class RenderPassTest extends AbstractVulkanTest {
	private RenderPass pass;
	private Subpass subpass;
	private Attachment attachment;

	@BeforeEach
	void before() {
		// Create an attachment
		attachment = new Attachment.Builder()
				.format(FORMAT)
				.finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
				.build();

		// Create a sub-pass with a dependency on the start of the render-pass
		subpass = new Subpass.Builder()
				.colour(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(Subpass.EXTERNAL)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.access(VkAccess.COLOR_ATTACHMENT_WRITE)
						.build()
					.build()
				.build();

		// Create render-pass
		pass = RenderPass.create(dev, List.of(subpass));
	}

	@Test
	void constructor() {
		assertNotNull(pass);
		assertEquals(List.of(attachment), pass.attachments());
	}

	// TODO - refactor to new approach
	@DisplayName("A render pass can be created from a group of sub-passes")
	@Test
	void create() {
		// Check API
		final ArgumentCaptor<VkRenderPassCreateInfo> captor = ArgumentCaptor.forClass(VkRenderPassCreateInfo.class);
		verify(lib).vkCreateRenderPass(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check create descriptor
		final VkRenderPassCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(0, info.flags);

		// Check attachments
		assertEquals(1, info.attachmentCount);
		assertNotNull(info.pAttachments);
		assertEquals(FORMAT, info.pAttachments.format);
		assertEquals(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL, info.pAttachments.finalLayout);

		// Check sub-passes
		assertEquals(1, info.subpassCount);
		assertNotNull(info.pSubpasses);
		assertEquals(1, info.pSubpasses.colorAttachmentCount);
		assertEquals(null, info.pSubpasses.pDepthStencilAttachment);

		// Check dependencies
		assertEquals(1, info.dependencyCount);
		assertNotNull(info.pDependencies);
		assertEquals(0, info.pDependencies.dependencyFlags);

		// Check source dependency
		assertEquals(-1, info.pDependencies.srcSubpass);
		assertEquals(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT.value(), info.pDependencies.srcStageMask);
		assertEquals(0, info.pDependencies.srcAccessMask);

		// Check destination dependency
		assertEquals(0, info.pDependencies.dstSubpass);
		assertEquals(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT.value(), info.pDependencies.dstStageMask);
		assertEquals(VkAccess.COLOR_ATTACHMENT_WRITE.value(), info.pDependencies.dstAccessMask);
	}

	@DisplayName("Create a render-pass with a self-referential sub-pass")
	@Test
	void createSelfDependency() {
		subpass = new Subpass.Builder()
				.colour(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(Subpass.SELF)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.build()
				.build();

		pass = RenderPass.create(dev, List.of(subpass));
	}

	@DisplayName("Cannot create a render-pass without any sub-passes")
	@Test
	void createEmpty() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of()));
	}

	@DisplayName("Cannot explicitly use the EXTERNAL sub-pass")
	@Test
	void createExternal() {
		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(Subpass.EXTERNAL)));
	}

	@DisplayName("Cannot have a dependency on a sub-pass that is not a member of the render-pass")
	@Test
	void createInvalidDependency() {
		final Subpass invalid = new Subpass.Builder()
				.depth(new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
				.dependency()
					.subpass(subpass)
					.source()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.destination()
						.stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
						.build()
					.build()
				.build();

		assertThrows(IllegalArgumentException.class, () -> RenderPass.create(dev, List.of(invalid)));
	}

	@Test
	void destroy() {
		pass.destroy();
		verify(lib).vkDestroyRenderPass(dev, pass, null);
	}

	@Nested
	class GroupTests {
		private Group group;
		private Subpass other;
		private Reference ref;
		private Subpass.Properties props;

		@BeforeEach
		void before() {
			ref = new Reference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

			other = new Subpass.Builder()
					.colour(ref)
					.dependency()
						.subpass(subpass)
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

			props = new Subpass.Properties(Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_READ));

			group = new Group(List.of(subpass, other));
		}

		@DisplayName("The total set of attachments can be retrieved from the group")
		@Test
		void attachments() {
			assertEquals(List.of(attachment), group.attachments());
		}

		@DisplayName("The set of sub-pass dependencies can be retrieved from the group")
		@Test
		void dependencies() {
			final var dependency = Pair.of(other, new Dependency(subpass, props, props));
			assertEquals(List.of(dependency), group.dependencies());
		}

		@DisplayName("A descriptor for a sub-pass can be generated")
		@Test
		void subpass() {
			final var descriptor = new VkSubpassDescription();
			group.populate(subpass, descriptor);
			assertEquals(0, descriptor.flags);
			assertEquals(VkPipelineBindPoint.GRAPHICS, descriptor.pipelineBindPoint);
			assertEquals(1, descriptor.colorAttachmentCount);
			assertNotNull(descriptor.pColorAttachments);
			assertNull(descriptor.pDepthStencilAttachment);
		}

		@DisplayName("A descriptor for a sub-pass dependency can be generated")
		@Test
		void dependency() {
			final var dependency = Pair.of(other, new Dependency(subpass, props, props));
			final var descriptor = new VkSubpassDependency();
			group.populate(dependency, descriptor);
			assertEquals(0, descriptor.srcSubpass);
			assertEquals(1, descriptor.dstSubpass);
		}

		@DisplayName("A sub-pass in the group can have a dependency on the implicit external sub-pass")
		@Test
		void external() {
			final var dependency = Pair.of(subpass, new Dependency(Subpass.EXTERNAL, props, props));
			final var descriptor = new VkSubpassDependency();
			group.populate(dependency, descriptor);
			assertEquals(-1, descriptor.srcSubpass);
			assertEquals(0, descriptor.dstSubpass);
		}

		@DisplayName("A sub-pass in the group can have a dependency on itself")
		@Test
		void self() {
			final var dependency = Pair.of(subpass, new Dependency(Subpass.SELF, props, props));
			final var descriptor = new VkSubpassDependency();
			group.populate(dependency, descriptor);
			assertEquals(0, descriptor.srcSubpass);
			assertEquals(0, descriptor.dstSubpass);
		}

		@DisplayName("A sub-pass cannot have a dependency on a sub-pass that is not part of the group")
		@Test
		void missing() {
			final Subpass missing = new Subpass(List.of(), ref, List.of());
			final var dependency = Pair.of(subpass, new Dependency(missing, props, props));
			assertThrows(IllegalArgumentException.class, () -> group.populate(dependency, new VkSubpassDependency()));
		}
	}
}
