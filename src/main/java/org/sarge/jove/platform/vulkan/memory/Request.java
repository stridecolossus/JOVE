package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>memory request</i> specifies application requirements for memory allocated by Vulkan and is used to select an appropriate memory type.
 * <p>
 * The request is comprised of:
 * <ul>
 * <li>the <i>size</i> of the requested memory</li>
 * <li>a bit-mask <i>filter</i> specifying the supported memory type(s) for this request (by index)</li>
 * <li>an <i>optimal</i> and a <i>required</i> set of memory properties</li>
 * </ul>
 * <p>
 * In general a consumer of this request will allocate memory that matches the <i>optimal</i> memory properties falling back to the <i>required</i> set as necessary.
 * <br>
 * Note that this implementation does not apply any assumptions or constraints on the relationship between the optimal and required memory property sets.
 * <p>
 * Example:
 * <pre>
 *  // Specify host memory of types 0 or 1 and optimally a coherent heap
 *  Request req = new Request.Builder()
 *  	.size(42)
 *  	.filter(0b11)
 *  	.optimal(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
 *  	.build()
 *
 *  // Select matching memory type for this request
 *  Collection<MemoryType> types = ...
 *  MemoryType selected = req.select(types);
 * </pre>
 * <p>
 * The builder also provides the convenience {@link Builder#init(VkMemoryRequirements)} mutator to configure a request given memory requirements returned from Vulkan.
 * <p>
 * @see MemoryType
 * @author Sarge
 */
public record Request(long size, int filter, Set<VkMemoryPropertyFlag> required, Set<VkMemoryPropertyFlag> optimal) {
	/**
	 * Constructor.
	 * @param size			Size of the requested memory (bytes)
	 * @param filter		Memory type filter mask
	 * @param required		Required memory properties
	 * @param optimal		Optimal properties
	 * @throws IllegalArgumentException if the filter is zero
	 */
	public Request(long size, int filter, Set<VkMemoryPropertyFlag> required, Set<VkMemoryPropertyFlag> optimal) {
		if(filter == 0) throw new IllegalArgumentException("Filter cannot be zero");
		this.size = oneOrMore(size);
		this.filter = filter;
		this.required = Set.copyOf(required);
		this.optimal = Set.copyOf(optimal);
	}

	/**
	 * Selects the memory type matching for this request from the given list.
	 * @param types Available memory types
	 * @return Selected memory type
	 */
	public Optional<MemoryType> select(Collection<MemoryType> types) {
		// Filter available memory types
		final var candidates = types
				.stream()
				.filter(type -> MathsUtil.isBit(filter, type.index()))
				.collect(toList());

		// Stop if all filtered out
		if(candidates.isEmpty()) {
			return Optional.empty();
		}

		// Find matching memory type
		return find(candidates, optimal).or(() -> find(candidates, required));
	}

	/**
	 * Finds a memory type with the given properties.
	 */
	private static Optional<MemoryType> find(List<MemoryType> types, Set<VkMemoryPropertyFlag> props) {
		return types
				.stream()
				.filter(type -> type.properties().containsAll(props))
				.findAny();
	}

	/**
	 * Builder for a memory request.
	 */
	public static class Builder {
		private long size;
		private int filter = Integer.MAX_VALUE;
		private final Set<VkMemoryPropertyFlag> required = new HashSet<>();
		private final Set<VkMemoryPropertyFlag> optimal = new HashSet<>();

		/**
		 * Sets the required size of the memory.
		 * @param size Required memory size (bytes)
		 */
		public Builder size(long size) {
			this.size = oneOrMore(size);
			return this;
		}

		/**
		 * Sets the memory type filter bit-mask.
		 * @param filter Memory type filter mask
		 */
		public Builder filter(int filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Convenience method to initialise this allocation to the given memory requirements descriptor.
		 * @param reqs Memory requirements
		 */
		public Builder init(VkMemoryRequirements reqs) {
			size(reqs.size);
			filter(reqs.memoryTypeBits);
			// TODO - alignment
			return this;
		}

		/**
		 * Adds a <i>required</i> memory property.
		 * @param flag Required memory property
		 */
		public Builder required(VkMemoryPropertyFlag flag) {
			required.add(notNull(flag));
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param flag Optimal memory property
		 */
		public Builder optimal(VkMemoryPropertyFlag flag) {
			optimal.add(notNull(flag));
			return this;
		}

		/**
		 * Constructs this memory request.
		 * @return New memory request
		 */
		public Request build() {
			return new Request(size, filter, required, optimal);
		}
	}
}
