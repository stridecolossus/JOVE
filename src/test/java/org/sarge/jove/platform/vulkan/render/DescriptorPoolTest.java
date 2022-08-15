package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout.Binding;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorPoolTest extends AbstractVulkanTest {
	private Binding binding;
	private DescriptorLayout layout;
	private DescriptorPool pool;

	@BeforeEach
	void before() {
		binding = new Binding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Pointer(1), dev, List.of(binding));
		pool = new DescriptorPool(new Pointer(2), dev, 1);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(2)), pool.handle());
		assertEquals(dev, pool.device());
		assertEquals(1, pool.maximum());
	}

	@Test
	void destroy() {
		pool.destroy();
		verify(lib).vkDestroyDescriptorPool(dev, pool, null);
	}

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

	@DisplayName("Multiple descriptor sets with a common layout can be allocated from the pool")
	@Test
	void multiple() {
		final Collection<DescriptorSet> sets = pool.allocate(2, layout);
		assertNotNull(sets);
		assertEquals(2, sets.size());
		assertEquals(layout, sets.iterator().next().layout());
	}

	@Test
	void free() {
		final Collection<DescriptorSet> sets = pool.allocate(layout);
		pool.free(sets);
		verify(lib).vkFreeDescriptorSets(dev, pool, 1, NativeObject.array(sets));
		assertEquals(1, pool.maximum());
	}

	@Test
	void reset() {
		pool.reset();
		verify(lib).vkResetDescriptorPool(dev, pool, 0);
	}

	@Nested
	class BuilderTests {
		private DescriptorPool.Builder builder;

		@BeforeEach
		void before() {
			builder = new DescriptorPool.Builder();
		}

		@Test
		void build() {
			// Build pool
			pool = builder
					.add(VkDescriptorType.SAMPLER, 3)
					.flag(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET)
					.build(dev);

			// Check pool
			assertNotNull(pool);
			assertEquals(3, pool.maximum());

			// Check API
			final var expected = new VkDescriptorPoolCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkDescriptorPoolCreateInfo) obj;
					assertEquals(3, info.maxSets);
					assertEquals(VkDescriptorPoolCreateFlag.FREE_DESCRIPTOR_SET.value(), info.flags);
					assertEquals(1, info.poolSizeCount);
					assertNotNull(info.pPoolSizes);
					return true;
				}
			};
			verify(lib).vkCreateDescriptorPool(dev, expected, null, factory.pointer());
		}

		@Test
		void max() {
			pool = builder
					.max(1)
					.add(VkDescriptorType.SAMPLER, 1)
					.add(VkDescriptorType.UNIFORM_BUFFER, 1)
					.build(dev);

			assertEquals(1, pool.maximum());
		}

		@Test
		void buildEmpty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}

		@Test
		void buildInvalidTotalPoolSize() {
			builder.max(1).add(VkDescriptorType.SAMPLER, 2);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}
}
