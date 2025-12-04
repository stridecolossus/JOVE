package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;

/**
 * A set of <i>memory properties</i> specifies the purpose and requirements of a memory request.
 * <p>
 * In general the client requests <i>required</i> and <i>optimal</i> properties for the memory, with the allocator falling back to the minimal properties as required.
 * <p>
 * Example for the properties of a uniform buffer visible to the application and ideally GPU resident:
 * <pre>
 * var properties = new MemoryProperties.Builder&lt;VkBufferUsageFlag&gt;()
 *     .usage(VkBufferUsageFlag.UNIFORM_BUFFER)
 *     .mode(VkSharingMode.CONCURRENT)
 *     .required(VkMemoryPropertyFlags.HOST_COHERENT)
 *     .required(VkMemoryPropertyFlags.HOST_VISIBLE)
 *     .optimal(VkMemoryPropertyFlags.DEVICE_LOCAL)
 *     .build()</pre>
 * <p>
 * @param <T> Usage enumeration
 * @see VkBufferUsageFlag
 * @see VkImageUsageFlag
 * @author Sarge
 */
public record MemoryProperties<T>(Set<T> usage, VkSharingMode mode, Set<VkMemoryPropertyFlags> required, Set<VkMemoryPropertyFlags> optimal) {
	/**
	 * Constructor.
	 * @param usage			Memory usage(s)
	 * @param mode			Sharing mode
	 * @param required		Required memory properties
	 * @param optimal		Optimal properties
	 * @throws IllegalArgumentException if {@link #usage} is empty
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
		private final Set<VkMemoryPropertyFlags> required = new HashSet<>();
		private final Set<VkMemoryPropertyFlags> optimal = new HashSet<>();
		private final Set<T> usage = new HashSet<>();
		private VkSharingMode mode = VkSharingMode.EXCLUSIVE;

		/**
		 * Adds a <i>required</i> memory property.
		 * @param property Required memory property
		 */
		public Builder<T> required(VkMemoryPropertyFlags property) {
			required.add(property);
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param property Optimal memory property
		 */
		public Builder<T> optimal(VkMemoryPropertyFlags property) {
			optimal.add(property);
			return this;
		}

		/**
		 * Adds a usage flag for this memory.
		 * @param usage Memory usage flag
		 */
		public Builder<T> usage(T usage) {
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the sharing mode for this memory.
		 * The default value is {@link VkSharingMode#EXCLUSIVE}.
		 * @param mode Sharing mode
		 */
		public Builder<T> mode(VkSharingMode mode) {
			this.mode = mode;
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
