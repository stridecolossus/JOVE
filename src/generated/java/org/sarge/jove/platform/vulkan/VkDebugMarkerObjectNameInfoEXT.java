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
public class VkDebugMarkerObjectNameInfoEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkDebugReportObjectTypeEXT objectType;
	public long object;
	public String pObjectName;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("objectType"),
			PADDING,
			JAVA_LONG.withName("object"),
			POINTER.withName("pObjectName")
		);
	}
}
