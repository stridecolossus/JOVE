package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"externalMemoryFeatures",
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes"
})
public class VkExternalMemoryProperties extends Structure {
	public static class ByValue extends VkExternalMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkExternalMemoryProperties implements Structure.ByReference { }
	
	public int externalMemoryFeatures;
	public int exportFromImportedHandleTypes;
	public int compatibleHandleTypes;
}
