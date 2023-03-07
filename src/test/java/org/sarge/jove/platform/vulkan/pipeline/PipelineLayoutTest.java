package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.Builder;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;

class PipelineLayoutTest {
	private PipelineLayout layout;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		layout = new PipelineLayout(new Handle(1), dev, new PushConstant(List.of()));
	}

	@Test
	void constructor() {
		assertEquals(false, layout.isDestroyed());
		assertNotNull(layout.push());
	}

	@Test
	void destroy() {
		layout.destroy();
		assertEquals(true, layout.isDestroyed());
		verify(dev.library()).vkDestroyPipelineLayout(dev, layout, null);
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
			final var set = mock(DescriptorSet.Layout.class);
			when(set.handle()).thenReturn(new Handle(1));

			// Create push constant ranges
			final Range one = new Range(0, 4, Set.of(VkShaderStage.VERTEX));
			final Range two = new Range(4, 8, Set.of(VkShaderStage.FRAGMENT));

			// Init push constants max size
//			limit("maxPushConstantsSize", 12);

			// Create layout
			final PipelineLayout layout = builder
					.add(set)
					.add(one)
					.add(two)
					.build(dev);

			// Check layout
			assertNotNull(layout);
			assertNotNull(layout.handle());
			assertNotNull(layout.push());

			// Init expected create descriptor
			final var expected = new VkPipelineLayoutCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var actual = (VkPipelineLayoutCreateInfo) obj;
					assertEquals(0, actual.flags);
					assertEquals(1, actual.setLayoutCount);
					assertEquals(NativeObject.array(Set.of(set)), actual.pSetLayouts);
					assertEquals(2, actual.pushConstantRangeCount);
					return true;
				}
			};

			// Check pipeline allocation API
			verify(dev.library()).vkCreatePipelineLayout(dev, expected, null, dev.factory().pointer());
		}

		@Test
		void empty() {
//			limit("maxPushConstantsSize", 0);
			assertNotNull(builder.build(dev));
		}
	}
}
