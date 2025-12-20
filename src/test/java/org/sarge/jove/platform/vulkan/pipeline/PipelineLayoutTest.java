package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.Arena;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.util.*;

class PipelineLayoutTest {
	@SuppressWarnings("unused")
	static class MockPipelineLayoutLibrary extends MockLibrary {
		public VkResult vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pPipelineLayout) {
			assertEquals(VkStructureType.PIPELINE_LAYOUT_CREATE_INFO, pCreateInfo.sType);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(pCreateInfo.setLayoutCount, pCreateInfo.pSetLayouts.length);
//			assertEquals(1, pCreateInfo.pushConstantRangeCount);
//			assertEquals(1, pCreateInfo.pPushConstantRanges.length);
//			assertEquals(new EnumMask<>(VkShaderStageFlags.FRAGMENT), pCreateInfo.pPushConstantRanges[0].stageFlags);
//			assertEquals(0, pCreateInfo.pPushConstantRanges[0].offset);
			init(pPipelineLayout);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
			init(pSetLayout);
			return VkResult.VK_SUCCESS;
		}
	}

	private PipelineLayout layout;
	private LogicalDevice device;
	private PushConstant constant;
	private Mockery mockery;

	@BeforeEach
	void before() {
		// Init pipeline layout library
		mockery = new Mockery(new MockPipelineLayoutLibrary(), PipelineLayout.Library.class, DescriptorSet.Library.class);
		device = new MockLogicalDevice(mockery.proxy());

		// Create a push constant
		@SuppressWarnings("resource")
		final var allocator = Arena.ofAuto();
		final var range = new Range(0, 256, Set.of(VkShaderStageFlags.FRAGMENT));
		constant = new PushConstant(List.of(range), allocator);

		// Create a descriptor set layout
		final var binding = new DescriptorSet.Binding(0, VkDescriptorType.SAMPLER, 1, Set.of(VkShaderStageFlags.VERTEX));
		final var descriptors = DescriptorSet.Layout.create(device, List.of(binding), Set.of());

		// Create layout
		layout = new PipelineLayout.Builder()
				.add(descriptors)
				.constant(constant)
				.build(device);
	}

	@Test
	void constructor() {
		assertFalse(layout.isDestroyed());
		assertEquals(Optional.of(constant), layout.constant());
	}

	@Test
	void destroy() {
		layout.destroy();
		assertTrue(layout.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyPipelineLayout").count());
	}
}
