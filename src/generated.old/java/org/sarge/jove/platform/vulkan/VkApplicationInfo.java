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
	"pApplicationName",
	"applicationVersion",
	"pEngineName",
	"engineVersion",
	"apiVersion"
})
public class VkApplicationInfo extends Structure {
	public static class ByValue extends VkApplicationInfo implements Structure.ByValue { }
	public static class ByReference extends VkApplicationInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO.value();
	public Pointer pNext;
	public String pApplicationName;
	public int applicationVersion;
	public String pEngineName;
	public int engineVersion;
	public int apiVersion;
}
