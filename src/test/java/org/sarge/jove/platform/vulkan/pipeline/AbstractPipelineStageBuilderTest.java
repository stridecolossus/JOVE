package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

public class AbstractPipelineStageBuilderTest {
	@Test
	void build() {
		final Builder parent = mock(Builder.class);
		final var builder = spy(AbstractPipelineStageBuilder.class);
		builder.parent(parent);
		assertEquals(parent, builder.build());
	}
}
