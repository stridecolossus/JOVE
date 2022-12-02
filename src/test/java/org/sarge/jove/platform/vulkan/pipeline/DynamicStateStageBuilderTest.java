package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.NativeHelper.PointerToIntArray;

public class DynamicStateStageBuilderTest {
	private DynamicStateStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DynamicStateStageBuilder();
	}

	@Test
	void build() {
		final VkPipelineDynamicStateCreateInfo info = builder.state(VkDynamicState.SCISSOR).get();
		assertEquals(0, info.flags);
		assertEquals(1, info.dynamicStateCount);
		assertEquals(new PointerToIntArray(new int[]{VkDynamicState.SCISSOR.value()}), info.pDynamicStates);
	}

	@Test
	void empty() {
		assertEquals(null, builder.get());
	}
}
