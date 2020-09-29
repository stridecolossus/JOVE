package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
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
	public static class ByValue extends VkDisplayPlaneCapabilitiesKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPlaneCapabilitiesKHR implements Structure.ByReference { }
	
	public VkDisplayPlaneAlphaFlagsKHR supportedAlpha;
	public VkOffset2D minSrcPosition;
	public VkOffset2D maxSrcPosition;
	public VkExtent2D minSrcExtent;
	public VkExtent2D maxSrcExtent;
	public VkOffset2D minDstPosition;
	public VkOffset2D maxDstPosition;
	public VkExtent2D minDstExtent;
	public VkExtent2D maxDstExtent;
}
