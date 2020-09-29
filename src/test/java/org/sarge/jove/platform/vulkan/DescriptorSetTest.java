package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.DescriptorSet.Layout;
import org.sarge.jove.platform.vulkan.DescriptorSet.Pool;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;

public class DescriptorSetTest extends AbstractVulkanTest {
	@Nested
	class DescriptorSetTests {
		private DescriptorSet set;
		private Layout layout;
		private VkDescriptorSetLayoutBinding binding;

		@BeforeEach
		public void before() {
			layout = mock(Layout.class);
			binding = mock(VkDescriptorSetLayoutBinding.class);
			when(layout.handle()).thenReturn(mock(Pointer.class));
			when(layout.bindings()).thenReturn(List.of(binding));
			set = new DescriptorSet(mock(Pointer.class), layout);
		}

		@Test
		public void constructor() {
			assertEquals(layout, set.layout());
		}

		@Test
		public void bind() {
			// Create pipeline layout
			final Pipeline.Layout pipelineLayout = mock(Pipeline.Layout.class);
			when(pipelineLayout.handle()).thenReturn(mock(Pointer.class));

			// Create command
			final Pointer buffer = mock(Pointer.class);
			final Command bind = set.bind(pipelineLayout);
			assertNotNull(bind);

			// Bind descriptor set
			bind.execute(library, buffer);
			verify(library).vkCmdBindDescriptorSets(buffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout.handle(), 0, 1, new Pointer[]{set.handle()}, 0, null);
		}
	}

	@Nested
	class LayoutTests {
		private Layout layout;
		private VkDescriptorSetLayoutBinding binding;

		@BeforeEach
		public void before() {
			binding = new VkDescriptorSetLayoutBinding();
			layout = new Layout(mock(Pointer.class), device, List.of(binding));
		}

		@Test
		public void constructor() {
			assertEquals(List.of(binding), layout.bindings());
		}
	}

	@Nested
	class LayoutBuilderTests {
		private Layout.Builder builder;

		@BeforeEach
		public void before() {
			builder = new Layout.Builder(device);
		}

		@Test
		public void build() {
			final Layout layout = new Layout.Builder(device)
				.binding(1)
					.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_ALL)
				.binding()
					.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
					.size(42)
				.build();

			assertNotNull(layout);
			// TODO - verify VK method
		}

		@Test
		public void buildEmpty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		public void buildMissingDescriptorType() {
			builder.binding().stage(VkShaderStageFlag.VK_SHADER_STAGE_ALL);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		public void buildMissingPipelineStages() {
			builder.binding().type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		public void buildDuplicateBindingIndex() {
			builder
				.binding(0)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_ALL)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			assertThrows(IllegalArgumentException.class, () -> builder.binding(0));
		}

		@Test
		public void buildInvalidBindingIndex() {
			assertThrows(IllegalArgumentException.class, () -> builder.binding(-1));
		}
	}

	@Nested
	class PoolTests {
		private Pool pool;

		@BeforeEach
		public void before() {
			pool = new Pool(mock(Pointer.class), device, 1);
		}

		@Test
		public void constructor() {
			assertEquals(1, pool.max());
		}

		@Test
		public void allocate() {
			// Create a layout
			final VkDescriptorSetLayoutBinding binding = new VkDescriptorSetLayoutBinding();
			final Layout layout = new Layout(mock(Pointer.class), device, List.of(binding));

			// Allocate descriptor sets
			final var sets = pool.allocate(List.of(layout));
			assertNotNull(sets);
			assertEquals(1, sets.size());

			// Check descriptor set
			final DescriptorSet ds = sets.iterator().next();
			// TODO

			// Check allocation
			final VkDescriptorSetAllocateInfo expected = new VkDescriptorSetAllocateInfo();
			expected.descriptorPool = pool.handle();
			expected.descriptorSetCount = 1;
			expected.pSetLayouts = StructureHelper.pointers(List.of(layout.handle()));
			verify(library).vkAllocateDescriptorSets(eq(device.handle()), argThat(structure(expected)), eq(factory.pointers(1)));
		}

		@Test
		public void reset() {
			pool.reset();
			verify(library).vkResetDescriptorPool(device.handle(), pool.handle(), 0);
		}

		@Test
		public void free() {
			final DescriptorSet set = mock(DescriptorSet.class);
			when(set.handle()).thenReturn(mock(Pointer.class));
			pool.free(List.of(set));
			verify(library).vkFreeDescriptorSets(device.handle(), pool.handle(), 1, new Pointer[]{set.handle()});
		}

		@Test
		public void build() {
			pool = new Pool.Builder(device)
				.add(1, VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.max(1)
				.build();

			assertNotNull(pool);
		}

		@Test
		public void buildEmpty() {
			assertThrows(IllegalArgumentException.class, () -> new Pool.Builder(device).build());
		}

		@Test
		public void buildInvalidMaximum() {
			final Pool.Builder builder = new Pool.Builder(device)
				.add(1, VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.add(2, VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
				.max(1);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}

/*

The pool will be a tracked resource so it `extends VulkanHandle` and we add a constructor for the test. As we are using builders the constructor doesn't _need_ to be public, however it's much easier to test if it's exposed, so we leave it at package visibility for the moment.

```
public static class Pool extends VulkanHandle {
	/**
	 * Constructor.
	 * @param handle Pool handle
	 *
	Pool(VulkanHandle handle) {
		super(handle);
	}
```

Obviously this test will fail dismally since we haven't implemented any code yet! Now we start iteratively adding code to satisfy the tests and add any additional tests as we go.

*/
