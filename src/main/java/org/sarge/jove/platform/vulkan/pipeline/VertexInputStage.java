package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.FormatBuilder;

/**
 * Builder for the vertex input pipeline stage descriptor.
 * TODO - usage, auto allocation, implications on shader
 * @author Sarge
 */
public class VertexInputStage {
	private final Map<Integer, VertexBinding> bindings = new HashMap<>();

	/**
	 * Starts a new binding.
	 * @return New binding
	 */
	public VertexBinding binding() {
		return new VertexBinding();
	}

	/**
	 * Allocates the next available slot in the given integer map.
	 * @param map Integer map
	 * @return Next slot
	 */
	private static int allocate(Map<Integer, ?> map) {
		return map
				.keySet()
				.stream()
				.mapToInt(Integer::intValue)
				.max()
				.orElse(0);
	}

	/**
	 * A vertex input <i>binding</i> specifies a group of vertex attributes.
	 */
	public class VertexBinding {
    	private int index = allocate(bindings);
    	private int stride;
    	private VkVertexInputRate rate = VkVertexInputRate.VERTEX;
    	private final Map<Integer, VertexAttribute> attributes = new HashMap<>();

		private VertexBinding() {
		}

		/**
		 * Sets the index of this binding.
		 * The binding index is initialised to the next available slot if not specified.
		 * @param index Binding index
		 * @throws IllegalStateException if the {@link #index} index has already been allocated
		 */
		public VertexBinding binding(int index) {
			if(bindings.containsKey(index)) throw new IllegalStateException("Duplicate binding index: " + index);
			this.index = requireZeroOrMore(index);
			return this;
		}

		/**
		 * Sets the vertex input rate.
		 * Default is {@link VkVertexInputRate#VERTEX}.
		 * @param rate Vertex rate
		 */
		public VertexBinding rate(VkVertexInputRate rate) {
			this.rate = requireNonNull(rate);
			return this;
		}

		/**
		 * Sets the <i>stride</i> of this binding, i.e. the number of bytes per binding.
		 * @param stride Binding stride (bytes)
		 */
		public VertexBinding stride(int stride) {
			this.stride = requireOneOrMore(stride);
			return this;
		}

		/**
		 * Starts a new vertex attribute for this binding.
		 * @return New vertex attribute
		 */
    	public VertexAttribute attribute() {
    		return new VertexAttribute(this);
    	}

    	/**
    	 * Helper - Adds a new vertex attribute with the given format and other properties set to appropriate defaults.
    	 * @param format Vertex format
    	 */
    	public VertexBinding attribute(VkFormat format) {
    		final var attribute = new VertexAttribute(this);
    		attribute.format(format);
    		attribute.build();
    		return this;
    	}

    	/**
    	 * Helper - Adds a vertex attribute derived from the given layout.
		 * The binding index is initialised to the next available slot.
		 * The vertex stride of this binding is incremented accordingly.
    	 * @param layout Vertex layout
    	 */
    	public VertexBinding attribute(Layout layout) {
    		// Configure vertex attribute for this layout
    		final var attribute = new VertexAttribute(this)
    				.format(FormatBuilder.format(layout))
    				.offset(stride);

    		// Increment total stride for this binding accordingly
    		stride += layout.stride();

    		// Attach attribute
    		attribute.build();

    		return this;
    	}

    	/**
    	 * @throws IndexOutOfBoundsException if any attribute offset exceeds the vertex stride of this binding
    	 */
    	private void validate() {
			for(VertexAttribute attribute : attributes.values()) {
				validate(attribute);
			}
    	}

    	/**
    	 * @throws IndexOutOfBoundsException if the attribute offset exceeds the vertex stride of this binding
    	 */
    	private void validate(VertexAttribute attribute) {
			if(attribute.offset >= stride) {
				throw new IndexOutOfBoundsException("Attribute offset exceeds stride: attribute=%s binding=%s".formatted(attribute, this));
			}
    	}

