package org.sarge.jove.platform.vulkan;

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
	"flags",
	"vertexBindingDescriptionCount",
	"pVertexBindingDescriptions",
	"vertexAttributeDescriptionCount",
	"pVertexAttributeDescriptions"
})
public class VkPipelineVertexInputStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineVertexInputStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineVertexInputStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public VkVertexInputBindingDescription pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public VkVertexInputAttributeDescription pVertexAttributeDescriptions;

	/**
	 * Builder for a pipeline vertex input descriptor.
	 */
	public static class Builder {
		// TODO

		/**
		 * Constructs this vertex input descriptor.
		 * @return New descriptor
		 */
		public VkPipelineVertexInputStateCreateInfo build() {
			final VkPipelineVertexInputStateCreateInfo info = new VkPipelineVertexInputStateCreateInfo();
			// TODO
			return info;
		}
	}
}
