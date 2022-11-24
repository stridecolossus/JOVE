package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.MockStructure;

class AbstractStageBuilderTest {
	private AbstractStageBuilder<MockStructure> builder;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		builder = spy(AbstractStageBuilder.class);
	}

	@Test
	void build() {
		final var parent = mock(GraphicsPipelineBuilder.class);
		builder.parent(parent);
		assertEquals(parent, builder.build());
	}
}