    	/**
    	 * Constructs this binding.
    	 * @throws IllegalStateException if the stride of this binding has not been populated
    	 * @throws IndexOutOfBoundsException if any attribute offset exceeds the vertex stride of this binding
    	 */
    	public VertexInputStage build() {
    		if(stride == 0) throw new IllegalStateException("Binding stride cannot be zero: " + this);
    		validate();
    		bindings.put(index, this);
    		return VertexInputStage.this;
    	}

    	/**
    	 * @return Descriptor for this binding
    	 */
    	private VkVertexInputBindingDescription populate() {
    		final var descriptor = new VkVertexInputBindingDescription();
    		descriptor.binding = index;
    		descriptor.stride = stride;
    		descriptor.inputRate = rate;
    		return descriptor;
    	}

    	@Override
    	public String toString() {
    		return String.format("Binding[binding=%d stride=%s rate=%s]", index, String.valueOf(stride), rate);
    	}
	}

	/**
	 * A vertex <i>attribute</i> describes the layout of a component of a vertex buffer.
	 */
	public class VertexAttribute {
		private final VertexBinding binding;
		private int location;
		private VkFormat format;
		private int offset;

		/**
		 * Constructor.
		 * @param binding Parent binding
		 */
		private VertexAttribute(VertexBinding binding) {
			this.binding = binding;
			this.location = allocate(binding.attributes);
		}

		/**
		 * Sets the location of this vertex attribute.
		 * If unspecified the location is automatically allocated to the next contiguous slot within this binding.
		 * @param location Attribute location
		 * @throws IllegalStateException if {@link #location} has already been allocated in this binding
		 */
		public VertexAttribute location(int location) {
			if(binding.attributes.containsKey(location)) throw new IllegalStateException();
			this.location = requireZeroOrMore(location);
			return this;
		}

		/**
		 * Sets the format of this vertex attribute.
		 * @param format Vertex format
		 */
		public VertexAttribute format(VkFormat format) {
			this.format = requireNonNull(format);
			return this;
		}

		/**
		 * Sets the offset of this vertex attribute.
		 * @param offset Offset (bytes)
		 */
		public VertexAttribute offset(int offset) {
			this.offset = requireZeroOrMore(offset);
			return this;
		}

		/**
		 * Constructs this vertex attribute.
    	 * @throws IndexOutOfBoundsException if the attribute offset exceeds the vertex stride of this binding
		 */
		public VertexBinding build() {
			binding.validate(this);
			binding.attributes.put(location, this);
			return binding;
		}

		/**
    	 * @return Descriptor for this vertex attribute
    	 */
    	private VkVertexInputAttributeDescription populate() {
    		final var descriptor = new VkVertexInputAttributeDescription();
    		descriptor.binding = binding.index;
    		descriptor.location = location;
    		descriptor.format = format;
    		descriptor.offset = offset;
    		return descriptor;
    	}

    	@Override
    	public String toString() {
    		return String.format("loc=%d format=%s offset=%d", location, format, offset);
    	}
	}

	/**
	 * @return Vertex input stage descriptor
	 * @throws IllegalStateException if any binding does not contain at least one vertex attribute
   	 * @throws IndexOutOfBoundsException if any attribute offset exceeds the vertex stride of its binding
	 */
	VkPipelineVertexInputStateCreateInfo descriptor() {
		// Validate bindings
		for(VertexBinding b : bindings.values()) {
			if(b.attributes.isEmpty()) {
				throw new IllegalStateException("Binding cannot be empty: " + b);
			}
			b.validate();
		}

		// Init stage descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();
		info.flags = 0;

		// Populate bindings
		info.vertexBindingDescriptionCount = bindings.size();
		info.pVertexBindingDescriptions = bindings.values().stream().map(VertexBinding::populate).toArray(VkVertexInputBindingDescription[]::new);

		// Aggregate vertex attributes from the bindings
		final var attributes = bindings
				.values()
				.stream()
				.flatMap(e -> e.attributes.values().stream())
				.map(VertexAttribute::populate)
				.toArray(VkVertexInputAttributeDescription[]::new);

		// Populate vertex attributes
		if(attributes.length > 0) {
    		info.vertexAttributeDescriptionCount = attributes.length;
    		info.pVertexAttributeDescriptions = attributes;
		}
		else {
			assert bindings.isEmpty();
		}

		return info;
	}
}
