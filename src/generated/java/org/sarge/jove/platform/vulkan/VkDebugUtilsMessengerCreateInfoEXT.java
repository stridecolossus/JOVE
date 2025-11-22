package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDebugUtilsMessengerCreateInfoEXT implements NativeStructure {
	public final VkStructureType sType = VkStructureType.DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
	public Handle pNext;
	public int flags;
	public EnumMask<VkDebugUtilsMessageSeverity> messageSeverity;
	public EnumMask<VkDebugUtilsMessageType> messageType;
	public Handle pfnUserCallback;
	public Handle pUserData;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				JAVA_INT.withName("flags"),
				JAVA_INT.withName("messageSeverity"),
				JAVA_INT.withName("messageType"),
				PADDING,
				POINTER.withName("pfnUserCallback"),
				POINTER.withName("pUserData")
		);
	}
}
