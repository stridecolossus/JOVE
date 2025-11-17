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
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Binding;
import org.sarge.jove.util.EnumMask;

class PipelineLayoutTest {
	private static class MockPipelineLayoutLibrary extends MockVulkanLibrary {
		private boolean updated;

		@Override
		public VkResult vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pPipelineLayout) {
			assertNotNull(device);
			assertEquals(0, pCreateInfo.flags);
			assertEquals(pCreateInfo.setLayoutCount, pCreateInfo.pSetLayouts.length);
			assertEquals(1, pCreateInfo.pushConstantRangeCount);
			assertEquals(1, pCreateInfo.pPushConstantRanges.length);
			assertEquals(new EnumMask<>(VkShaderStage.FRAGMENT), pCreateInfo.pPushConstantRanges[0].stageFlags);
			assertEquals(0, pCreateInfo.pPushConstantRanges[0].offset);
			pPipelineLayout.set(new Handle(2));
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
			pSetLayout.set(new Handle(3));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStage> stageFlags, int offset, int size, Handle pValues) {
			assertNotNull(layout);
			assertEquals(new EnumMask<>(VkShaderStage.FRAGMENT), stageFlags);
			assertEquals(0, offset);
			assertEquals(256, size);
			assertEquals(256, pValues.address().byteSize());
			updated = true;
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

	@Nested
	class PushConstantTest {
		@Test
		void constant() {
			assertEquals(256, layout.constant().data().byteSize());
		}

		@Test
		void limit() {
			final var range = new Range(0, 256 + 4, Set.of(VkShaderStage.FRAGMENT));
			final var constant = new PushConstant(List.of(range), allocator);
			final var builder = new PipelineLayout.Builder().constant(constant);
			assertThrows(IllegalArgumentException.class, () -> builder.build(device));
		}

		@Test
		void update() {
			final Range range = layout.constant().ranges().getFirst();
			final Command update = layout.new PushConstantUpdateCommand(range);
			update.execute(null);
			assertEquals(true, library.updated);
		}

		@Test
		void invalid() {
			final var other = new Range(0, 4, Set.of(VkShaderStage.GEOMETRY));
			assertThrows(IllegalArgumentException.class, () -> layout.new PushConstantUpdateCommand(other));
		}

		@Test
		void whole() {
			final Command update = layout.new PushConstantUpdateCommand();
			update.execute(null);
			assertEquals(true, library.updated);
		}
	}
}
