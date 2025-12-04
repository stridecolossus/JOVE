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
public class VkDisplaySurfaceCreateInfoKHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int flags;
	public Handle displayMode;
	public int planeIndex;
	public int planeStackIndex;
	public EnumMask<VkSurfaceTransformFlagsKHR> transform;
	public float globalAlpha;
	public EnumMask<VkDisplayPlaneAlphaFlagsKHR> alphaMode;
	public VkExtent2D imageExtent;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			PADDING,
			POINTER.withName("displayMode"),
			JAVA_INT.withName("planeIndex"),
			JAVA_INT.withName("planeStackIndex"),
			JAVA_INT.withName("transform"),
			JAVA_FLOAT.withName("globalAlpha"),
			JAVA_INT.withName("alphaMode"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("imageExtent")
		);
	}
}
