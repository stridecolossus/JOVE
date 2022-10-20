package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.Builder;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

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
		assertEquals(4, layout.pushConstantsSize());
		assertEquals(STAGES, layout.stages());
	}

	@Test
	void destroy() {
		layout.destroy();
		verify(lib).vkDestroyPipelineLayout(dev, layout, null);
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

			// Init push constants max size
			limit("maxPushConstantsSize", 8);

			// Init push constant data
			final Component data = Component.floats(2);

			// Create layout
			final PipelineLayout layout = builder
					.add(set)
					.push(data, VkShaderStage.VERTEX, VkShaderStage.FRAGMENT)
					.build(dev);

			// Check layout
			assertNotNull(layout);
			assertNotNull(layout.handle());

			// Init expected create descriptor
			final var expected = new VkPipelineLayoutCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					// Check descriptor
					final var actual = (VkPipelineLayoutCreateInfo) obj;
					assertNotNull(actual);
					assertEquals(0, actual.flags);

					// Check descriptor-set layouts
					assertEquals(1, actual.setLayoutCount);
					assertEquals(NativeObject.array(Set.of(set)), actual.pSetLayouts);

					// Check push constants
					assertEquals(1, actual.pushConstantRangeCount);
					assertEquals(0, actual.pPushConstantRanges.offset);
					assertEquals(2 * Float.BYTES, actual.pPushConstantRanges.size);
					assertEquals(IntegerEnumeration.reduce(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT), actual.pPushConstantRanges.stageFlags);

					return true;
				}
			};

			// Check pipeline allocation API
			verify(lib).vkCreatePipelineLayout(dev, expected, null, factory.pointer());
		}

		@Test
		void buildEmpty() {
			limit("maxPushConstantsSize", 0);
			assertNotNull(builder.build(dev));
		}
	}
}
