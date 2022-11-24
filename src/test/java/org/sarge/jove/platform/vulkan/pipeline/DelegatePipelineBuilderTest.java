package org.sarge.jove.platform.vulkan.pipeline;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.util.*;

class DelegatePipelineBuilderTest {
	@SuppressWarnings("unchecked")
	@Test
	void build() {
		final DelegatePipelineBuilder<MockStructure> builder = spy(DelegatePipelineBuilder.class);
		final var dev = mock(DeviceContext.class);
		final var layout = mock(PipelineLayout.class);
		final var info = new MockStructure();
		when(builder.type()).thenReturn(VkPipelineBindPoint.GRAPHICS);
		when(builder.identity()).thenReturn(info);
		builder.build(dev, layout);
		verify(builder).populate(new BitMask<>(0), layout, null, -1, info);
	}
}
