package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDeviceTest.MockLogicalDeviceLibrary;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

public class MockLogicalDevice extends LogicalDevice {
	public MockLogicalDevice() {
		this(new MockLogicalDeviceLibrary());
	}

	public MockLogicalDevice(LogicalDevice.Library library) {
		final var family = new Family(0, 1, Set.of());
		final var queue = new WorkQueue(new Handle(2), family);
		super(new Handle(1), library, Map.of(family, List.of(queue)));
	}
}
