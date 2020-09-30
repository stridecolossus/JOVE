package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkVertexInputAttributeDescription;
import org.sarge.jove.platform.vulkan.VkVertexInputBindingDescription;
import org.sarge.jove.platform.vulkan.VkVertexInputRate;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the vertex input pipeline stage descriptor.
 * @author Sarge
 */
public class VertexInputStageBuilder extends AbstractPipelineStageBuilder<VkPipelineVertexInputStateCreateInfo> {
	private final List<VkVertexInputBindingDescription> bindings = new ArrayList<>();
	private final List<VkVertexInputAttributeDescription> attributes = new ArrayList<>();

	/**
	 * Adds a binding description.
	 * @param layout Buffer layout
	 * @throws IllegalArgumentException for a duplicate binding index
	 * @throws IllegalArgumentException for a duplicate attribute location index
	 */
	public VertexInputStageBuilder binding(DataBuffer.Layout layout) {
		// Add binding descriptor
		if(bindings.stream().anyMatch(d -> d.binding == layout.binding())) {
			throw new IllegalArgumentException("Duplicate binding index: " + layout.binding());
		}
		final VkVertexInputBindingDescription binding = new VkVertexInputBindingDescription();
		binding.binding = layout.binding();
		binding.stride = layout.stride();
		binding.inputRate = rate(layout);
		bindings.add(binding);

		// Add attribute descriptors
		for(DataBuffer.Layout.Attribute attribute : layout.attributes()) {
			// Check location
			if(attributes.stream().anyMatch(attr -> attr.location == attribute.location())) {
				throw new IllegalArgumentException("Duplicate attribute location: " + attribute.location());
			}

			// Determine Vulkan format for this attribute
			final Vertex.Component component = attribute.component();
			final VkFormat format = new FormatBuilder()
				.components(component.size())
				.type(component.type())
				.bytes(component.bytes())
				.build();

			// Add attribute descriptor
			final VkVertexInputAttributeDescription attr = new VkVertexInputAttributeDescription();
			attr.binding = binding.binding;
			attr.location = attribute.location();
			attr.format = format;
			attr.offset = attribute.offset();
			attributes.add(attr);
		}

		return this;
	}

	/**
	 *
	 * @param layout
	 * @return
	 */
	private static VkVertexInputRate rate(DataBuffer.Layout layout) {
		switch(layout.rate()) {
		case VERTEX:		return VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX;
		case INSTANCE:		return VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE;
		default:			throw new UnsupportedOperationException("Unsupported input rate: " + layout.rate());
		}
	}

	/**
	 * Constructs this vertex input stage.
	 * @return New vertex input stage
	 * @throws IllegalArgumentException if no bindings were specified
	 */
	@Override
	protected VkPipelineVertexInputStateCreateInfo result() {
		// Create descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();

		// Add binding descriptions
		info.vertexBindingDescriptionCount = bindings.size();
		info.pVertexBindingDescriptions = StructureHelper.structures(bindings);

		// Add attributes
		info.vertexAttributeDescriptionCount = attributes.size();
		info.pVertexAttributeDescriptions = StructureHelper.structures(attributes);

		return info;
	}
}
