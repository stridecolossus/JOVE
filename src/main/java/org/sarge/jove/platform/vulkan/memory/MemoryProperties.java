package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.util.Check;

/**
 * A set of <i>memory properties</i> specifies the purpose and requirements of a memory request.
 * <p>
 * In general the client requests memory for the <i>optimal</i> memory properties falling back to the <i>required</i> set as necessary.
 * Note that this implementation assumes that <i>optimal</i> is a super-set of the <i>required</i> properties.
 * <p>
 * Example for the properties of a uniform buffer which would ideally be GPU resident but also visible to the application:
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
		if(usage.isEmpty()) throw new IllegalArgumentException("At least one memory usage must be specified");
		usage = Set.copyOf(usage);
		mode = Check.notNull(mode);
		required = Set.copyOf(required);
		optimal = Set.copyOf(CollectionUtils.union(required, optimal));
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
