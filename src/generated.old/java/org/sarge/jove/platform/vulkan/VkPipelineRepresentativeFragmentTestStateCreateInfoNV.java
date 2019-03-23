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
	"representativeFragmentTestEnable"
})
public class VkPipelineRepresentativeFragmentTestStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_REPRESENTATIVE_FRAGMENT_TEST_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public boolean representativeFragmentTestEnable;
}
