package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.util.MockStructure;

public class AbstractPipelineStageBuilderTest {
	private AbstractPipelineStageBuilder<MockStructure, ?> builder;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		builder = spy(AbstractPipelineStageBuilder.class);
	}

	@Test
	void build() {
		final Builder parent = mock(Builder.class);
		builder.parent(parent);
		assertEquals(parent, builder.build());
	}
}
