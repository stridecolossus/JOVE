package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDebugUtilsMessengerCreateInfoEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public EnumMask<VkDebugUtilsMessageSeverityFlagsEXT> messageSeverity;
	public EnumMask<VkDebugUtilsMessageTypeFlagsEXT> messageType;
	public Handle pfnUserCallback;
	public Handle pUserData;

	@Override
	public GroupLayout layout() {
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
