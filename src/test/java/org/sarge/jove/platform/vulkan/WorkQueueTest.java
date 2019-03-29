package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.vulkan.WorkQueue.Work;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;

public class WorkQueueTest extends AbstractVulkanTest {
	private WorkQueue queue;

	@BeforeEach
	public void before() {
		queue = new WorkQueue(mock(Pointer.class), library);
	}

	@Test
	public void submit() {
		// Create some work
		final VkSubmitInfo info = mock(VkSubmitInfo.class);
		final Work work = mock(Work.class);
		when(work.descriptor()).thenReturn(info);

		// Submit work
		queue.submit(work);
		verify(library).vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, null);

		// Create fence
		final Fence fence = mock(Fence.class);
		when(fence.handle()).thenReturn(mock(Pointer.class));

		// Submit work with fence
		queue.submit(work, fence);
		verify(library).vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, fence.handle());
	}

	@Test
	public void waitIdle() {
		queue.waitIdle();
		verify(library).vkQueueWaitIdle(queue.handle());
	}

	@Test
	public void build() {
		// Create recorded command buffer
		final Command.Buffer cmd = mock(Command.Buffer.class);
		when(cmd.handle()).thenReturn(mock(Pointer.class));
		when(cmd.isReady()).thenReturn(true);

		// Create semaphore
		final PointerHandle semaphore = mock(PointerHandle.class);
		when(semaphore.handle()).thenReturn(mock(Pointer.class));

		// Create work
		final Work work = new Work.Builder()
			.add(cmd)
			.wait(VkPipelineStageFlag.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
			.wait(semaphore)
			.signal(semaphore)
			.build();

		// Build expected descriptor
		final VkSubmitInfo info = work.descriptor();
		final VkSubmitInfo expected = new VkSubmitInfo();
		expected.pWaitDstStageMask = StructureHelper.integers(new int[]{VkPipelineStageFlag.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT.value()});
		expected.commandBufferCount = 1;
		expected.pCommandBuffers = StructureHelper.pointers(Arrays.asList(cmd.handle()));
		expected.waitSemaphoreCount = 1;
		expected.pWaitSemaphores = StructureHelper.pointers(Arrays.asList(semaphore.handle()));
		expected.signalSemaphoreCount = 1;
		expected.pSignalSemaphores = StructureHelper.pointers(Arrays.asList(semaphore.handle()));

		// Check work descriptor
		assertNotNull(info);
		assertEquals(true, expected.dataEquals(info));
	}

	@Test
	public void buildEmptyCommandBuffers() {
		assertThrows(IllegalArgumentException.class, () -> new Work.Builder().build());
	}

	@Test
	public void buildInvalidCommandBuffer() {
		final Command.Buffer cmd = mock(Command.Buffer.class);
		when(cmd.isReady()).thenReturn(false);
		assertThrows(IllegalArgumentException.class, () -> new Work.Builder().build());
	}
}
