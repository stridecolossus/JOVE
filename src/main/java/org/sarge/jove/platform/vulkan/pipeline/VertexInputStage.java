package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.FormatBuilder;

/**
 * The <i>vertex input stage</i> specifies the configuration of the vertex input fixed function of the pipeline.
 * @author Sarge
 */
public class VertexInputStage {
	private final Map<Integer, VertexBinding> bindings = new HashMap<>();
	private final Set<Integer> locations = new HashSet<>();

	/**
	 * A <i>vertex attribute</i> specifies the position and format of a vertex component.
	 */
	public record VertexAttribute(int location, VkFormat format, int offset) {
		/**
		 * Constructor.
		 * @param location		Shader location
		 * @param format		Format of this attribute
		 * @param offset		Offset within the vertex data
		 */
		public VertexAttribute {
			requireZeroOrMore(location);
			requireNonNull(format);
			requireZeroOrMore(offset);
		}

		/**
		 * @param binding Binding index
		 * @return Vertex attribute descriptor
		 */
		private VkVertexInputAttributeDescription populate(int binding) {
			final var attribute = new VkVertexInputAttributeDescription();
			attribute.binding = binding;
			attribute.location = location;
			attribute.format = format;
			attribute.offset = offset;
			return attribute;
		}
	}

	/**
	 * A <i>vertex binding</i> specifies the vertex attributes used in a shader.
	 */
	public record VertexBinding(int index, int stride, VkVertexInputRate rate, List<VertexAttribute> attributes) {
		/**
		 * Constructor.
		 * @param index				Binding index
		 * @param stride			Stride (bytes)
		 * @param rate				Input rate
		 * @param attributes		Vertex attributes
		 * @throws IllegalArgumentException if the offset of any attribute is not less than the {@link #stride} of this binding
		 */
		public VertexBinding {
			requireZeroOrMore(index);
			requireZeroOrMore(stride);
			requireNonNull(rate);
			attributes = List.copyOf(attributes);

			final int offset = offset(attributes);
			if(offset >= stride) {
				throw new IllegalArgumentException("Invalid vertex attribute offset %d for stride %d".formatted(offset, stride));
			}
		}

		/**
		 * @return Maximum offset of the given attributes
		 */
		private static int offset(List<VertexAttribute> attributes) {
			return attributes
					.stream()
					.mapToInt(VertexAttribute::offset)
					.max()
					.orElse(0);
		}

		/**
		 * Helper.
		 * Creates a vertex binding from the given layouts.
		 * TODO - contiguous, RGBA
		 * @param index			Binding index
		 * @param start			Starting attribute location
		 * @param layouts		Vertex layouts
		 * @return Vertex binding
		 */
		public static VertexBinding of(int index, int start, List<Layout> layouts) {
			// Init attribute location and stride
			int loc = requireZeroOrMore(start);
			int stride = 0;

			// Create a vertex attribute for each layout
			final var builder = new FormatBuilder();
			final List<VertexAttribute> attributes = new ArrayList<>();
			for(Layout layout : layouts) {
				final VkFormat format = builder.init(layout).build();
				final var attribute = new VertexAttribute(loc, format, stride);
				attributes.add(attribute);
				++loc;
				stride += layout.stride();
			}

			// Create binding
			return new VertexBinding(index, stride, VkVertexInputRate.VERTEX, attributes);
		}

		/**
		 * @return Binding descriptor
		 */
		private VkVertexInputBindingDescription populate() {
			final var description = new VkVertexInputBindingDescription();
			description.binding = index;
			description.stride = stride;
			description.inputRate = rate;
			return description;
		}

		/**
		 * @return Attribute descriptors
		 */
		private Stream<VkVertexInputAttributeDescription> stream() {
			return attributes
					.stream()
					.map(attribute -> attribute.populate(index));
		}

		/**
		 * Builder for a vertex binding.
		 */
		public static class Builder {
			private int index;
			private int stride;
			private VkVertexInputRate rate = VkVertexInputRate.VERTEX;
			private final List<VertexAttribute> attributes = new ArrayList<>();

			public Builder index(int index) {
				this.index = index;
				return this;
			}

			public Builder stride(int stride) {
				this.stride = stride;
				return this;
			}

			public Builder rate(VkVertexInputRate rate) {
				this.rate = rate;
				return this;
			}

			public Builder attribute(VertexAttribute attribute) {
				attributes.add(attribute);
				return this;
			}

			public VertexBinding build() {
				return new VertexBinding(index, stride, rate, attributes);
			}
		}
	}

	/**
	 * Adds a vertex binding.
	 * @param binding Binding to add
	 * @throws IllegalArgumentException for a duplicate binding index
	 * @throws IllegalArgumentException for a duplicate vertex attribute location
	 */
	public void add(VertexBinding binding) {
		if(bindings.containsKey(binding.index)) {
			throw new IllegalArgumentException("Duplicate binding index: " + binding);
		}

		for(var attribute : binding.attributes) {
			if(locations.contains(attribute.location)) {
				throw new IllegalArgumentException("Duplicate location for attribute %s in binding %s".formatted(attribute, binding));
			}
			locations.add(attribute.location);
		}

		bindings.put(binding.index, binding);
	}

	/**
	 * @return Vertex input stage descriptor
	 * @throws IllegalStateException if any binding does not contain at least one vertex attribute
  	 * @throws IndexOutOfBoundsException if any attribute offset exceeds the vertex stride of its binding
	 */
	VkPipelineVertexInputStateCreateInfo descriptor() {
		// Init stage descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();
		info.flags = 0;

		// Add vertex bindings
		info.vertexBindingDescriptionCount = bindings.size();
		info.pVertexBindingDescriptions = bindings
				.values()
				.stream()
				.map(VertexBinding::populate)
				.toArray(VkVertexInputBindingDescription[]::new);

		// Aggregate vertex attributes across all bindings
		final var attributes = bindings
				.values()
				.stream()
				.flatMap(VertexBinding::stream)
				.toArray(VkVertexInputAttributeDescription[]::new);

		// Add vertex attributes
		info.vertexAttributeDescriptionCount = attributes.length;
		info.pVertexAttributeDescriptions = attributes;

		return info;
	}
}
