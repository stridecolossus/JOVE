package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import static org.sarge.lib.Validation.*;

/**
 * A set of <i>memory properties</i> specifies the purpose and requirements of a memory request.
 * <p>
 * In general the client requests <i>required</i> and <i>optimal</i> properties for the memory, with the allocator falling back to the minimal properties as required.
 * <p>
 * Example for the properties of a uniform buffer visible to the application and ideally GPU resident:
 * <pre>
 * var props = new MemoryProperties.Builder&lt;VkBufferUsageFlag&gt;()
 *     .usage(VkBufferUsageFlag.UNIFORM_BUFFER)
 *     .mode(VkSharingMode.CONCURRENT)
 *     .required(VkMemoryProperty.HOST_COHERENT)
 *     .required(VkMemoryProperty.HOST_VISIBLE)
 *     .optimal(VkMemoryProperty.DEVICE_LOCAL)
 *     .build()</pre>
 * <p>
 * @param <T> Usage enumeration
 * @see VkBufferUsageFlag
 * @see VkImageUsageFlag
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
		requireNonNull(mode);
		requireNotEmpty(usage);
		usage = Set.copyOf(usage);
		required = Set.copyOf(required);
		optimal = Set.copyOf(optimal);
	}

	/**
	 * Convenience constructor for basic memory properties with the given usage.
	 * @param usage Memory usage
	 */
	public MemoryProperties(T usage) {
		this(Set.of(usage), VkSharingMode.EXCLUSIVE, Set.of(), Set.of());
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
			required.add(requireNonNull(prop));
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param prop Optimal memory property
		 */
		public Builder<T> optimal(VkMemoryProperty prop) {
			optimal.add(requireNonNull(prop));
			return this;
		}

		/**
		 * Adds a usage flag for this memory.
		 * @param usage Memory usage flag
		 */
		public Builder<T> usage(T usage) {
			this.usage.add(requireNonNull(usage));
			return this;
		}

		/**
		 * Sets the sharing mode for this memory (default is {@link VkSharingMode#EXCLUSIVE}).
		 * @param mode Sharing mode
		 */
		public Builder<T> mode(VkSharingMode mode) {
			this.mode = requireNonNull(mode);
			return this;
		}

		/**
		 * Constructs this memory properties instance.
		 * @return New memory properties
		 * @see MemoryProperties#MemoryProperties(Set, VkSharingMode, Set, Set)
		 */
		public MemoryProperties<T> build() {
			return new MemoryProperties<>(usage, mode, required, optimal);
		}
	}
}
