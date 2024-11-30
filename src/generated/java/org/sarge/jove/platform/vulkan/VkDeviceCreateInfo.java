package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDeviceCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.DEVICE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int queueCreateInfoCount;
	public VkDeviceQueueCreateInfo pQueueCreateInfos;
	public int enabledLayerCount;
	public String[] ppEnabledLayerNames;
	public int enabledExtensionCount;
	public String[] ppEnabledExtensionNames;
	public VkPhysicalDeviceFeatures pEnabledFeatures;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("queueCreateInfoCount"),
				POINTER.withName("pQueueCreateInfos"),
				JAVA_INT.withName("enabledLayerCount"),
				PADDING,
				POINTER.withName("ppEnabledLayerNames"),
				JAVA_INT.withName("enabledExtensionCount"),
				PADDING,
				POINTER.withName("ppEnabledExtensionNames"),
				POINTER.withName("pEnabledFeatures")
		);
	}
}
