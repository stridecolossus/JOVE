package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.Builder;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;

import com.sun.jna.Pointer;

class PipelineLayoutTest extends AbstractVulkanTest {
	private static final Set<VkShaderStage> STAGES = Set.of(VkShaderStage.VERTEX, VkShaderStage.FRAGMENT);
	private static final VulkanProperty.Key PROPERTY = new VulkanProperty.Key("maxPushConstantsSize");

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

			// Create push constants range
			final PushConstantRange range = new PushConstantRange(0, 4, Set.of(VkShaderStage.VERTEX));

			// Init push constants max size
			property(PROPERTY, 4, true);

			// Create layout
			final PipelineLayout layout = builder
					.add(set)
					.add(range)
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
					assertNotNull(actual.pPushConstantRanges);

					return true;
				}
			};

			// Check pipeline allocation API
			verify(lib).vkCreatePipelineLayout(dev, expected, null, POINTER);
		}

		@Test
		void buildEmpty() {
			property(PROPERTY, 0, true);
			assertNotNull(builder.build(dev));
		}
	}
}
