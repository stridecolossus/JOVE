package org.sarge.jove.platform.vulkan.pipeline;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.DeviceContext;

public class AbstractPipelineBuilderTest {
	@Test
	void build() {
		final DelegatePipelineBuilder<?> builder = mock(DelegatePipelineBuilder.class);
		final DeviceContext dev = mock(DeviceContext.class);
		final PipelineLayout layout = mock(PipelineLayout.class);
		builder.build(dev, layout);
	}
}
