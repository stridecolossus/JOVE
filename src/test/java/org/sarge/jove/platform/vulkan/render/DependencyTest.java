package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Dependency.Properties;
import org.sarge.jove.platform.vulkan.render.Subpass.AttachmentReference;
import org.sarge.jove.util.EnumMask;

class DependencyTest {
	private Subpass one, two;
	private Dependency dependency;

	@BeforeEach
	void before() {
		final var attachment = new AttachmentReference(Attachment.colour(VkFormat.R32G32B32A32_SFLOAT), VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		one = new Subpass(List.of(attachment), null, Set.of());
		two = new Subpass(List.of(attachment), null, Set.of());
		dependency = new Dependency(
				new Properties(one, Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of(VkAccess.SHADER_READ)),
				new Properties(two, Set.of(VkPipelineStage.VERTEX_SHADER), Set.of(VkAccess.SHADER_WRITE)),
				Set.of()
		);
	}

	@Test
	void descriptor() {
		final VkSubpassDependency descriptor = dependency.descriptor(List.of(one, two));
		assertEquals(new EnumMask<>(), descriptor.dependencyFlags);
		assertEquals(0, descriptor.srcSubpass);
		assertEquals(new EnumMask<>(VkPipelineStage.FRAGMENT_SHADER), descriptor.srcStageMask);
		assertEquals(new EnumMask<>(VkAccess.SHADER_READ), descriptor.srcAccessMask);
		assertEquals(1, descriptor.dstSubpass);
		assertEquals(new EnumMask<>(VkPipelineStage.VERTEX_SHADER), descriptor.dstStageMask);
		assertEquals(new EnumMask<>(VkAccess.SHADER_WRITE), descriptor.dstAccessMask);
	}

	@Test
	void unknown() {
		assertThrows(IllegalArgumentException.class, () -> dependency.descriptor(List.of()));
		assertThrows(IllegalArgumentException.class, () -> dependency.descriptor(List.of(one)));
		assertThrows(IllegalArgumentException.class, () -> dependency.descriptor(List.of(two)));
	}

	@Test
	void external() {
		final Dependency external = new Dependency(
				new Properties(one, Set.of(VkPipelineStage.FRAGMENT_SHADER), Set.of()),
				new Properties(Dependency.VK_SUBPASS_EXTERNAL, Set.of(VkPipelineStage.VERTEX_SHADER), Set.of()),
				Set.of()
		);
		final VkSubpassDependency descriptor = external.descriptor(List.of(one));
		assertEquals(-1, descriptor.dstSubpass);
	}
}
