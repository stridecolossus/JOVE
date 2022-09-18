package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"supportedAlpha",
	"minSrcPosition",
	"maxSrcPosition",
	"minSrcExtent",
	"maxSrcExtent",
	"minDstPosition",
	"maxDstPosition",
	"minDstExtent",
	"maxDstExtent"
})
public class VkDisplayPlaneCapabilitiesKHR extends VulkanStructure {
	public VkDisplayPlaneAlphaFlagKHR supportedAlpha;
	public VkOffset2D minSrcPosition;
	public VkOffset2D maxSrcPosition;
	public VkExtent2D minSrcExtent;
	public VkExtent2D maxSrcExtent;
	public VkOffset2D minDstPosition;
	public VkOffset2D maxDstPosition;
	public VkExtent2D minDstExtent;
	public VkExtent2D maxDstExtent;
}
