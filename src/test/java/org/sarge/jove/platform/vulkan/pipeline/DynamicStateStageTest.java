package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

public class DynamicStateStageTest {
	private DynamicStateStage builder;

	@BeforeEach
	void before() {
		builder = new DynamicStateStage();
	}

	@Test
	void build() {
		final VkPipelineDynamicStateCreateInfo info = builder.state(VkDynamicState.SCISSOR).descriptor();
		assertEquals(0, info.flags);
		assertEquals(1, info.dynamicStateCount);
		assertArrayEquals(new int[]{VkDynamicState.SCISSOR.value()}, info.pDynamicStates);
	}

	@Test
	void empty() {
		assertEquals(null, builder.descriptor());
	}
}
