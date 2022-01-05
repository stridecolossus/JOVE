package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkDynamicState;
import org.sarge.jove.platform.vulkan.VkPipelineDynamicStateCreateInfo;
import org.sarge.jove.util.IntegerArray;

public class DynamicStatePipelineStageBuilderTest {
	private DynamicStatePipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DynamicStatePipelineStageBuilder();
	}

	@Test
	void build() {
		final VkPipelineDynamicStateCreateInfo info = builder.state(VkDynamicState.SCISSOR).get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals(1, info.dynamicStateCount);
		assertEquals(new IntegerArray(new int[]{VkDynamicState.SCISSOR.value()}), info.pDynamicStates);
	}

	@Test
	void buildEmpty() {
		assertEquals(null, builder.get());
	}

	@Test
	void copy() {
		final var copy = new DynamicStatePipelineStageBuilder();
		builder.state(VkDynamicState.SCISSOR);
		copy.copy(builder);
		assertNotNull(copy);
		assertNotNull(copy.get());
		assertNotSame(builder.get(), copy.get());
	}
}
