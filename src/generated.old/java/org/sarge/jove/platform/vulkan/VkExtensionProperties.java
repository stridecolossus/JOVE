package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"extensionName",
	"specVersion"
})
public class VkExtensionProperties extends Structure {
	public static class ByValue extends VkExtensionProperties implements Structure.ByValue { }
	public static class ByReference extends VkExtensionProperties implements Structure.ByReference { }
	
	public final byte[] extensionName = new byte[256];
	public int specVersion;
}
