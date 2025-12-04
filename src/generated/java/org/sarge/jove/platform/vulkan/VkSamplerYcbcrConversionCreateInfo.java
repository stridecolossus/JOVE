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
public class VkSamplerYcbcrConversionCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkFormat format;
	public VkSamplerYcbcrModelConversion ycbcrModel;
	public VkSamplerYcbcrRange ycbcrRange;
	public VkComponentMapping components;
	public VkChromaLocation xChromaOffset;
	public VkChromaLocation yChromaOffset;
	public VkFilter chromaFilter;
	public boolean forceExplicitReconstruction;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("format"),
			JAVA_INT.withName("ycbcrModel"),
			JAVA_INT.withName("ycbcrRange"),
			PADDING,
			MemoryLayout.structLayout(
				JAVA_INT.withName("r"),
				JAVA_INT.withName("g"),
				JAVA_INT.withName("b"),
				JAVA_INT.withName("a")
			).withName("components"),
			JAVA_INT.withName("xChromaOffset"),
			JAVA_INT.withName("yChromaOffset"),
			JAVA_INT.withName("chromaFilter"),
			JAVA_INT.withName("forceExplicitReconstruction")
		);
	}
}
