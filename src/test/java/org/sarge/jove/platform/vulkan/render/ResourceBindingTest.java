package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkShaderStage;

class ResourceBindingTest {
	private ResourceBinding binding;

	@BeforeEach
	void before() {
		binding = new ResourceBinding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of(VkShaderStage.FRAGMENT));
	}

	@Test
	void constructor() {
		assertEquals(1, binding.index());
		assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, binding.type());
		assertEquals(2, binding.count());
		assertEquals(Set.of(VkShaderStage.FRAGMENT), binding.stages());
	}

	@SuppressWarnings("static-method")
	@Test
	void constructorEmptyStages() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBinding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of()));
	}

	@Test
	void build() {
		final ResourceBinding result = new ResourceBinding.Builder()
				.binding(1)
				.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
				.count(2)
				.stage(VkShaderStage.FRAGMENT)
				.build();

		assertEquals(binding, result);
	}
}
