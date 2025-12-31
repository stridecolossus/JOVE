package org.sarge.jove.platform.vulkan.present;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.memory.MockAllocator;
import org.sarge.jove.util.Mockery;

class CaptureTaskTest {
	private CaptureTask task;
	private Swapchain swapchain;

	@BeforeEach
	void before() {
		final var mockery = new Mockery(Image.Library.class);
		final var device = new MockLogicalDevice(mockery.proxy());
		task = new CaptureTask(new MockAllocator(device), new MockCommandPool(), device);
		swapchain = new MockSwapchain();
	}

	@Test
	void capture() {
		// TODO - this is going to be a bitch to test
		//task.capture(swapchain);
	}
}
