package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"pUserData",
	"pfnAllocation",
	"pfnReallocation",
	"pfnFree",
	"pfnInternalAllocation",
	"pfnInternalFree"
})
public class VkAllocationCallbacks extends VulkanStructure {
	public static class ByValue extends VkAllocationCallbacks implements Structure.ByValue { }
	public static class ByReference extends VkAllocationCallbacks implements Structure.ByReference { }

	public Pointer pUserData;
	public Pointer pfnAllocation;
	public Pointer pfnReallocation;
	public Pointer pfnFree;
	public Pointer pfnInternalAllocation;
	public Pointer pfnInternalFree;
}
