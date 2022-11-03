package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.StructureCollector;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;

/**
 * Builder for the vertex input pipeline stage descriptor.
 * @see VkPipelineVertexInputStateCreateInfo
 * @author Sarge
 */
public class VertexInputPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineVertexInputStateCreateInfo> {
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
	 * Helper - Adds a vertex input binding and attributes for the given vertex layout.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The binding index is allocated as the next available index</li>
	 * <li>Vertex data is assumed to be contiguous, i.e. the offset of each component is the end of the previous element</li>
	 * </ul>
	 * <p>
	 * @param layouts Vertex layout
	 */
	public VertexInputPipelineStageBuilder add(Layout layout) {
		// Add binding
		final var binding = new BindingBuilder();

		// Init vertex stride
		final int stride = layout.stride();
		binding.stride(stride);

		// Add attribute for each layout component
		int offset = 0;
		for(Component c : layout.components()) {
			// Determine component format
			final VkFormat format = FormatBuilder.format(c);

			// Add attribute for component
			new AttributeBuilder(binding)
					.format(format)
					.offset(offset)
					.build();

			// Increment offset to the start of the next attribute
			offset += c.stride();
		}
		assert offset == stride;

		// Construct binding
		return binding.build();
	}

	/**
	 * Constructs this vertex input stage.
	 * @return New vertex input stage
	 * @throws IllegalArgumentException for any binding without attributes
	 */
	@Override
	VkPipelineVertexInputStateCreateInfo get() {
		// Create descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();

		// Add binding descriptions
		info.vertexBindingDescriptionCount = bindings.size();
		info.pVertexBindingDescriptions = StructureCollector.pointer(bindings.values(), new VkVertexInputBindingDescription(), BindingBuilder::populate);

		// Add attributes
		info.vertexAttributeDescriptionCount = attributes.size();
		info.pVertexAttributeDescriptions = StructureCollector.pointer(attributes, new VkVertexInputAttributeDescription(), AttributeBuilder::populate);

		return info;
	}

	/**
	 * Builder for a vertex input binding.
	 */
	public class BindingBuilder {
		private int index = bindings.size();
		private int stride;
		private VkVertexInputRate rate = VkVertexInputRate.VERTEX;
		private final Set<Integer> locations = new HashSet<>();

		private BindingBuilder() {
		}

		/**
		 * Sets the binding index.
		 * @param binding Binding index
		 */
		public BindingBuilder index(int binding) {
			this.index = zeroOrMore(binding);
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
		 * @param rate Input rate (default is {@link VkVertexInputRate#VERTEX})
		 */
		public BindingBuilder rate(VkVertexInputRate rate) {
			this.rate = notNull(rate);
			return this;
		}

		/**
		 * Starts a new attribute.
		 * @return New attribute builder
		 */
		public AttributeBuilder attribute() {
			return new AttributeBuilder(this);
		}

		private void populate(VkVertexInputBindingDescription desc) {
			desc.binding = index;
			desc.stride = stride;
			desc.inputRate = rate;
		}

		/**
		 * Constructs this input binding.
		 * @throws IllegalArgumentException for a duplicate binding index or if no vertex attributes are specified
		 */
		public VertexInputPipelineStageBuilder build() {
			// Validate binding description
			if(bindings.containsKey(index)) throw new IllegalArgumentException("Duplicate binding index: " + index);
			if(locations.isEmpty()) throw new IllegalArgumentException(String.format("No attributes specified for binding: ", index));

			// Add binding
			bindings.put(index, this);

			return VertexInputPipelineStageBuilder.this;
		}
	}

	/**
	 * Builder for a vertex input attribute.
	 */
	public class AttributeBuilder {
		private final BindingBuilder binding;
		private int loc;
		private VkFormat format;
		private int offset;

		private AttributeBuilder(BindingBuilder binding) {
			this.binding = binding;
			this.loc = binding.locations.size();
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
			attr.binding = binding.index;
			attr.location = loc;
			attr.format = format;
			attr.offset = offset;
		}

		/**
		 * Constructs this attribute description.
		 * @return Parent binding builder
		 * @throws IllegalArgumentException if the location is a duplicate, the attribute format is not specified, or the offset exceeds the vertex stride
		 */
		public BindingBuilder build() {
			// Validate attribute
			if(offset >= binding.stride) throw new IllegalArgumentException("Offset exceeds vertex stride");
			if(format == null) throw new IllegalArgumentException("No format specified for attribute");

			// Check location
			if(binding.locations.contains(loc)) throw new IllegalArgumentException("Duplicate location: " + loc);
			binding.locations.add(loc);

			// Add attribute
			attributes.add(this);

			return binding;
		}
	}
}
