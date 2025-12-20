package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.util.Mockery;

public class MockVulkanSemaphore extends VulkanSemaphore {
	public MockVulkanSemaphore() {
		final var library = new Mockery(VulkanSemaphore.Library.class).proxy();
		super(new Handle(1), new MockLogicalDevice(library));
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
