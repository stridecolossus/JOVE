package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>memory properties</i> is a descriptor for the usage and requirements of a memory request.
 * <p>
 * In general a consumer of this request will allocate memory that matches the <i>optimal</i> memory properties falling back to the <i>required</i> set as necessary.
 * <br>
 * Note that this implementation does not apply any assumptions or constraints on the relationship between the optimal and required memory property sets.
 * <p>
 * Example for the properties of an image:
 * <pre>
 *  // Specify properties for this image
 *  MemoryProperties&lt;VkImageUsageFlag&gt; props = new MemoryProperties.Builder()
 *  	.usage(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
 *  	.mode(VkSharingMode.VK_SHARING_MODE_CONCURRENT)
 *  	.optimal(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
 *  	.build()
 *
 *  // Retrieve image memory requirements from Vulkan
 *  VkMemoryRequirements reqs = ...
 *
 *  // Select matching memory type
 *  Collection&lt;MemoryType&gt; types = ...
 *  MemoryType selected = req.select(types, reqs);
 * </pre>
 * <p>
 * @param <T> Usage enumeration
 * @author Sarge
 */
public record MemoryProperties<T>(Set<T> usage, VkSharingMode mode, Set<VkMemoryPropertyFlag> required, Set<VkMemoryPropertyFlag> optimal) {
	/**
	 * Constructor.
	 * @param usage			Memory usage(s)
	 * @param mode			Sharing mode
	 * @param required		Required memory properties
	 * @param optimal		Optimal properties
	 * @throws IllegalArgumentException if the memory {@link #usage} flags are empty
	 */
	public MemoryProperties(Set<T> usage, VkSharingMode mode, Set<VkMemoryPropertyFlag> required, Set<VkMemoryPropertyFlag> optimal) {
		if(usage.isEmpty()) throw new IllegalArgumentException("At least one memory usage flags must be specified");
		this.usage = Set.copyOf(usage);
		this.mode = notNull(mode);
		this.required = Set.copyOf(required);
		this.optimal = Set.copyOf(optimal);
	}

	/**
	 * Selects the memory type matching for this request from the given list.
	 * @param filter		Memory types filter
	 * @param types 		Available memory types
	 * @return Selected memory type
	 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>
	 */
	public Optional<MemoryType> select(int filter, Collection<MemoryType> types) {
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
	 *
	 * TODO
	 * - should select() be in a separate Selector?
	 * - would we ever want a different strategy
	 * - combined with Allocator in device?
	 *
	 */

	/**
	 * Builder for memory properties.
	 * @param <T> Usage enumeration
	 */
	public static class Builder<T> {
		private final Set<VkMemoryPropertyFlag> required = new HashSet<>();
		private final Set<VkMemoryPropertyFlag> optimal = new HashSet<>();
		private final Set<T> usage = new HashSet<>();
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;

		/**
		 * Adds a <i>required</i> memory property.
		 * @param flag Required memory property
		 */
		public Builder<T> required(VkMemoryPropertyFlag flag) {
			required.add(notNull(flag));
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param flag Optimal memory property
		 */
		public Builder<T> optimal(VkMemoryPropertyFlag flag) {
			optimal.add(notNull(flag));
			return this;
		}

		/**
		 * Adds a usage for this memory.
		 * @param usage Memory usage
		 */
		public Builder<T> usage(T usage) {
			this.usage.add(notNull(usage));
			return this;
		}

		/**
		 * Sets the sharing mode for this memory (default is {@link VkSharingMode#VK_SHARING_MODE_EXCLUSIVE}).
		 * @param mode Sharing mode
		 */
		public Builder<T> mode(VkSharingMode mode) {
			this.mode = notNull(mode);
			return this;
		}

		/**
		 * Constructs this memory request.
		 * @return New memory request
		 */
		public MemoryProperties<T> build() {
			return new MemoryProperties<>(usage, mode, required, optimal);
		}
	}
}
