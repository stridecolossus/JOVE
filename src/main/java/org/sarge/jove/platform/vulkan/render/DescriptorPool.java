package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
 * @author Sarge
 */
public class DescriptorPool extends AbstractVulkanObject {
	private final int max;

	/**
	 * Constructor.
	 * @param handle		Pool handle
	 * @param dev			Logical device
	 * @param max			Maximum number of descriptor sets that can be allocated from this pool
	 */
	DescriptorPool(Handle handle, DeviceContext dev, int max) {
		super(handle, dev);
		this.max = oneOrMore(max);
	}

	/**
	 * @return Maximum number of sets that can be allocated from this pool
	 */
	public int maximum() {
		return max;
	}

	/**
	 * Allocates a number of descriptor sets with the given layout(s).
	 * @param layouts Layout for each set
	 * @return New descriptor sets
	 */
	public Collection<DescriptorSet> allocate(List<DescriptorLayout> layouts) {
		Check.notEmpty(layouts);
		final int count = layouts.size();

		// Build allocation descriptor
		final var info = new VkDescriptorSetAllocateInfo();
		info.descriptorPool = this.handle();
		info.descriptorSetCount = count;
		info.pSetLayouts = NativeObject.array(layouts);

		// Allocate descriptors sets
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final Pointer[] pointers = new Pointer[count];
		check(lib.vkAllocateDescriptorSets(dev, info, pointers));

		// Create descriptor sets
		return IntStream
				.range(0, count)
				.mapToObj(n -> new DescriptorSet(new Handle(pointers[n]), layouts.get(n)))
				.toList();
	}

	/**
	 * @see #allocate(List)
	 */
	public Collection<DescriptorSet> allocate(DescriptorLayout... layouts) {
		return allocate(Arrays.asList(layouts));
	}

	/**
	 * Convenience method to allocate a number of descriptor sets each with the given layout.
	 * @param count			Number of sets to allocate
	 * @param layout		Descriptor layout
	 * @return New descriptor sets
	 */
	public Collection<DescriptorSet> allocate(int count, DescriptorLayout layout) {
		return allocate(Collections.nCopies(count, layout));
	}

	/**
	 * Releases the given sets back to this pool.
	 * @param sets Sets to release
	 */
	public void free(Collection<DescriptorSet> sets) {
		final DeviceContext dev = this.device();
		check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.array(sets)));
	}

	/**
	 * Resets this pool and releases <b>all</b> allocated descriptor sets.
	 */
	public void reset() {
		final DeviceContext dev = this.device();
		check(dev.library().vkResetDescriptorPool(dev, this, 0));
	}

	@Override
	protected Destructor<DescriptorPool> destructor(VulkanLibrary lib) {
		return lib::vkDestroyDescriptorPool;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("max", max)
				.build();
	}

	/**
	 * Builder for a descriptor pool.
	 */
	public static class Builder {
		private final Map<VkDescriptorType, Integer> pool = new HashMap<>();
		private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
		private Integer max;

		/**
		 * Adds a descriptor type to this pool.
		 * @param type		Descriptor set type
		 * @param count		Number of available sets of this type
		 */
		public Builder add(VkDescriptorType type, int count) {
			Check.notNull(type);
			Check.oneOrMore(count);
			pool.put(type, count);
			return this;
		}

		/**
		 * Sets the maximum number of sets that <b>can</b> be allocated from this pool.
		 * @param max Maximum number of sets
		 */
		public Builder max(int max) {
			this.max = oneOrMore(max);
			return this;
		}

		/**
		 * Adds a creation flag for this pool.
		 * @param flag Flag
		 */
		public Builder flag(VkDescriptorPoolCreateFlag flag) {
			flags.add(notNull(flag));
			return this;
		}

		/**
		 * Constructs this pool.
		 * @param dev Logical device
		 * @return New descriptor-set pool
		 * @throws IllegalArgumentException if the available sets is empty or the pool size exceeds the specified maximum
		 */
		public DescriptorPool build(DeviceContext dev) {
			// Determine logical maximum number of sets that can be allocated
			final int limit = pool
					.values()
					.stream()
					.mapToInt(Integer::intValue)
					.max()
					.orElseThrow(() -> new IllegalArgumentException("No pool sizes specified"));

			// Initialise or validate the maximum number of sets
			if(max == null) {
				max = limit;
			}
			else
			if(limit > max) {
				throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: limit=%d max=%d", limit, max));
			}

			// Init pool descriptor
			final var info = new VkDescriptorPoolCreateInfo();
			info.flags = IntegerEnumeration.reduce(flags);
			info.poolSizeCount = pool.size();
			info.pPoolSizes = StructureCollector.pointer(pool.entrySet(), new VkDescriptorPoolSize(), Builder::populate);
			info.maxSets = max;

			// Allocate pool
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateDescriptorPool(dev, info, null, ref));

			// Create pool
			return new DescriptorPool(new Handle(ref), dev, max);
		}

		/**
		 * Populates a descriptor pool size from a map entry.
		 */
		private static void populate(Entry<VkDescriptorType, Integer> entry, VkDescriptorPoolSize size) {
			size.type = entry.getKey();
			size.descriptorCount = entry.getValue();
		}
	}
}
