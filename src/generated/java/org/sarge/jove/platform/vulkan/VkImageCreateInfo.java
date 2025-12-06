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
public class VkImageCreateInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkImageCreateFlags> flags;
	public VkImageType imageType;
	public VkFormat format;
	public VkExtent3D extent;
	public int mipLevels;
	public int arrayLayers;
	public EnumMask<VkSampleCountFlags> samples;
	public VkImageTiling tiling;
	public EnumMask<VkImageUsageFlags> usage;
	public VkSharingMode sharingMode;
	public int queueFamilyIndexCount;
	public int[] pQueueFamilyIndices;
	public VkImageLayout initialLayout;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("imageType"),
			JAVA_INT.withName("format"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height"),
				JAVA_INT.withName("depth")
			).withName("extent"),
			JAVA_INT.withName("mipLevels"),
			JAVA_INT.withName("arrayLayers"),
			JAVA_INT.withName("samples"),
			JAVA_INT.withName("tiling"),
			JAVA_INT.withName("usage"),
			JAVA_INT.withName("sharingMode"),
			JAVA_INT.withName("queueFamilyIndexCount"),
			PADDING,
			POINTER.withName("pQueueFamilyIndices"),
			JAVA_INT.withName("initialLayout"),
			PADDING
		);
	}
}
