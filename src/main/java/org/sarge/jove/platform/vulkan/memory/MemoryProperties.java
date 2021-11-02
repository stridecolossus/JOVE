package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.lib.util.Check;

/**
 * A <i>memory properties</i> specifies the usage and requirements of a memory request.
 * <p>
 * In general a consumer will allocate memory that matches the <i>optimal</i> memory properties falling back to the <i>required</i> set as necessary.
 * <br>
 * Note that this implementation does not apply any assumptions or constraints on the relationship between the optimal and required memory property sets.
 * <p>
 * Example for the properties of an image:
 * <pre>
 *  MemoryProperties&lt;VkImageUsageFlag&gt; props = new MemoryProperties.Builder&lt;&gt;()
 *  	.usage(VkImageUsage.COLOR_ATTACHMENT)
 *  	.mode(VkSharingMode.CONCURRENT)
 *  	.optimal(VkMemoryProperty.HOST_COHERENT)
 *  	.build()
 * </pre>
 * <p>
 * @param <T> Usage enumeration
 * @author Sarge
 */
public record MemoryProperties<T>(Set<T> usage, VkSharingMode mode, Set<VkMemoryProperty> required, Set<VkMemoryProperty> optimal) {
	/**
	 * Constructor.
	 * @param usage			Memory usage(s)
	 * @param mode			Sharing mode
	 * @param required		Required memory properties
	 * @param optimal		Optimal properties
	 * @throws IllegalArgumentException if the memory {@link #usage} flags are empty
	 */
	public MemoryProperties {
		if(usage.isEmpty()) throw new IllegalArgumentException("At least one memory usage must be specified");
		usage = Set.copyOf(usage);
		mode = Check.notNull(mode);
		required = Set.copyOf(required);
		optimal = Set.copyOf(optimal);
	}

	/**
	 * Builder for memory properties.
	 * @param <T> Usage enumeration
	 */
	public static class Builder<T> {
		private final Set<VkMemoryProperty> required = new HashSet<>();
		private final Set<VkMemoryProperty> optimal = new HashSet<>();
		private final Set<T> usage = new HashSet<>();
		private VkSharingMode mode = VkSharingMode.EXCLUSIVE;

		/**
		 * Adds a <i>required</i> memory property.
		 * @param flag Required memory property
		 */
		public Builder<T> required(VkMemoryProperty flag) {
			required.add(notNull(flag));
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param flag Optimal memory property
		 */
		public Builder<T> optimal(VkMemoryProperty flag) {
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
		 * Sets the sharing mode for this memory (default is {@link VkSharingMode#EXCLUSIVE}).
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
