package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

public class BindingTest {
	private Binding binding;

	@BeforeEach
	void before() {
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of(VkShaderStage.FRAGMENT));
	}

	@Test
	void constructor() {
		assertEquals(1, binding.index());
		assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, binding.type());
		assertEquals(2, binding.count());
		assertEquals(Set.of(VkShaderStage.FRAGMENT), binding.stages());
	}

	@Test
	void empty() {
		assertThrows(IllegalArgumentException.class, () -> new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of()));
	}

	@Test
	void build() {
		final Binding result = new Binding.Builder()
				.binding(1)
				.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
				.count(2)
				.stage(VkShaderStage.FRAGMENT)
				.build();

		assertEquals(binding, result);
	}
}
