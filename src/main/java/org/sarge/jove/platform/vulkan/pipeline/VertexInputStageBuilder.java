package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sarge.jove.common.Component.Layout;
import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkVertexInputAttributeDescription;
import org.sarge.jove.platform.vulkan.VkVertexInputBindingDescription;
import org.sarge.jove.platform.vulkan.VkVertexInputRate;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.platform.vulkan.util.StructureCollector;

/**
 * Builder for the vertex input pipeline stage descriptor.
 * @see VkPipelineVertexInputStateCreateInfo
 * @author Sarge
 */
public class VertexInputStageBuilder extends AbstractPipelineBuilder<VkPipelineVertexInputStateCreateInfo> {
	private final Map<Integer, BindingBuilder> bindings = new HashMap<>();
	private final List<AttributeBuilder> attributes = new ArrayList<>();

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
	 * Helper - Adds a vertex input binding and attributes for the given model.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The binding index is allocated as the next available index</li>
	 * <li>Vertex data is assumed to be contiguous, i.e. the offset of each component is the end of the previous element</li>
	 * </ul>
	 * <p>
	 * Using this helper is equivalent to the following code:
	 * <pre>
	 *  // Define model header
	 *  Model.Header header = ...
	 *
	 *  // Allocate binding index
	 *  int index = ...
	 *
	 *  // Create builder
	 *  VertexInputStageBuilder builder = new VertexInputStageBuilder();
	 *
	 *  // Configure binding
	 *  builder
	 *  	.binding()
	 *  	.index(index)
	 *  	.stride(header.stride())
	 *  	.build();
	 *
	 *  // Configure vertex attribute for each component of the model
	 *  int loc = 0;
	 *  int offset = 0;
	 *  for(Layout layout : header.layout()) {
	 *  	builder
	 *  		.attribute()
	 *  		.binding(index)
	 *  		.location(loc)
	 *  		.format(FormatBuilder.format(layout))
	 *  		.offset(offset)
	 *  		.build();
	 *
	 *  	++loc;
	 *  	offset += layout.length();
	 *  }
	 * </pre>
	 * @param header Model header
	 */
	public VertexInputStageBuilder binding(Model.Header header) {
		// Allocate next binding
		final int index = bindings.size();

		// Calculate vertex stride for this layout
		final int stride = header.stride();

		// Add binding
		new BindingBuilder()
				.index(index)
				.stride(stride)
				.build();

		// Add attribute for each layout component
		int offset = 0;
		int loc = 0;
		for(Layout layout : header.layout()) {
			// Determine component format
			final VkFormat format = FormatBuilder.format(layout);

			// Add attribute for component
			new AttributeBuilder()
					.binding(index)
					.location(loc)
					.format(format)
					.offset(offset)
					.build();

			// Increment offset to the start of the next attribute
			++loc;
			offset += layout.length();
			// TODO - assumes contiguous => introduce offset (default 0)
		}
		assert offset == stride;

		return this;
	}

	/**
	 * Constructs this vertex input stage.
	 * @return New vertex input stage
	 * @throws IllegalArgumentException for any binding without attributes
	 */
	@Override
	protected VkPipelineVertexInputStateCreateInfo result() {
		// Validate bindings
		for(final var b : bindings.values()) {
			if(b.locations.isEmpty()) {
				throw new IllegalArgumentException(String.format("No attributes specified for binding: ", b.binding));
			}
		}

		// Create descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();

		// Add binding descriptions
		if(!bindings.isEmpty()) {
			info.vertexBindingDescriptionCount = bindings.size();
			info.pVertexBindingDescriptions = StructureCollector.toPointer(bindings.values(), VkVertexInputBindingDescription::new, BindingBuilder::populate);

			// Add attributes
			info.vertexAttributeDescriptionCount = attributes.size();
			info.pVertexAttributeDescriptions = StructureCollector.toPointer(attributes, VkVertexInputAttributeDescription::new, AttributeBuilder::populate);
		}

		return info;
	}

	/**
	 * Builder for a vertex input binding.
	 */
	public class BindingBuilder {
		private int binding;
		private int stride;
		private VkVertexInputRate rate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX; // TODO - instancing
		private final Set<Integer> locations = new HashSet<>();

		private BindingBuilder() {
		}

		/**
		 * Sets the binding index.
		 * @param binding Binding index
		 */
		public BindingBuilder index(int binding) {
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

		private void populate(VkVertexInputBindingDescription desc) {
			desc.binding = binding;
			desc.stride = stride;
			desc.inputRate = rate;
		}

		/**
		 * Constructs this input binding.
		 * @throws IllegalArgumentException for a duplicate binding index or an invalid vertex stride
		 */
		public VertexInputStageBuilder build() {
			// Validate binding description
			if(bindings.containsKey(binding)) throw new IllegalArgumentException("Duplicate binding index: " + binding);
			if(stride == 0) throw new IllegalArgumentException("Invalid vertex stride");

			// Add binding
			bindings.put(binding, this);

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

		private void populate(VkVertexInputAttributeDescription attr) {
			attr.binding = binding;
			attr.location = loc;
			attr.format = format;
			attr.offset = offset;
		}

		/**
		 * Constructs this attribute description.
		 * @throws IllegalArgumentException for a duplicate location, if the attribute format is not specified, the binding is not present, or the offset exceeds the vertex stride
		 */
		public VertexInputStageBuilder build() {
			// Validate attribute
			final BindingBuilder desc = bindings.get(binding);
			if(desc == null) throw new IllegalArgumentException("Invalid binding index for attribute: " + binding);
			if(offset >= desc.stride) throw new IllegalArgumentException("Offset exceeds vertex stride");
			if(format == null) throw new IllegalArgumentException("No format specified for attribute");

			// Check location
			if(desc.locations.contains(loc)) throw new IllegalArgumentException("Duplicate location: " + loc);
			desc.locations.add(loc);

			// Add attribute
			attributes.add(this);

			return VertexInputStageBuilder.this;
		}
	}
}
