package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"presentID",
	"desiredPresentTime"
})
public class VkPresentTimeGOOGLE extends Structure {
	public static class ByValue extends VkPresentTimeGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkPresentTimeGOOGLE implements Structure.ByReference { }
	
	public int presentID;
	public long desiredPresentTime;
}
