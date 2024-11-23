package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkInstanceCreateInfo extends NativeStructure {
	public final VkStructureType sType = VkStructureType.INSTANCE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public VkApplicationInfo pApplicationInfo;
	public int enabledLayerCount;
	public String[] ppEnabledLayerNames;
	public int enabledExtensionCount;
	public String[] ppEnabledExtensionNames;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
    			JAVA_INT.withName("sType"),
    			PADDING,
    			POINTER.withName("pNext"),
    			JAVA_INT.withName("flags"),
    			PADDING,
    			POINTER.withName("pApplicationInfo"),
    			JAVA_INT.withName("enabledLayerCount"),
    			PADDING,
    			POINTER.withName("ppEnabledLayerNames"),
    			JAVA_INT.withName("enabledExtensionCount"),
    			PADDING,
    			POINTER.withName("ppEnabledExtensionNames")
		);
	}
}
