package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker.DefaultFrameTracker;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.Pointer;

public class FrameStateTest extends AbstractVulkanTest {
	private DefaultFrameTracker tracker;
	private LogicalDevice.Queue queue;
	private Command.Buffer buffer;

	@BeforeEach
	public void before() {
		// Create work queue
		queue = mock(LogicalDevice.Queue.class);
		when(queue.device()).thenReturn(device);
		when(queue.work()).thenReturn(new LogicalDevice.Work.Builder(queue));

		// Create command
		buffer = mock(Command.Buffer.class);
		when(buffer.isReady()).thenReturn(true);

		// Create frame tracker
		tracker = new DefaultFrameTracker(device, 2, queue);
	}

	@Test
	public void constructor() {
		assertEquals(0, tracker.index());
	}

	@Test
	public void waitReady() {
		tracker.waitReady();
		verify(library).vkWaitForFences(eq(device.handle()), eq(1), any(Pointer[].class), eq(VulkanBoolean.TRUE), eq(Long.MAX_VALUE));
	}

	@Test
	public void submit() {
		final FrameState frame = tracker.submit(buffer);
		assertNotNull(frame);
		assertNotNull(frame.available());
		assertNotNull(frame.finished());
		assertNotNull(frame.fence());
		assertEquals(1, tracker.index());

		/*
		// Check submitted to queue
		final ArgumentCaptor<WorkQueue.Work> work = ArgumentCaptor.forClass(WorkQueue.Work.class);
//		verify(library).vkQueueSubmit(queue, submitCount, pSubmits, fence)
		verify(queue).submit(work.capture(), any(Fence.class));

		// Check work descriptor
		final VkSubmitInfo info = work.getValue().descriptor();
		assertEquals(1, info.commandBufferCount);
		assertEquals(1, info.waitSemaphoreCount);
		assertEquals(1, info.signalSemaphoreCount);
		assertNotNull(info.pCommandBuffers);
		assertNotNull(info.pWaitSemaphores);
		assertNotNull(info.pSignalSemaphores);
		assertNotNull(info.pWaitDstStageMask);
		*/
	}

	@Test
	public void submitLoop() {
		tracker.submit(buffer);
		tracker.submit(buffer);
		assertEquals(0, tracker.index());
	}

	@Test
	public void destroy() {
		tracker.destroy();
	}
}
