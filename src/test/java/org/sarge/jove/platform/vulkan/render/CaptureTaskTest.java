package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class CaptureTaskTest extends AbstractVulkanTest {
	private CaptureTask task;
	private Swapchain swapchain;
	private Image image;
	private DeviceMemory mem;
	private Pool pool;
	private Buffer buffer;

	@BeforeEach
	void before() {
		// Init swapchain image
		final Descriptor descriptor = new Descriptor.Builder()
				.aspect(VkImageAspect.COLOR)
				.extents(new Dimensions(2, 3))
				.format(FORMAT)
				.build();

		// Create swapchain image
		image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create swapchain attachment
		final View view = mock(View.class);
		when(view.image()).thenReturn(image);

		// Init swapchain
		swapchain = mock(Swapchain.class);
		when(swapchain.latest()).thenReturn(view);
		when(swapchain.device()).thenReturn(dev);

		// Create allocator
		mem = mock(DeviceMemory.class);
		when(allocator.allocate(any(), any())).thenReturn(mem);

		// Init command and pool
		buffer = mock(Buffer.class);
		pool = mock(Pool.class);
		when(pool.allocate()).thenReturn(buffer);
		when(buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)).thenReturn(buffer);
		when(buffer.end()).thenReturn(buffer);
		when(buffer.add(any(Command.class))).thenReturn(buffer);

		// Create capture task
		task = new CaptureTask(pool);
	}

	@Test
	void capture() {
		final Image screenshot = task.capture(swapchain);
		assertNotNull(screenshot);
		assertEquals(VkFormat.R8G8B8A8_UNORM, screenshot.descriptor().format());
		assertEquals(image.descriptor().extents(), screenshot.descriptor().extents());
		// TODO - check API, barriers, copy, etc
	}
}
