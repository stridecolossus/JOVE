package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.Builder;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.PushConstantRange;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;

class PipelineLayoutTest extends AbstractVulkanTest {
	private static final Set<VkShaderStage> STAGES = Set.of(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT);

	private PipelineLayout layout;

	@BeforeEach
	void before() {
		layout = new PipelineLayout(new Pointer(1), dev, 4, STAGES);
	}

	@Test
	void constructor() {
		assertEquals(false, layout.isDestroyed());
		assertEquals(4, layout.max());
		assertEquals(STAGES, layout.stages());
	}

	@Test
	void destroy() {
		layout.destroy();
		verify(lib).vkDestroyPipelineLayout(dev, layout, null);
	}

	@Nested
	class PushConstantRangeTests {
		private PushConstantRange range;

		@BeforeEach
		void before() {
			range = new PushConstantRange(4, 8, STAGES);
		}

		@Test
		void constructor() {
			assertEquals(4, range.offset());
			assertEquals(8, range.size());
			assertEquals(STAGES, range.stages());
			assertEquals(4 + 8, range.length());
		}

		@Test
		void populate() {
			final var info = new VkPushConstantRange();
			range.populate(info);
			assertEquals(4, info.offset);
			assertEquals(8, info.size);
			assertEquals(IntegerEnumeration.mask(STAGES), info.stageFlags);
		}

		@Test
		void constructorInvalidOffsetAlignment() {
			assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(3, 4, Set.of(VkShaderStage.VERTEX)));
		}

		@Test
		void constructorInvalidSizeAlignment() {
			assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(0, 3, Set.of(VkShaderStage.VERTEX)));
		}

		@Test
		void constructorEmptyStages() {
			assertThrows(IllegalArgumentException.class, () -> new PushConstantRange(0, 4, Set.of()));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			// Create descriptor set layout
			final DescriptorLayout set = mock(DescriptorLayout.class);
			when(set.handle()).thenReturn(new Handle(1));

			// Create push constants range
			final PushConstantRange range = new PushConstantRange(0, 4, Set.of(VkShaderStage.VERTEX));

			// Create layout
			final PipelineLayout layout = builder
					.add(set)
					.add(range)
					.build(dev);

			// Check layout
			assertNotNull(layout);
			assertNotNull(layout.handle());

			// Check pipeline allocation
			final ArgumentCaptor<VkPipelineLayoutCreateInfo> captor = ArgumentCaptor.forClass(VkPipelineLayoutCreateInfo.class);
			verify(lib).vkCreatePipelineLayout(eq(dev), captor.capture(), isNull(), eq(POINTER));

			// Check descriptor
			final VkPipelineLayoutCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(0, info.flags);

			// Check descriptor-set layouts
			assertEquals(1, info.setLayoutCount);
			assertEquals(NativeObject.array(Set.of(set)), info.pSetLayouts);

			// Check push constants
			assertEquals(1, info.pushConstantRangeCount);
			assertNotNull(info.pPushConstantRanges);
		}

		@Test
		void buildEmpty() {
			assertNotNull(builder.build(dev));
		}
	}
}
