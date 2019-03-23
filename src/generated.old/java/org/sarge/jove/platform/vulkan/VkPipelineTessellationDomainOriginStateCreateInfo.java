package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"domainOrigin"
})
public class VkPipelineTessellationDomainOriginStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineTessellationDomainOriginStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineTessellationDomainOriginStateCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_DOMAIN_ORIGIN_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int domainOrigin;
}
