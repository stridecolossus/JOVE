package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.*;
import org.sarge.jove.util.*;

public class DescriptorSetTest {
	@SuppressWarnings("unused")
	private static class MockDescriptorSetLibrary extends MockLibrary {
		public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
			assertEquals(VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO, pCreateInfo.sType);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.bindingCount);
			assertEquals(1, pCreateInfo.pBindings.length);

			assertEquals(1, pCreateInfo.pBindings[0].binding);
			assertEquals(VkDescriptorType.SAMPLER, pCreateInfo.pBindings[0].descriptorType);
			assertEquals(2, pCreateInfo.pBindings[0].descriptorCount);
			assertEquals(new EnumMask<>(VkShaderStageFlags.FRAGMENT), pCreateInfo.pBindings[0].stageFlags);

			init(pSetLayout);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkCreateDescriptorPool(LogicalDevice device, VkDescriptorPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pDescriptorPool) {
			assertEquals(new EnumMask<>(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.poolSizeCount);
			assertEquals(1, pCreateInfo.pPoolSizes.length);
			assertEquals(2, pCreateInfo.pPoolSizes[0].descriptorCount);
			assertEquals(VkDescriptorType.SAMPLER, pCreateInfo.pPoolSizes[0].type);
			init(pDescriptorPool);
			return VkResult.VK_SUCCESS;
		}

		public VkResult vkAllocateDescriptorSets(LogicalDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, Handle[] pDescriptorSets) {
			assertEquals(VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO, pAllocateInfo.sType);
			assertEquals(pDescriptorSets.length, pAllocateInfo.descriptorSetCount);
			assertEquals(1, pAllocateInfo.pSetLayouts.length);
			init(pDescriptorSets, new Handle(1));
			return VkResult.VK_SUCCESS;
		}

		public void vkUpdateDescriptorSets(LogicalDevice device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies) {
			for(var write : pDescriptorWrites) {
				assertEquals(VkStructureType.WRITE_DESCRIPTOR_SET, write.sType);
				assertEquals(1, write.dstBinding);
				assertEquals(VkDescriptorType.SAMPLER, write.descriptorType);
				assertEquals(1, write.descriptorCount);
				assertEquals(0, write.dstArrayElement);
			}
			assertEquals(descriptorWriteCount, pDescriptorWrites.length);
			assertEquals(0, descriptorCopyCount);
			assertEquals(null, pDescriptorCopies);
		}

		public void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, DescriptorSet[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets) {
			assertEquals(VkPipelineBindPoint.GRAPHICS, pipelineBindPoint);
			assertEquals(0, firstSet);
			assertEquals(descriptorSetCount, pDescriptorSets.length);
			assertEquals(0, dynamicOffsetCount);
			assertEquals(null, pDynamicOffsets);
		}
	}

	private Binding binding;
	private DescriptorSet set;
	private Resource resource;
	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		// Init device
		mockery = new Mockery(new MockDescriptorSetLibrary(), DescriptorSet.Library.class);
		device = new MockLogicalDevice(mockery.proxy());

		// Create layout with a sampler binding
		binding = new Binding(1, VkDescriptorType.SAMPLER, 2, Set.of(VkShaderStageFlags.FRAGMENT));

		// Create a resource
		resource = new Resource() {
			@Override
			public VkDescriptorType type() {
				return VkDescriptorType.SAMPLER;
			}

			@Override
			public NativeStructure descriptor() {
				return new VkDescriptorImageInfo();
			}
		};

		// Create descriptor set
		set = new DescriptorSet(new Handle(2), List.of(binding));
	}

	@Test
	void empty() {
		assertEquals(null, set.get(binding));
	}

	// TODO - better name: apply, resource?
	@Test
	void set() {
		set.set(binding, resource);
		assertEquals(resource, set.get(binding));
	}

	@Test
	void setMultiple() {
		DescriptorSet.set(List.of(set), binding, resource);
		assertEquals(resource, set.get(binding));
	}

	@Test
	void setArray() {
		DescriptorSet.set(List.of(set), binding, List.of(resource));
		assertEquals(resource, set.get(binding));
	}

	@Test
	void replace() {
		set.set(binding, resource);
		set.set(binding, resource);
	}

	@Test
	void invalidBinding() {
		final Binding other = new Binding.Builder()
				.stage(VkShaderStageFlags.VERTEX)
				.type(VkDescriptorType.UNIFORM_BUFFER)
				.build();

		assertThrows(IllegalArgumentException.class, () -> set.set(other, resource));
	}

	@Test
	void invalidResource() {
		final var invalid = new Resource() {
			@Override
			public VkDescriptorType type() {
				return VkDescriptorType.UNIFORM_BUFFER;
			}

			@Override
			public NativeStructure descriptor() {
				return null;
			}
		};

		assertThrows(IllegalArgumentException.class, () -> set.set(binding, invalid));
	}

	@Test
	void update() {
		set.set(binding, resource);
		assertEquals(1, DescriptorSet.update(device, List.of(set)));
		assertEquals(1, mockery.mock("vkUpdateDescriptorSets").count());
	}

	@Test
	void updateNotPopulated() {
		assertThrows(IllegalStateException.class, () -> DescriptorSet.update(device, List.of(set)));
	}

	@Test
	void ignored() {
		set.set(binding, resource);
		DescriptorSet.update(device, List.of(set));
		assertEquals(0, DescriptorSet.update(device, List.of(set)));
	}

	@Test
	void bind() {
		final var pipeline = new MockPipelineLayout() {
			@Override
			public LogicalDevice device() {
				return device;
			}
		};
		final Command bind = set.bind(pipeline);
		bind.execute(null);
		assertEquals(1, mockery.mock("vkCmdBindDescriptorSets").count());
	}

	@Test
	void binding() {
		final var builder = new Binding.Builder().type(VkDescriptorType.SAMPLER);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void layout() {
		final Layout layout = Layout.create(device, List.of(binding), Set.of());
		layout.destroy();
		assertEquals(true, layout.isDestroyed());
	}

	@Nested
	class PoolTest {
		private Pool pool;
		private Layout layout;

		@BeforeEach
		void before() {
			layout = Layout.create(device, List.of(binding), Set.of());

			pool = new Pool.Builder()
					.add(VkDescriptorType.SAMPLER, 2)
					.flag(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET)
					.build(device);
		}

		@Test
		void maximum() {
			assertEquals(2, pool.maximum());
		}

		@Test
		void invalidMaximum() {
			final var builder = new Pool.Builder()
					.add(VkDescriptorType.SAMPLER, 2)
					.max(3);

			assertThrows(IllegalArgumentException.class, () -> builder.build(device));
		}

		@Test
		void none() {
			assertThrows(IllegalArgumentException.class, () -> new Pool.Builder().build(device));
		}

		@Test
		void allocate() {
			final List<DescriptorSet> allocated = pool.allocate(List.of(layout));
			assertEquals(1, allocated.size());
			allocated.getFirst().set(binding, resource);
		}

		@Test
		void free() {
			final var allocated = pool.allocate(List.of(layout));
			pool.free(allocated);
		}

		@Test
		void reset() {
			pool.reset();
		}

		@Test
		void destroy() {
			pool.destroy();
			assertEquals(true, pool.isDestroyed());
		}
	}
}
