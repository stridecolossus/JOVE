package org.sarge.jove.platform.vulkan.present;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.*;

class FrameIteratorTest {
	private FrameIterator iterator;

	@BeforeEach
	void before() {
		iterator = new FrameIterator(new MockLogicalDevice(), 2) {
			@Override
			protected FrameState create(int index, LogicalDevice device) {
				return new FrameState(index, new MockVulkanSemaphore(), new MockVulkanSemaphore(), new MockFence());
			}
		};
	}

	@Test
	void next() {
		final FrameState next = iterator.next();
		assertEquals(0, next.index());
	}

	@Test
	void cycle() {
		assertEquals(0, iterator.next().index());
		assertEquals(1, iterator.next().index());
		assertEquals(0, iterator.next().index());
	}

	@Test
	void destroy() {
		iterator.destroy();
	}
}
