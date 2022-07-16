package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
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
	private final Set<DescriptorSet> sets = new HashSet<>();
	private final int max;

	/**
	 * Constructor.
	 * @param handle		Pool handle
	 * @param dev			Logical device
	 * @param max			Maximum number of descriptor sets that <b>can</b> be allocated by this pool
	 */
	DescriptorPool(Pointer handle, DeviceContext dev, int max) {
		super(handle, dev);
		this.max = oneOrMore(max);
	}

	/**
	 * @return Maximum number of sets that <b>can</b> be allocated by this pool
	 */
	public int maximum() {
		return max;
	}

	/**
	 * @return Available number of sets that can be allocated by this pool
	 */
	public int available() {
		return max - sets.size();
	}

	/**
	 * @return Allocated descriptor sets
	 */
	public Stream<DescriptorSet> sets() {
		return sets.stream();
	}

	/**
	 * Allocates a number of descriptor sets for the given layouts.
	 * @return New descriptor sets
	 * @throws IllegalArgumentException if the requested number of sets exceeds the maximum for this pool
	 */
	public synchronized List<DescriptorSet> allocate(List<DescriptorLayout> layouts) {
		// Check pool size
		final int size = layouts.size();
		if(sets.size() + size > max) {
			throw new IllegalArgumentException("Number of descriptor sets exceeds the maximum for this pool");
		}

		// Build allocation descriptor
		final var info = new VkDescriptorSetAllocateInfo();
		info.descriptorPool = this.handle();
		info.descriptorSetCount = size;
		info.pSetLayouts = NativeObject.array(layouts);

		// Allocate descriptors sets
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final Pointer[] handles = new Pointer[size];
		check(lib.vkAllocateDescriptorSets(dev, info, handles));

		// Create descriptor sets
		final List<DescriptorSet> allocated = IntStream
				.range(0, handles.length)
				.mapToObj(n -> new DescriptorSet(handles[n], layouts.get(n)))
				.toList();

		// Record sets allocated by this pool
		sets.addAll(allocated);

		return allocated;
	}

	/**
	 * Helper - Allocates a number of descriptor-sets with the given layout.
	 * @param layout		Layout
	 * @param num			Number of sets to allocate
	 * @return New descriptor-sets
	 * @see #allocate(List)
	 */
	public List<DescriptorSet> allocate(DescriptorLayout layout, int num) {
		return allocate(Collections.nCopies(num, layout));
	}

	/**
	 * Releases the given sets back to this pool.
	 * @param sets Sets to release
	 * @throws IllegalArgumentException if the given sets are not present in this pool or have already been released
	 */
	public synchronized void free(Collection<DescriptorSet> sets) {
		// Remove sets
		if(!this.sets.containsAll(sets)) throw new IllegalArgumentException(String.format("Invalid descriptor sets for this pool: sets=%s pool=%s", sets, this));
		this.sets.removeAll(sets);

		// Release sets
		final DeviceContext dev = this.device();
		check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.array(sets)));
	}

	/**
	 * Releases <b>all</b> descriptor-sets allocated by this pool.
	 * @throws IllegalArgumentException if the pool is already empty
	 */
	public synchronized void free() {
		if(sets.isEmpty()) throw new IllegalArgumentException("Pool is already empty");
		final DeviceContext dev = this.device();
		check(dev.library().vkResetDescriptorPool(dev, this, 0));
		sets.clear();
	}

	@Override
	protected Destructor<DescriptorPool> destructor(VulkanLibrary lib) {
		return lib::vkDestroyDescriptorPool;
	}

	@Override
	protected void release() {
		sets.clear();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", this.handle())
				.append("sets", sets.size())
				.append("max", max)
				.build();
	}

	/**
	 * Builder for a descriptor-set pool.
	 */
	public static class Builder {
		private final Map<VkDescriptorType, Integer> pool = new HashMap<>();
		private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
		private Integer max;

		/**
		 * Adds a number of available sets to this pool.
		 * @param type		Descriptor set type
		 * @param count		Number of available sets of this type
		 */
		public DescriptorPool.Builder add(VkDescriptorType type, int count) {
			Check.notNull(type);
			Check.oneOrMore(count);
			pool.put(type, count);
			return this;
		}

		/**
		 * Sets the maximum number of sets that <b>can</b> be allocated from this pool.
		 * @param max Maximum number of sets
		 */
		public DescriptorPool.Builder max(int max) {
			this.max = oneOrMore(max);
			return this;
		}

		/**
		 * Adds a creation flag for this pool.
		 * @param flag Flag
		 */
		public DescriptorPool.Builder flag(VkDescriptorPoolCreateFlag flag) {
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
			info.pPoolSizes = StructureHelper.pointer(pool.entrySet(), VkDescriptorPoolSize::new, Builder::populate);
			info.maxSets = max;

			// Allocate pool
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
			check(lib.vkCreateDescriptorPool(dev, info, null, handle));

			// Create pool
			return new DescriptorPool(handle.getValue(), dev, max);
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