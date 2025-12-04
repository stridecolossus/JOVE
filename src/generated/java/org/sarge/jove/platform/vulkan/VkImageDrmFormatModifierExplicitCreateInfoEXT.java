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
public class VkImageDrmFormatModifierExplicitCreateInfoEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public long drmFormatModifier;
	public int drmFormatModifierPlaneCount;
	public VkSubresourceLayout[] pPlaneLayouts;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_LONG.withName("drmFormatModifier"),
			JAVA_INT.withName("drmFormatModifierPlaneCount"),
			PADDING,
			POINTER.withName("pPlaneLayouts")
		);
	}
}
