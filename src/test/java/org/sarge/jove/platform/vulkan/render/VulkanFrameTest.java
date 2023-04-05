package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class VulkanFrameTest {
	@Test
	void array() {
		final var frame = mock(VulkanFrame.class);
		assertArrayEquals(new VulkanFrame[]{frame, frame}, VulkanFrame.array(2, () -> frame));
	}
}
