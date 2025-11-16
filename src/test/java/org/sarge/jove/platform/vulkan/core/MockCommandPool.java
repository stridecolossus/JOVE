package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

public class MockCommandPool extends Command.Pool {
	public MockCommandPool() {
		this(new MockLogicalDevice());
	}

	public MockCommandPool(LogicalDevice device) {
		final var family = new Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(1), family);
		super(new Handle(1), device, queue, device.library());
	}

	@Override
	public List<Buffer> allocate(int number, boolean primary) {
		final var buffer = new Command.Buffer(new Handle(2), this, primary) {
			@Override
			public boolean isReady() {
				return true;
			}
		};
		return Collections.nCopies(number, buffer);
	}
}
