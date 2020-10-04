package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final Map<Integer, VkVertexInputBindingDescription> bindings = new HashMap<>();
	private final List<VkVertexInputAttributeDescription> attributes = new ArrayList<>();

	/**
	 * Starts a new binding.
	 * @return New binding builder
	 */
	public BindingBuilder binding() {
		return new BindingBuilder();
	}

	/**
	 * Starts a new attribute.
	 * @return New attribute builder
	 */
	public AttributeBuilder attribute() {
		return new AttributeBuilder();
	}

	/**
	 * Helper - Adds a vertex input binding and attributes for the given vertex layout.
	 * The binding index is allocated the next available index.
	 * @param layout Vertex layout
	 */
	public VertexInputStageBuilder binding(Vertex.Layout layout) {
		// Allocate next binding
		final int index = bindings.size();

		// Add binding
		new BindingBuilder()
				.binding(index)
				.stride(layout.size() * Float.BYTES)
				.build();

		// Add attribute for each component
		int offset = 0;
		int loc = 0;
		for(Vertex.Component c : layout.components()) {
			// Determine component format
			final VkFormat format = new FormatBuilder()
					.components(c.size())
					.type(FormatBuilder.Type.FLOAT)
					.bytes(Float.BYTES)
					.build();

			// Add attribute for component
			new AttributeBuilder()
					.binding(index)
					.location(loc)
					.format(format)
					.offset(offset)
					.build();

			// Increment offset to the start of the next attribute
			++loc;
			offset += c.size() * Float.BYTES;
		}
		assert offset == layout.size() * Float.BYTES;

		return this;
	}

	/**
	 * Constructs this vertex input stage.
	 * @return New vertex input stage
	 * @throws IllegalArgumentException if no bindings were specified
	 */
	@Override
	protected VkPipelineVertexInputStateCreateInfo result() {
		// TODO - check locations are unique for a given binding

		// Create descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();

		// Add binding descriptions
		info.vertexBindingDescriptionCount = bindings.size();
		info.pVertexBindingDescriptions = StructureHelper.structures(bindings.values());

		// Add attributes
		info.vertexAttributeDescriptionCount = attributes.size();
		info.pVertexAttributeDescriptions = StructureHelper.structures(attributes);

		return info;
	}

	/**
	 * Builder for a vertex input binding.
	 */
	public class BindingBuilder {
		private int binding;
		private int stride;
		private VkVertexInputRate rate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX; // TODO - instancing

		private BindingBuilder() {
		}

		/**
		 * Sets the binding index.
		 * @param binding Binding index
		 */
		public BindingBuilder binding(int binding) {
			this.binding = zeroOrMore(binding);
			return this;
		}

		/**
		 * Sets the vertex stride.
		 * @param stride Vertex stride (bytes)
		 */
		public BindingBuilder stride(int stride) {
			this.stride = oneOrMore(stride);
			return this;
		}

		/**
		 * Sets the input rate.
		 * @param rate Input rate (default is {@link VkVertexInputRate#VK_VERTEX_INPUT_RATE_VERTEX})
		 */
		public BindingBuilder rate(VkVertexInputRate rate) {
			this.rate = notNull(rate);
			return this;
		}

		/**
		 * Constructs this input binding.
		 * @throws IllegalArgumentException for a duplicate binding index or an invalid vertex stride
		 */
		public VertexInputStageBuilder build() {
			// Validate binding description
			if(bindings.containsKey(binding)) throw new IllegalArgumentException("Duplicate binding index: " + binding);
			if(stride == 0) throw new IllegalArgumentException("Invalid vertex stride");

			// Build binding description
			final VkVertexInputBindingDescription desc = new VkVertexInputBindingDescription();
			desc.binding = binding;
			desc.stride = stride;
			desc.inputRate = rate;

			// Add binding
			bindings.put(binding, desc);

			return VertexInputStageBuilder.this;
		}
	}

	/**
	 * Builder for a vertex input attribute.
	 */
	public class AttributeBuilder {
		private int binding;
		private int loc;
		private VkFormat format;
		private int offset;

		private AttributeBuilder() {
		}

		/**
		 * Sets the binding index of this attribute.
		 * @param binding Binding index
		 */
		public AttributeBuilder binding(int binding) {
			this.binding = zeroOrMore(binding);
			return this;
		}

		/**
		 * Sets the vertex shader location of this attribute.
		 * @param loc Shader location
		 */
		public AttributeBuilder location(int loc) {
			this.loc = zeroOrMore(loc);
			return this;
		}

		/**
		 * Sets the attribute format.
		 * @param format Attribute format
		 */
		public AttributeBuilder format(VkFormat format) {
			this.format = notNull(format);
			return this;
		}

		/**
		 * Sets the offset of this attribute within a vertex.
		 * @param offset Attribute offset (bytes)
		 */
		public AttributeBuilder offset(int offset) {
			this.offset = zeroOrMore(offset);
			return this;
		}

		/**
		 * Constructs this attribute description.
		 * @throws IllegalArgumentException if the attribute format is not specified, the binding is not present, or the offset exceeds the vertex stride
		 */
		public VertexInputStageBuilder build() {
			// Validate attribute
			final VkVertexInputBindingDescription desc = bindings.get(binding);
			if(desc == null) throw new IllegalArgumentException("Invalid binding index for attribute: " + binding);
			if(offset >= desc.stride) throw new IllegalArgumentException("Offset exceeds vertex stride");
			if(format == null) throw new IllegalArgumentException("No format specified for attribute");

			// Create attribute description
			final VkVertexInputAttributeDescription attr = new VkVertexInputAttributeDescription();
			attr.binding = binding;
			attr.location = loc;
			attr.format = format;
			attr.offset = offset;

			// Add attribute
			attributes.add(attr);

			return VertexInputStageBuilder.this;
		}
	}
}
