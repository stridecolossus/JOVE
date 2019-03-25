package org.sarge.jove.platform.vulkan;

import java.util.List;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
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
public class VkPipelineVertexInputStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int vertexBindingDescriptionCount;
	public Pointer pVertexBindingDescriptions;
	public int vertexAttributeDescriptionCount;
	public Pointer pVertexAttributeDescriptions;

	// TODO
	public VkPipelineVertexInputStateCreateInfo() {
		final VkVertexInputBindingDescription desc = new VkVertexInputBindingDescription();
		desc.binding = 0;
		desc.stride = (3 + 4) * Float.BYTES;
		desc.inputRate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX;

		final VkVertexInputAttributeDescription vertex = new VkVertexInputAttributeDescription();
		vertex.binding = 0;
		vertex.location = 0;
		vertex.format = VkFormat.VK_FORMAT_R32G32B32_SFLOAT;
		vertex.offset = 0;

		final VkVertexInputAttributeDescription col = new VkVertexInputAttributeDescription();
		col.binding = 0;
		col.location = 1;
		col.format = VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT;
		col.offset = Point.SIZE * Float.BYTES;

		vertexBindingDescriptionCount = 1;
		pVertexBindingDescriptions = StructureHelper.structures(List.of(desc));

		vertexAttributeDescriptionCount = 2;
		pVertexAttributeDescriptions = StructureHelper.structures(List.of(vertex, col));
	}
}
