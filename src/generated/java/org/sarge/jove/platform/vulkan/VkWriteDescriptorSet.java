package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkWriteDescriptorSet implements NativeStructure {
	public final VkStructureType sType = VkStructureType.WRITE_DESCRIPTOR_SET;
	public Handle pNext;
	public Handle dstSet;
	public int dstBinding;
	public int dstArrayElement;
	public int descriptorCount;
	public VkDescriptorType descriptorType;
	public VkDescriptorImageInfo pImageInfo;
	public VkDescriptorBufferInfo pBufferInfo;
	public Handle pTexelBufferView;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            POINTER.withName("dstSet"),
	            JAVA_INT.withName("dstBinding"),
	            JAVA_INT.withName("dstArrayElement"),
	            JAVA_INT.withName("descriptorCount"),
	            JAVA_INT.withName("descriptorType"),
	            POINTER.withName("pImageInfo"),
	            POINTER.withName("pBufferInfo"),
	            POINTER.withName("pTexelBufferView")
	    );
	}
}
