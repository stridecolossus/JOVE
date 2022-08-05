package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorLayoutTest extends AbstractVulkanTest {
	private ResourceBinding binding;
	private DescriptorLayout layout;

	@BeforeEach
	void before() {
		binding = new ResourceBinding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Pointer(1), dev, List.of(binding));
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), layout.handle());
		assertEquals(binding, layout.binding(1));
	}

	@Test
	void constructorEmptyBindings() {
		assertThrows(IllegalArgumentException.class, () -> new DescriptorLayout(new Pointer(1), dev, List.of()));
	}

	@Test
	void constructorDuplicateBindingIndex() {
		assertThrows(IllegalStateException.class, () -> new DescriptorLayout(new Pointer(1), dev, List.of(binding, binding)));
	}

	@Test
	void destroy() {
		layout.destroy();
		verify(lib).vkDestroyDescriptorSetLayout(dev, layout, null);
	}

	@Test
	void create() {
		// Create layout
		layout = DescriptorLayout.create(dev, List.of(binding));
		assertNotNull(layout);

		// Init expected create descriptor
		final var expected = new VkDescriptorSetLayoutCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var actual = (VkDescriptorSetLayoutCreateInfo) obj;
				assertEquals(1, actual.bindingCount);
				assertNotNull(actual.pBindings);
				return true;
			}
		};

		// Check API
		verify(lib).vkCreateDescriptorSetLayout(dev, expected, null, factory.pointer());
	}
}
