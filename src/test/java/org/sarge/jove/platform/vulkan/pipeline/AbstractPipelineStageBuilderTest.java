package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class AbstractPipelineStageBuilderTest {
	@Test
	void build() {
		// Create builder
		final AbstractPipelineStageBuilder<Object> builder = new AbstractPipelineStageBuilder<>() {
			@Override
			protected Object result() {
				return null;
			}
		};

		// Check parent
		final Pipeline.Builder parent = mock(Pipeline.Builder.class);
		builder.parent(parent);
		assertEquals(parent, builder.build());
	}
}
