package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.PhysicalDeviceTest.MockPhysicalDeviceLibrary;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

public class MockPhysicalDevice extends PhysicalDevice {
	public MockPhysicalDevice() {
		this(new MockPhysicalDeviceLibrary());
	}

	public MockPhysicalDevice(PhysicalDevice.Library library) {
		super(new Handle(2), List.of(new Family(0, 1, Set.of())), new MockInstance(), library);
	}
}
