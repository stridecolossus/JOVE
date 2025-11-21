package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Binding;
import org.sarge.jove.util.EnumMask;

class PipelineLayoutTest {
	private static class MockPipelineLayoutLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pPipelineLayout) {
			assertNotNull(device);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(pCreateInfo.setLayoutCount, pCreateInfo.pSetLayouts.length);
			assertEquals(1, pCreateInfo.pushConstantRangeCount);
			assertEquals(1, pCreateInfo.pPushConstantRanges.length);
			assertEquals(new EnumMask<>(VkShaderStage.FRAGMENT), pCreateInfo.pPushConstantRanges[0].stageFlags);
			assertEquals(0, pCreateInfo.pPushConstantRanges[0].offset);
			pPipelineLayout.set(MemorySegment.ofAddress(2));
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
			pSetLayout.set(MemorySegment.ofAddress(3));
			return VkResult.SUCCESS;
		}
	}

	private PipelineLayout layout;
	private DescriptorSet.Layout set;
	private MockPipelineLayoutLibrary library;
	private LogicalDevice device;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		// Init library
		library = new MockPipelineLayoutLibrary();
		allocator = Arena.ofAuto();

		// Init logical device
		device = new MockLogicalDevice(library) {
			@Override
			public DeviceLimits limits() {
				final var limits = new VkPhysicalDeviceLimits();
				limits.maxPushConstantsSize = 256;
				return new DeviceLimits(limits);
			}
		};

		// Create a descriptor set layout
		final var binding = new Binding(0, VkDescriptorType.SAMPLER, 1, Set.of(VkShaderStage.VERTEX));
		set = DescriptorSet.Layout.create(device, List.of(binding), Set.of());

		// Create a push constant
		final var range = new Range(0, 256, Set.of(VkShaderStage.FRAGMENT));
		final var constant = new PushConstant(List.of(range), allocator);

		// Create layout
		layout = new PipelineLayout.Builder()
				.add(set)
				.constant(constant)
				.build(device);
	}

	@Test
	void destroy() {
		layout.destroy();
	}
}
