package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"imageFormatProperties",
	"externalMemoryFeatures",
	"exportFromImportedHandleTypes",
	"compatibleHandleTypes"
})
public class VkExternalImageFormatPropertiesNV extends Structure {
	public static class ByValue extends VkExternalImageFormatPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkExternalImageFormatPropertiesNV implements Structure.ByReference { }
	
	public VkImageFormatProperties imageFormatProperties;
	public int externalMemoryFeatures;
	public int exportFromImportedHandleTypes;
	public int compatibleHandleTypes;
}
