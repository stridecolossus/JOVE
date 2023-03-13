package org.sarge.jove.platform.vulkan.pipeline;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.util.*;

class DelegatePipelineBuilderTest {
	@SuppressWarnings("unchecked")
	@Test
	void build() {
		final DelegatePipelineBuilder<MockStructure> builder = spy(DelegatePipelineBuilder.class);
		final var dev = new MockDeviceContext();
		final var layout = new PipelineLayout.Builder().build(dev);
		final var info = new MockStructure();
		when(builder.type()).thenReturn(VkPipelineBindPoint.GRAPHICS);
		when(builder.identity()).thenReturn(info);
		builder.build(dev, layout);
		verify(builder).populate(BitMask.of(), layout, null, -1, info);
	}
}
