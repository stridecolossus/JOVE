package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateFlag;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Resource;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Binding;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Entry;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Pool;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class DescriptorSetTest extends AbstractVulkanTest {
	private static final int BINDING = 42;

	private Binding binding;
	private Layout layout;
	private DescriptorSet descriptor;
	private Resource res;

	@BeforeEach
	void before() {
		// Create layout with a sampler binding
		binding = new Binding(BINDING, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStageFlag.FRAGMENT));
		layout = new Layout(new Pointer(1), dev, List.of(binding));

		// Create sampler resource
		res = mock(Resource.class);
		when(res.type()).thenReturn(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

		// Create descriptor set
		descriptor = new DescriptorSet(new Handle(new Pointer(2)), layout);
	}

	@Nested
	class DescriptorTests {
		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(2)), descriptor.handle());
			assertEquals(layout, descriptor.layout());
		}

		@Test
		void entry() {
			final Entry entry = descriptor.entry(binding);
			assertNotNull(entry);
			assertEquals(Optional.empty(), entry.resource());
		}

		@Test
		void entryInvalidBinding() {
			final Binding other = new Binding(999, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStageFlag.FRAGMENT));
			assertThrows(IllegalArgumentException.class, () -> descriptor.entry(other));
		}

		@Test
		void set() {
			final Entry entry = descriptor.entry(binding);
			entry.set(res);
			entry.set(res);
			assertEquals(Optional.of(res), entry.resource());
		}

		@Test
		void setInvalidResource() {
			when(res.type()).thenReturn(VkDescriptorType.STORAGE_BUFFER);
			assertThrows(IllegalArgumentException.class, () -> descriptor.entry(binding).set(res));
		}

		@Test
		void bind() {
			// Create bind command
			final PipelineLayout pipelineLayout = mock(PipelineLayout.class);
			final Command bind = descriptor.bind(pipelineLayout);
			assertNotNull(bind);

			// Check API
			final Handle handle = new Handle(new Pointer(3));
			bind.execute(lib, handle);
			verify(lib).vkCmdBindDescriptorSets(handle, VkPipelineBindPoint.GRAPHICS, pipelineLayout.handle(), 0, 1, Handle.toArray(List.of(descriptor)), 0, null);
		}
	}

	@Nested
	class BindingTests {
		@Test
		void constructor() {
			assertEquals(BINDING, binding.index());
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, binding.type());
			assertEquals(1, binding.count());
			assertEquals(Set.of(VkShaderStageFlag.FRAGMENT), binding.stages());
		}

		@Test
		void constructorEmptyStages() {
			assertThrows(IllegalArgumentException.class, () -> new Binding(BINDING, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of()));
		}

		@Test
		void build() {
			final Binding result = new Binding.Builder()
					.binding(BINDING)
					.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
					.stage(VkShaderStageFlag.FRAGMENT)
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
			verify(lib).vkCreateDescriptorSetLayout(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

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
			pool = new Pool(new Pointer(2), dev, 1);
		}

		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(2)), pool.handle());
			assertEquals(dev, pool.device());
			assertEquals(1, pool.maximum());
			assertEquals(1, pool.available());
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
			// Allocate some sets
			final var sets = pool.allocate(List.of(layout));
			assertNotNull(sets);
			assertEquals(1, sets.size());

			// Check allocated sets
			assertNotNull(sets.get(0));
			assertEquals(1, pool.maximum());
			assertEquals(0, pool.available());
			assertEquals(new HashSet<>(sets), pool.sets().collect(toSet()));

			// Check API
			final ArgumentCaptor<VkDescriptorSetAllocateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetAllocateInfo.class);
			verify(lib).vkAllocateDescriptorSets(eq(dev.handle()), captor.capture(), isA(Pointer[].class));

			// Check descriptor
			final VkDescriptorSetAllocateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pool.handle(), info.descriptorPool);
			assertEquals(1, info.descriptorSetCount);
			assertNotNull(info.pSetLayouts);
		}

		@Test
		void allocateExceedsPoolSize() {
			pool.allocate(layout, 1);
			assertThrows(IllegalArgumentException.class, () -> pool.allocate(layout, 1));
		}

		@Test
		void free() {
			final var sets = pool.allocate(List.of(layout));
			pool.free(sets);
			verify(lib).vkFreeDescriptorSets(dev.handle(), pool.handle(), 1, Handle.toArray(sets));
			assertEquals(1, pool.maximum());
			assertEquals(1, pool.available());
			assertEquals(0, pool.sets().count());
		}

		@Test
		void freeEmpty() {
			assertThrows(IllegalArgumentException.class, () -> pool.free());
		}

		@Test
		void freeNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> pool.free(List.of(descriptor)));
		}

		@Test
		void freeAll() {
			pool.allocate(List.of(layout));
			pool.free();
			verify(lib).vkResetDescriptorPool(dev.handle(), pool.handle(), 0);
			assertEquals(1, pool.maximum());
			assertEquals(1, pool.available());
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
						.add(VkDescriptorType.SAMPLER, 3)
						.flag(VkDescriptorPoolCreateFlag.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT)
						.build();

				// Check pool
				assertNotNull(pool);
				assertEquals(3, pool.maximum());
				assertEquals(3, pool.available());
				assertEquals(0, pool.sets().count());

				// Check API
				final ArgumentCaptor<VkDescriptorPoolCreateInfo> captor = ArgumentCaptor.forClass(VkDescriptorPoolCreateInfo.class);
				verify(lib).vkCreateDescriptorPool(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

				// Check create descriptor
				final VkDescriptorPoolCreateInfo info = captor.getValue();
				assertNotNull(info);
				assertEquals(3, info.maxSets);
				assertEquals(VkDescriptorPoolCreateFlag.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT.value(), info.flags);
				assertEquals(1, info.poolSizeCount);
				assertNotNull(info.pPoolSizes);
			}

			@Test
			void max() {
				pool = builder
						.max(1)
						.add(VkDescriptorType.SAMPLER, 1)
						.add(VkDescriptorType.UNIFORM_BUFFER, 1)
						.build();

				assertEquals(1, pool.maximum());
				assertEquals(1, pool.available());
			}

			@Test
			void buildEmpty() {
				assertThrows(IllegalArgumentException.class, () -> builder.build());
			}

			@Test
			void buildInvalidTotalPoolSize() {
				builder.max(1).add(VkDescriptorType.SAMPLER, 2);
				assertThrows(IllegalArgumentException.class, () -> builder.build());
			}
		}
	}

	@Nested
	class UpdateTests {
		@Test
		void update() {
			// Update resource
			descriptor.entry(binding).set(res);
			DescriptorSet.update(dev, Set.of(descriptor));

			// Check API
			final ArgumentCaptor<VkWriteDescriptorSet[]> captor = ArgumentCaptor.forClass(VkWriteDescriptorSet[].class);
			verify(lib).vkUpdateDescriptorSets(eq(dev.handle()), eq(1), captor.capture(), eq(0), isNull());

			// Check write descriptors array
			final VkWriteDescriptorSet[] array = captor.getValue();
			assertNotNull(array);
			assertEquals(1, array.length);

			// Check write descriptor
			final VkWriteDescriptorSet write = array[0];
			assertEquals(BINDING, write.dstBinding);
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, write.descriptorType);
			assertEquals(descriptor.handle(), write.dstSet);
			assertEquals(1, write.descriptorCount);
			assertEquals(0, write.dstArrayElement);
			verify(res).populate(write);
		}

		@Test
		void applyNone() {
			// Update resource
			descriptor.entry(binding).set(res);
			DescriptorSet.update(dev, Set.of(descriptor));
			clearInvocations(lib);

			// Apply again and check nothing to update
			DescriptorSet.update(dev, Set.of(descriptor));
			verifyNoMoreInteractions(lib);
		}

		@Test
		void applyEmpty() {
			assertThrows(IllegalStateException.class, () -> DescriptorSet.update(dev, Set.of()));
		}

		@Test
		void applyResourceNotPopulated() {
			assertThrows(IllegalStateException.class, () -> DescriptorSet.update(dev, Set.of(descriptor)));
		}
	}
}
