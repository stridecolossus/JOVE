package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

public class AbstractPipelineStageBuilderTest {
	@Test
	void build() {
		// Create parent builder
		final Builder parent = mock(Builder.class);

		// Create builder
		final AbstractPipelineStageBuilder<Object> builder = new AbstractPipelineStageBuilder<>(parent) {
			@Override
			protected Object get() {
				return null;
			}
		};

		// Check parent
		assertEquals(parent, builder.build());
	}
}
