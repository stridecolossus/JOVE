package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.lib.*;
import org.sarge.jove.util.BitMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDebugUtilsMessengerCreateInfoEXT extends NativeStructure {
	public VkStructureType sType = VkStructureType.DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
	public Handle pNext;
	public int flags;
	public BitMask<VkDebugUtilsMessageSeverity> messageSeverity;
	public BitMask<VkDebugUtilsMessageType> messageType;
	public Handle pfnUserCallback;
	public Handle pUserData;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
//				PADDING,
				JAVA_INT.withName("messageSeverity"),
//				PADDING,
				JAVA_INT.withName("messageType"),
				PADDING,
				POINTER.withName("pfnUserCallback"),
				POINTER.withName("pUserData")
		);
	}
}

// TODO - how were we meant to know that there is NO padding above 1. after flags and 2. after severity!!!
