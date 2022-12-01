package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.*;
import org.sarge.jove.platform.vulkan.render.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;

public class DescriptorSetTest extends AbstractVulkanTest {
	private Binding binding;
	private DescriptorSet.Layout layout;
	private DescriptorSet descriptor;
	private DescriptorResource res;

	@BeforeEach
	void before() {
		// Create layout with a sampler binding
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorSet.Layout(new Handle(1), dev, List.of(binding));

		// Create sampler resource
		res = mock(DescriptorResource.class);
		when(res.type()).thenReturn(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

		// Create descriptor set
		descriptor = new DescriptorSet(new Handle(2), layout);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(2), descriptor.handle());
		assertEquals(layout, descriptor.layout());
	}

	@DisplayName("A descriptor set can be bound to the graphics pipeline")
	@Test
	void bind() {
		final PipelineLayout pipelineLayout = mock(PipelineLayout.class);
		final Command bind = descriptor.bind(pipelineLayout);
		final var buffer = mock(Command.Buffer.class);
		bind.record(lib, buffer);
		verify(lib).vkCmdBindDescriptorSets(buffer, VkPipelineBindPoint.GRAPHICS, pipelineLayout, 0, 1, NativeObject.array(List.of(descriptor)), 0, null);
	}

	@DisplayName("A descriptor set resource...")
	@Nested
	class ResourceTests {
    	@DisplayName("can be populated")
    	@Test
    	void set() {
    		descriptor.set(binding, res);
    	}

    	@DisplayName("can be overwritten with a new resource")
    	@Test
    	void reset() {
    		descriptor.set(binding, res);
    		descriptor.set(binding, res);
    	}

    	@DisplayName("cannot populate a binding that does not belong to the layout")
    	@Test
    	void invalid() {
    		final Binding other = new Binding(2, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
    		assertThrows(IllegalArgumentException.class, () -> descriptor.set(other, res));
    	}

    	@DisplayName("cannot be set to a resource with a different descriptor type")
    	@Test
    	void setInvalidResource() {
    		when(res.type()).thenReturn(VkDescriptorType.STORAGE_BUFFER);
    		assertThrows(IllegalArgumentException.class, () -> descriptor.set(binding, res));
    	}
	}

	@DisplayName("A descriptor set update...")
	@Nested
	class UpdateTests {
		@DisplayName("is used to update the resources for a group of descriptor sets")
    	@Test
    	void update() {
    		// Apply update
    		descriptor.set(binding, res);
    		assertEquals(1, DescriptorSet.update(dev, Set.of(descriptor)));

    		// Init expected write descriptor
    		final var write = new VkWriteDescriptorSet() {
    			@Override
    			public boolean equals(Object obj) {
    				final var expected = (VkWriteDescriptorSet) obj;
    				assertEquals(1, expected.dstBinding);
    				assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, expected.descriptorType);
    				assertEquals(descriptor.handle(), expected.dstSet);
    				assertEquals(1, expected.descriptorCount);
    				assertEquals(0, expected.dstArrayElement);
    				return true;
    			}
    		};

    		// Check API
    		verify(lib).vkUpdateDescriptorSets(dev, 1, new VkWriteDescriptorSet[]{write}, 0, null);
    		verify(res).populate(write);
    	}

		@DisplayName("is ignored if none of the resources have been modified")
    	@Test
    	void none() {
    		descriptor.set(binding, res);
    		DescriptorSet.update(dev, Set.of(descriptor));
    		assertEquals(0, DescriptorSet.update(dev, Set.of(descriptor)));
    	}

		@DisplayName("cannot be applied if any resource has not been configured")
    	@Test
    	void empty() {
    		assertThrows(IllegalStateException.class, () -> DescriptorSet.update(dev, Set.of(descriptor)));
    	}
	}

	@DisplayName("A descriptor set binding...")
	@Nested
	class BindingTests {
		@Test
		void constructor() {
			assertEquals(1, binding.index());
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, binding.type());
			assertEquals(2, binding.count());
			assertEquals(Set.of(VkShaderStage.FRAGMENT), binding.stages());
		}

