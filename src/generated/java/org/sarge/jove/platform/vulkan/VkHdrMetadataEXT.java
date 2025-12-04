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
public class VkHdrMetadataEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkXYColorEXT displayPrimaryRed;
	public VkXYColorEXT displayPrimaryGreen;
	public VkXYColorEXT displayPrimaryBlue;
	public VkXYColorEXT whitePoint;
	public float maxLuminance;
	public float minLuminance;
	public float maxContentLightLevel;
	public float maxFrameAverageLightLevel;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				JAVA_FLOAT.withName("x"),
				JAVA_FLOAT.withName("y")
			).withName("displayPrimaryRed"),
			MemoryLayout.structLayout(
				JAVA_FLOAT.withName("x"),
				JAVA_FLOAT.withName("y")
			).withName("displayPrimaryGreen"),
			MemoryLayout.structLayout(
				JAVA_FLOAT.withName("x"),
				JAVA_FLOAT.withName("y")
			).withName("displayPrimaryBlue"),
			MemoryLayout.structLayout(
				JAVA_FLOAT.withName("x"),
				JAVA_FLOAT.withName("y")
			).withName("whitePoint"),
			JAVA_FLOAT.withName("maxLuminance"),
			JAVA_FLOAT.withName("minLuminance"),
			JAVA_FLOAT.withName("maxContentLightLevel"),
			JAVA_FLOAT.withName("maxFrameAverageLightLevel")
		);
	}
}
