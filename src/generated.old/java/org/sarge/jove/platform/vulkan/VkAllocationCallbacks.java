package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkAllocationCallbacks extends Structure {
	public static class ByValue extends VkAllocationCallbacks implements Structure.ByValue { }
	public static class ByReference extends VkAllocationCallbacks implements Structure.ByReference { }
	
	public Pointer pUserData;
	public PFN_vkAllocationFunction pfnAllocation;
	public PFN_vkReallocationFunction pfnReallocation;
	public PFN_vkFreeFunction pfnFree;
	public PFN_vkInternalAllocationNotification pfnInternalAllocation;
	public PFN_vkInternalFreeNotification pfnInternalFree;
}
