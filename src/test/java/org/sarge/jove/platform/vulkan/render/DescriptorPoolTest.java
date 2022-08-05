package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class DescriptorPoolTest extends AbstractVulkanTest {
	private ResourceBinding binding;
	private DescriptorLayout layout;
	private DescriptorPool pool;

	@BeforeEach
	void before() {
		binding = new ResourceBinding(1, VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1, Set.of(VkShaderStage.FRAGMENT));
		layout = new DescriptorLayout(new Pointer(1), dev, List.of(binding));
		pool = new DescriptorPool(new Pointer(2), dev, 1);
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
		pool.destroy();
		verify(lib).vkDestroyDescriptorPool(dev, pool, null);
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
		verify(lib).vkAllocateDescriptorSets(eq(dev), captor.capture(), isA(Pointer[].class));

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
		final List<DescriptorSet> sets = pool.allocate(List.of(layout));
		pool.free(sets);
		verify(lib).vkFreeDescriptorSets(dev, pool, 1, NativeObject.array(sets));
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
		assertThrows(IllegalArgumentException.class, () -> pool.free(List.of(mock(DescriptorSet.class))));
	}

	@Test
	void freeAll() {
		pool.allocate(List.of(layout));
		pool.free();
		verify(lib).vkResetDescriptorPool(dev, pool, 0);
		assertEquals(1, pool.maximum());
		assertEquals(1, pool.available());
		assertEquals(0, pool.sets().count());
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
			assertEquals(3, pool.available());
			assertEquals(0, pool.sets().count());

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
			assertEquals(1, pool.available());
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
