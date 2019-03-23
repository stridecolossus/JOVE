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
	"protectedSubmit"
})
public class VkProtectedSubmitInfo extends Structure {
	public static class ByValue extends VkProtectedSubmitInfo implements Structure.ByValue { }
	public static class ByReference extends VkProtectedSubmitInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PROTECTED_SUBMIT_INFO.value();
	public Pointer pNext;
	public boolean protectedSubmit;
}
