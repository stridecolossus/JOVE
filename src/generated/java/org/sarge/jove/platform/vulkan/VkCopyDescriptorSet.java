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
public class VkCopyDescriptorSet implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public Handle srcSet;
	public int srcBinding;
	public int srcArrayElement;
	public Handle dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("srcSet"),
			JAVA_INT.withName("srcBinding"),
			JAVA_INT.withName("srcArrayElement"),
			POINTER.withName("dstSet"),
			JAVA_INT.withName("dstBinding"),
			JAVA_INT.withName("dstArrayElement"),
			JAVA_INT.withName("descriptorCount"),
			PADDING
		);
	}
}
