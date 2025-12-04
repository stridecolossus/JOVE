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
public class VkDescriptorUpdateTemplateEntry implements NativeStructure {
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
	public VkDescriptorType descriptorType;
	public long offset;
	public long stride;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("dstBinding"),
			JAVA_INT.withName("dstArrayElement"),
			JAVA_INT.withName("descriptorCount"),
			JAVA_INT.withName("descriptorType"),
			JAVA_LONG.withName("offset"),
			JAVA_LONG.withName("stride")
		);
	}
}
