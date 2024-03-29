package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"domainOrigin"
})
public class VkPipelineTessellationDomainOriginStateCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineTessellationDomainOriginStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineTessellationDomainOriginStateCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_TESSELLATION_DOMAIN_ORIGIN_STATE_CREATE_INFO;
	public Pointer pNext;
	public VkTessellationDomainOrigin domainOrigin;
}
