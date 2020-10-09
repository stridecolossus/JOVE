package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Sampler;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Pool;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorSetTest extends AbstractVulkanTest {
	private Layout layout;
	private DescriptorSet set;

	@BeforeEach
	void before() {
		layout = new Layout(new Pointer(1), dev, List.of(new VkDescriptorSetLayoutBinding()));
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

		@Test
		void sampler() {

			// TODO
			// TODO
			// TODO

			layout = new Layout.Builder(dev)
					.binding()
						.binding(42)
						.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
						.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
						.build()
					.build();

			set = new DescriptorSet(new Handle(new Pointer(2)), layout);

			final Sampler sampler = mock(Sampler.class);
			when(sampler.handle()).thenReturn(new Handle(new Pointer(3)));

			final View view = mock(View.class);
			when(view.handle()).thenReturn(new Handle(new Pointer(4)));

			set.sampler(42, sampler, view);

			// Check API
			final ArgumentCaptor<VkWriteDescriptorSet[]> captor = ArgumentCaptor.forClass(VkWriteDescriptorSet[].class);
			verify(lib).vkUpdateDescriptorSets(eq(dev.handle()), eq(1), captor.capture(), eq(0), isNull());
			assertNotNull(captor.getValue());
			assertEquals(1, captor.getValue().length);

			// Check descriptor
			final VkWriteDescriptorSet info = captor.getValue()[0];
			assertNotNull(info);
			assertEquals(42, info.dstBinding);
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER, info.descriptorType);
			assertEquals(1, info.descriptorCount);
			assertEquals(0, info.dstArrayElement);

			// Check sampler descriptor
			// TODO
			assertNotNull(info.pImageInfo);
		}

		@Test
		void uniform() {
			// TODO
		}
	}

	@Nested
	class LayoutTests {
		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(1)), layout.handle());
		}

		@Test
		void constructorEmptyBindings() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(new Pointer(1), dev, List.of()));
		}

		@Test
		void destroy() {
			final Handle handle = layout.handle();
			layout.destroy();
			verify(lib).vkDestroyDescriptorSetLayout(dev.handle(), handle, null);
		}

		@Nested
		class LayoutBuilderTests {
			private Layout.Builder builder;

			@BeforeEach
			void before() {
				builder = new Layout.Builder(dev);
			}

			@Test
			void build() {
				// Build a layout with two bindings
				layout = builder
						.binding()
							.binding(2)
							.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
							.count(3)
							.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
							.build()
						.binding()
							.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
							.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
							.build()
						.build();

				// Check layout
				assertNotNull(layout);
				// TODO
				// - check bindings
				// - check second is auto incremented

				// Check API
				final ArgumentCaptor<VkDescriptorSetLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetLayoutCreateInfo.class);
				verify(lib).vkCreateDescriptorSetLayout(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

				// Check create descriptor
				final VkDescriptorSetLayoutCreateInfo info = captor.getValue();
				assertNotNull(info);
				assertEquals(2, info.bindingCount);
				assertNotNull(info.pBindings);
			}

			@Test
			void buildDuplicateBinding() {
				builder.binding()
						.binding(42)
						.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
						.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
						.build();

				assertThrows(IllegalArgumentException.class, () -> builder.binding().binding(42));
			}

			@Test
			void buildRequiresDescriptorType() {
				assertThrows(IllegalArgumentException.class, () -> builder.binding().stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT).build());
			}

			@Test
			void buildRequiresPipelineStage() {
				assertThrows(IllegalArgumentException.class, () -> builder.binding().type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER).build());
			}
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

//			System.out.println(sets);
//			System.out.println(Arrays.toString(pool.sets().toArray()));

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
			verify(lib).vkFreeDescriptorSets(dev.handle(), pool.handle(), 1, Handle.toArray(sets, DescriptorSet::handle));
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
			pool.allocate(List.of(layout, layout));
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
}
