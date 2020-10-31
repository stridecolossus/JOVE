package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateFlag;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Layout.Binding;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Pool;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.Resource;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class DescriptorSetTest extends AbstractVulkanTest {
	private static final int BINDING = 42;

	private Binding binding;
	private Layout layout;
	private DescriptorSet set;

	@BeforeEach
	void before() {
		binding = new Binding(BINDING, VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT));
		layout = new Layout(new Pointer(1), dev, List.of(binding));
		set = new DescriptorSet(new Handle(new Pointer(2)), layout);
	}

	@Nested
	class DescriptorTests {
		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(2)), set.handle());
			assertEquals(layout, set.layout());
		}

		@Test
		void bind() {
			// Create bind command
			final Pipeline.Layout pipelineLayout = mock(Pipeline.Layout.class);
			final Command bind = set.bind(pipelineLayout);
			assertNotNull(bind);

			// Check API
			final Handle handle = new Handle(new Pointer(3));
			bind.execute(lib, handle);
			verify(lib).vkCmdBindDescriptorSets(handle, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout.handle(), 0, 1, new Pointer[]{new Pointer(2)}, 0, null);
		}
	}

	@Nested
	class BindingTests {
		@Test
		void constructor() {
			assertEquals(BINDING, binding.binding());
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, binding.type());
			assertEquals(1, binding.count());
			assertEquals(Set.of(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT), binding.stages());
		}

		@Test
		void constructorEmptyStages() {
			assertThrows(IllegalArgumentException.class, () -> new Binding(BINDING, VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, Set.of()));
		}

		@Test
		void build() {
			final Binding result = new Binding.Builder()
					.binding(BINDING)
					.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
					.build();
			assertEquals(binding, result);
		}
	}

	@Nested
	class LayoutTests {
		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(1)), layout.handle());
			assertEquals(binding, layout.binding(BINDING));
		}

		@Test
		void constructorEmptyBindings() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(new Pointer(1), dev, List.of()));
		}

		@Test
		void constructorDuplicateBindingIndex() {
			assertThrows(IllegalStateException.class, () -> new Layout(new Pointer(1), dev, List.of(binding, binding)));
		}

		@Test
		void destroy() {
			final Handle handle = layout.handle();
			layout.destroy();
			verify(lib).vkDestroyDescriptorSetLayout(dev.handle(), handle, null);
		}

		@Test
		void create() {
			// Create layout
			layout = Layout.create(dev, List.of(binding));
			assertNotNull(layout);

			// Check API
			final ArgumentCaptor<VkDescriptorSetLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetLayoutCreateInfo.class);
			verify(lib).vkCreateDescriptorSetLayout(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

			// Check create descriptor
			final VkDescriptorSetLayoutCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(1, info.bindingCount);
			assertNotNull(info.pBindings);
		}
	}

	@Nested
	class PoolTests {
		private Pool pool;

		@BeforeEach
		void before() {
			pool = new Pool(new Pointer(2), dev, 3);
		}

		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(2)), pool.handle());
			assertEquals(dev, pool.device());
			assertEquals(3, pool.maximum());
			assertEquals(3, pool.available());
			assertNotNull(pool.sets());
			assertEquals(0, pool.sets().count());
		}

		@Test
		void destroy() {
			final Handle handle = pool.handle();
			pool.destroy();
			verify(lib).vkDestroyDescriptorPool(dev.handle(), handle, null);
			assertEquals(0, pool.sets().count());
		}

		@Test
		void allocate() {
			//factory.pointers = new Pointer[]{new Pointer(3), new Pointer(4)};
			//final ReferenceFactory ref = mock(ReferenceFactory.class);
			//when(lib.factory()).thenReturn(ref);

			//final Pointer[] ptrs = {new Pointer(3), new Pointer(4)};
			//when(ref.pointers(2)).thenReturn(ptrs);
			final var array = factory.array(2);

			// Allocate some sets
			final var sets = pool.allocate(List.of(layout, layout));
			assertNotNull(sets);
			assertEquals(2, sets.size());

			// Check allocated sets
			assertNotNull(sets.get(0));
			assertNotNull(sets.get(1));
			assertEquals(3, pool.maximum());
			assertEquals(1, pool.available());
			assertEquals(new HashSet<>(sets), pool.sets().collect(toSet()));

			// Check API
			final ArgumentCaptor<VkDescriptorSetAllocateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetAllocateInfo.class);
			verify(lib).vkAllocateDescriptorSets(eq(dev.handle()), captor.capture(), eq(array)); //isA(Pointer[].class));

			// Check descriptor
			final VkDescriptorSetAllocateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pool.handle(), info.descriptorPool);
			assertEquals(2, info.descriptorSetCount);
			assertNotNull(info.pSetLayouts);
		}

		@Test
		void allocateExceedsPoolSize() {
			// Allocate available descriptors
			final var layouts = Collections.nCopies(3, layout);
			factory.array(3);
			pool.allocate(layouts);

			// Attempt to allocate another
			factory.array(1);
			assertThrows(IllegalArgumentException.class, () -> pool.allocate(List.of(layout)));
		}

		@Test
		void free() {
			final var sets = pool.allocate(List.of(layout));
			pool.free(sets);
			verify(lib).vkFreeDescriptorSets(dev.handle(), pool.handle(), 1, Handle.toArray(sets));
			assertEquals(3, pool.maximum());
			assertEquals(3, pool.available());
			assertEquals(0, pool.sets().count());
		}

		@Test
		void freeEmpty() {
			assertThrows(IllegalArgumentException.class, () -> pool.free());
		}

		@Test
		void freeNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> pool.free(List.of(set)));
		}

		@Test
		void freeAll() {
			pool.allocate(List.of(layout));
			pool.free();
			verify(lib).vkResetDescriptorPool(dev.handle(), pool.handle(), 0);
			assertEquals(3, pool.maximum());
			assertEquals(3, pool.available());
			assertEquals(0, pool.sets().count());
		}

		@Nested
		class BuilderTests {
			private Pool.Builder builder;

			@BeforeEach
			void before() {
				builder = new Pool.Builder(dev);
			}

			@Test
			void build() {
				// Build pool
				pool = builder
						.max(3)
						.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER, 3)
						.flag(VkDescriptorPoolCreateFlag.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
						.build();

				// Check pool
				assertNotNull(pool);
				assertEquals(3, pool.maximum());
				assertEquals(3, pool.available());
				assertEquals(0, pool.sets().count());

				// Check API
				final ArgumentCaptor<VkDescriptorPoolCreateInfo> captor = ArgumentCaptor.forClass(VkDescriptorPoolCreateInfo.class);
				verify(lib).vkCreateDescriptorPool(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

				// Check create descriptor
				final VkDescriptorPoolCreateInfo info = captor.getValue();
				assertNotNull(info);
				assertEquals(3, info.maxSets);
				assertEquals(VkDescriptorPoolCreateFlag.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT.value(), info.flags);
				assertEquals(1, info.poolSizeCount);
				assertNotNull(info.pPoolSizes);
			}

			@Test
			void buildEmpty() {
				assertThrows(IllegalArgumentException.class, () -> builder.build());
			}

			@Test
			void buildInvalidTotalPoolSize() {
				builder.max(1).add(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER, 2);
				assertThrows(IllegalArgumentException.class, () -> builder.build());
			}
		}
	}

	@Nested
	class UpdateTests {
		private Resource<Structure> res;
		private Structure info;

		@SuppressWarnings("unchecked")
		@BeforeEach
		void before() {
			// Init update descriptor
			info = mock(Structure.class);
			when(info.toArray(1)).thenReturn(new Structure[]{info});

			// Create resource
			res = mock(Resource.class);
			when(res.type()).thenReturn(binding.type());
			when(res.identity()).thenReturn(() -> info);
		}

		@Test
		void update() {
			final var update = set.update(binding, res);
			assertNotNull(update);
		}

		@Test
		void populate() {
			// Populate write descriptor
			final var update = set.update(binding, res);
			final var write = new VkWriteDescriptorSet();
			update.populate(write);

			// Check descriptor
			assertEquals(binding.binding(), write.dstBinding);
			assertEquals(binding.type(), write.descriptorType);
			assertEquals(set.handle(), write.dstSet);
			assertEquals(0, write.dstArrayElement);

			verify(res).populate(info);
			verify(res).apply(info, write);
		}

		@Test
		void apply() {
			final var update = set.update(binding, res);
			DescriptorSet.update(dev, List.of(update, update));
			verify(lib).vkUpdateDescriptorSets(eq(dev.handle()), eq(2), isA(VkWriteDescriptorSet[].class), eq(0), isNull());
		}
	}
}
