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
import org.sarge.jove.util.EnumMask;

public class DescriptorSetTest {
	private static class MockDescriptorSetLibrary extends MockVulkanLibrary {
		private boolean updated;
		private boolean bound;

		@Override
		public VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.bindingCount);
			assertEquals(1, pCreateInfo.pBindings.length);

			assertEquals(1, pCreateInfo.pBindings[0].binding);
			assertEquals(VkDescriptorType.SAMPLER, pCreateInfo.pBindings[0].descriptorType);
			assertEquals(2, pCreateInfo.pBindings[0].descriptorCount);
			assertEquals(new EnumMask<>(VkShaderStage.FRAGMENT), pCreateInfo.pBindings[0].stageFlags);

			pSetLayout.set(new Handle(3));
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkCreateDescriptorPool(LogicalDevice device, VkDescriptorPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pDescriptorPool) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET), pCreateInfo.flags);
			assertEquals(1, pCreateInfo.poolSizeCount);
			assertEquals(1, pCreateInfo.pPoolSizes.length);
			assertEquals(2, pCreateInfo.pPoolSizes[0].descriptorCount);
			assertEquals(VkDescriptorType.SAMPLER, pCreateInfo.pPoolSizes[0].type);
			pDescriptorPool.set(new Handle(4));
			return VkResult.SUCCESS;
		}

		@Override
		public VkResult vkAllocateDescriptorSets(LogicalDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, Handle[] pDescriptorSets) {
			assertNotNull(device);
			assertNotNull(pAllocateInfo.descriptorPool);
			assertEquals(pDescriptorSets.length, pAllocateInfo.descriptorSetCount);
			assertEquals(1, pAllocateInfo.pSetLayouts.length);
			Arrays.fill(pDescriptorSets, new Handle(5));
			return VkResult.SUCCESS;
		}

		@Override
		public void vkUpdateDescriptorSets(LogicalDevice device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies) {
			assertNotNull(device);
			assertEquals(1, descriptorWriteCount);
			assertEquals(1, pDescriptorWrites.length);
			assertEquals(0, descriptorCopyCount);
			assertEquals(null, pDescriptorCopies);

			assertEquals(1, pDescriptorWrites[0].dstBinding);
			assertEquals(VkDescriptorType.SAMPLER, pDescriptorWrites[0].descriptorType);
			assertEquals(1, pDescriptorWrites[0].descriptorCount);
			assertEquals(0, pDescriptorWrites[0].dstArrayElement);
			assertEquals(new Handle(2), pDescriptorWrites[0].dstSet);

			updated = true;
		}

		@Override
		public void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, DescriptorSet[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets) {
			assertEquals(VkPipelineBindPoint.GRAPHICS, pipelineBindPoint);
			assertEquals(0, firstSet);
			assertEquals(1, descriptorSetCount);
			assertEquals(1, pDescriptorSets.length);
			assertEquals(new Handle(2), pDescriptorSets[0].handle());
			assertEquals(0, dynamicOffsetCount);
			assertEquals(null, pDynamicOffsets);
			bound = true;
		}
	}

	private Binding binding;
	private DescriptorSet set;
	private Resource resource;
	private LogicalDevice device;
	private MockDescriptorSetLibrary library;

	@BeforeEach
	void before() {
		// Init device
		library = new MockDescriptorSetLibrary();
		device = new MockLogicalDevice(library);

		// Create layout with a sampler binding
		binding = new Binding(1, VkDescriptorType.SAMPLER, 2, Set.of(VkShaderStage.FRAGMENT));

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
		assertEquals(null, set.resource(binding));
	}

	@Test
	void resource() {
		set.resource(binding, resource);
		assertEquals(resource, set.resource(binding));
	}

	@Test
	void replace() {
		set.resource(binding, resource);
		set.resource(binding, resource);
	}

	@Test
	void invalidBinding() {
		final Binding other = new Binding.Builder()
				.stage(VkShaderStage.VERTEX)
				.type(VkDescriptorType.UNIFORM_BUFFER)
				.build();

		assertThrows(IllegalArgumentException.class, () -> set.resource(other, resource));
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

		assertThrows(IllegalArgumentException.class, () -> set.resource(binding, invalid));
	}

	@Test
	void update() {
		set.resource(binding, resource);
		assertEquals(1, DescriptorSet.update(device, List.of(set)));
		assertEquals(true, library.updated);
	}

	@Test
	void updateNotPopulated() {
		assertThrows(IllegalStateException.class, () -> DescriptorSet.update(device, List.of(set)));
	}

	@Test
	void ignored() {
		set.resource(binding, resource);
		DescriptorSet.update(device, List.of(set));
		assertEquals(0, DescriptorSet.update(device, List.of(set)));
	}

	@Test
	void bind() {
		final Command bind = set.bind(new MockPipelineLayout(device));
		bind.execute(null);
		assertEquals(true, library.bound);
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
					.flag(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET)
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
			allocated.getFirst().resource(binding, resource);
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
