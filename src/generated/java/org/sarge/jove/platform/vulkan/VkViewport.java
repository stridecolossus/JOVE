package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y",
	"width",
	"height",
	"minDepth",
	"maxDepth"
})
public class VkViewport extends VulkanStructure implements ByReference {
	public float x;
	public float y;
	public float width;
	public float height;
	public float minDepth;
	public float maxDepth;
}
