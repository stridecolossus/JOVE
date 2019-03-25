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
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker.DefaultFrameTracker;

import com.sun.jna.Pointer;

public class FrameStateTest extends AbstractVulkanTest {
	private DefaultFrameTracker tracker;
	private WorkQueue queue;
	private Command.Buffer buffer;

	@BeforeEach
	public void before() {
		queue = mock(WorkQueue.class);
		buffer = mock(Command.Buffer.class);
		when(buffer.isReady()).thenReturn(true);
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
		// Submit next frame
		final FrameState frame = tracker.submit(buffer);
		assertNotNull(frame);
		assertNotNull(frame.available());
		assertNotNull(frame.finished());
		assertNotNull(frame.fence());
		assertEquals(1, tracker.index());

		// Check submitted to queue
		final ArgumentCaptor<WorkQueue.Work> work = ArgumentCaptor.forClass(WorkQueue.Work.class);
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