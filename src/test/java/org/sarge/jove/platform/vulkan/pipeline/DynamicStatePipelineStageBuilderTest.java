package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkDynamicState;
import org.sarge.jove.platform.vulkan.VkPipelineDynamicStateCreateInfo;
import org.sarge.jove.util.IntegerArray;

public class DynamicStatePipelineStageBuilderTest {
	private DynamicStatePipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DynamicStatePipelineStageBuilder(null);
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
}
