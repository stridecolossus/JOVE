package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.util.Check;

/**
 * A set of <i>memory properties</i> specifies the purpose and requirements of a memory request.
 * <p>
 * In general the client requests memory for the <i>optimal</i> memory properties falling back to the <i>required</i> set as necessary.
 * <br>
 * Note that this implementation does not apply any assumptions or constraints on the relationship between the optimal and required memory properties.
 * <p>
 * Example for the properties of an image:
 * <pre>
 * var props = new MemoryProperties.Builder&lt;VkImageUsageFlag&gt;()
 *     .usage(VkImageUsageFlag.COLOR_ATTACHMENT)
 *     .mode(VkSharingMode.CONCURRENT)
 *     .optimal(VkMemoryProperty.HOST_COHERENT)
 *     .build()</pre>
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
		 * @param prop Required memory property
		 */
		public Builder<T> required(VkMemoryProperty prop) {
			required.add(notNull(prop));
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param prop Optimal memory property
		 */
		public Builder<T> optimal(VkMemoryProperty prop) {
			optimal.add(notNull(prop));
			return this;
		}

		/**
		 * Convenience helper to initialise the <i>optimal</i> properties to the configured <i>required</i> set.
		 * @throws IllegalStateException if the required properties are empty
		 */
		public Builder<T> copy() {
			if(required.isEmpty()) throw new IllegalStateException("No required properties specified");
			optimal.addAll(required);
			return this;
		}

		/**
		 * Adds a usage flag for this memory.
		 * @param usage Memory usage flag
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
		 * Constructs this memory properties instance.
		 * @return New memory properties
		 */
		public MemoryProperties<T> build() {
			return new MemoryProperties<>(usage, mode, required, optimal);
		}
	}
}
