package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.common.SupportedFeatures;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

public class MockPhysicalDevice extends PhysicalDevice {

	MockPhysicalDevice(VulkanLibrary lib) {
		final Family family = new Family(0, 1, Set.of());
		final var features = new SupportedFeatures(new VkPhysicalDeviceFeatures());
		super(new Handle(2), lib, List.of(family), features);
	}
}
// TODO - not really needed?