		@DisplayName("must specify at least one pipeline stage")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2, Set.of()));
		}

		@DisplayName("can be created via a builder")
		@Test
		void build() {
			final var builder = new Binding.Builder()
					.binding(1)
					.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
					.count(2)
					.stage(VkShaderStage.FRAGMENT);

			assertEquals(binding, builder.build());
		}
	}

	@DisplayName("A descriptor set layout...")
	@Nested
	class LayoutTests {
		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(1)), layout.handle());
			assertEquals(List.of(binding), layout.bindings());
		}

		@DisplayName("must contain at least one binding")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> new Layout(new Handle(1), dev, List.of()));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			layout.destroy();
			verify(lib).vkDestroyDescriptorSetLayout(dev, layout, null);
		}

		@DisplayName("can be created for a given list of bindings")
		@Test
		void create() {
			// Create layout
			layout = Layout.create(dev, List.of(binding));
			assertNotNull(layout);

			// Init expected create descriptor
			final var expected = new VkDescriptorSetLayoutCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var actual = (VkDescriptorSetLayoutCreateInfo) obj;
					assertEquals(1, actual.bindingCount);
					assertNotNull(actual.pBindings);
					return true;
				}
			};

			// Check API
			verify(lib).vkCreateDescriptorSetLayout(dev, expected, null, factory.pointer());
		}

		@DisplayName("cannot contain duplicate bindings")
		@Test
		void duplicate() {
			assertThrows(IllegalArgumentException.class, () -> Layout.create(dev, List.of(binding, binding)));
		}
	}

	@DisplayName("A descriptor set pool...")
	@Nested
	class PoolTests {
		private Pool pool;

		@BeforeEach
		void before() {
			pool = new Pool(new Handle(2), dev, 1);
		}

		@Test
		void constructor() {
			assertEquals(new Handle(new Pointer(2)), pool.handle());
			assertEquals(dev, pool.device());
			assertEquals(1, pool.maximum());
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			pool.destroy();
			verify(lib).vkDestroyDescriptorPool(dev, pool, null);
		}

		@DisplayName("can allocate a descriptor set with a given layout")
		@Test
		void allocate() {
			// Allocate some sets
			final Collection<DescriptorSet> sets = pool.allocate(layout);
			assertNotNull(sets);
			assertEquals(1, sets.size());

			// Check allocated sets
			assertNotNull(sets.iterator().next());
			assertEquals(1, pool.maximum());

			// Check API
			final ArgumentCaptor<VkDescriptorSetAllocateInfo> captor = ArgumentCaptor.forClass(VkDescriptorSetAllocateInfo.class);
			verify(lib).vkAllocateDescriptorSets(eq(dev), captor.capture(), isA(Pointer[].class));

			// Check descriptor
			final VkDescriptorSetAllocateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(pool.handle(), info.descriptorPool);
			assertEquals(1, info.descriptorSetCount);
			assertNotNull(info.pSetLayouts);
		}

		@DisplayName("can allocate multiple descriptor sets with a given layout")
		@Test
		void multiple() {
			final Collection<DescriptorSet> sets = pool.allocate(2, layout);
			assertNotNull(sets);
			assertEquals(2, sets.size());
			assertEquals(layout, sets.iterator().next().layout());
		}

		@DisplayName("can release all its descriptor sets back to the pool")
		@Test
		void free() {
			final Collection<DescriptorSet> sets = pool.allocate(layout);
			pool.free(sets);
			verify(lib).vkFreeDescriptorSets(dev, pool, 1, NativeObject.array(sets));
			assertEquals(1, pool.maximum());
		}

		@DisplayName("can be reset")
		@Test
		void reset() {
			pool.reset();
			verify(lib).vkResetDescriptorPool(dev, pool, 0);
		}

		@Nested
		class BuilderTests {
			private Pool.Builder builder;

			@BeforeEach
			void before() {
				builder = new Pool.Builder();
			}

			@DisplayName("can be created via a builder")
			@Test
			void build() {
				// Build pool
				pool = builder
						.add(VkDescriptorType.SAMPLER, 3)
						.flag(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET)
						.build(dev);

				// Check API
				final var expected = new VkDescriptorPoolCreateInfo() {
					@Override
					public boolean equals(Object obj) {
						final var info = (VkDescriptorPoolCreateInfo) obj;
						assertEquals(3, info.maxSets);
						assertEquals(BitMask.reduce(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET), info.flags);
						assertEquals(1, info.poolSizeCount);
						assertNotNull(info.pPoolSizes);
						return true;
					}
				};
				verify(lib).vkCreateDescriptorPool(dev, expected, null, factory.pointer());
			}

			@DisplayName("must contain at least one descriptor type")
			@Test
			void empty() {
				assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
			}

			@DisplayName("has an implicit maximum number of descriptor sets that can be allocated")
			@Test
			void implicit() {
				pool = builder.add(VkDescriptorType.SAMPLER, 1).build(dev);
				assertEquals(1, pool.maximum());
			}

			@DisplayName("can specify a maximum allocation size larger than the configured pool entries")
			@Test
			void max() {
				pool = builder
						.add(VkDescriptorType.SAMPLER, 1)
						.max(2)
						.build(dev);

				assertEquals(2, pool.maximum());
			}

			@DisplayName("cannot configure a maximum allocation size smaller than any of the pool entries")
			@Test
			void size() {
				builder.max(1).add(VkDescriptorType.SAMPLER, 2);
				assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
			}
		}
	}
}
