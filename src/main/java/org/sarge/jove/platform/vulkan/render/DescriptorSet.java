package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Resource;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * <p>
 * Example for a texture sampler:
 * <pre>
 *  // Define binding for a sampler at binding zero
 *  Binding binding = new Binding.Builder()
 * 		.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
 * 		.stage(VkShaderStage.FRAGMENT)
 * 		.build()
 *
 *  // Create layout for a sampler at binding zero
 *  Layout layout = Layout.create(List.of(binding, ...));
 *
 *  // Create descriptor pool for 3 swapchain images
 *  Pool pool = new Pool.Builder(dev)
 *  	.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 3)
 *  	.max(3)
 *  	.build();
 *
 *  // Create descriptors
 *  List descriptors = pool.allocate(layout, 3);
 *
 *  // Create a descriptor set resource
 *  View view = ...
 *  Resource res = sampler.resource(view);
 *  ...
 *
 *  // Set resource
 *  DescriptorSet first = descriptors.get(0);
 *  first.set(binding, res);
 *
 *  // Or bulk set resources
 *  DescriptorSet.set(descriptors, binding, res);
 *
 *  // Apply updates
 *  DescriptorSet.update(dev, descriptors);
 * </pre>
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
	/**
	 * A <i>descriptor set binding</i> defines the resource properties for a binding in this descriptor set.
	 */
	public static record Binding(int index, VkDescriptorType type, int count, Set<VkShaderStage> stages) {
		/**
		 * Constructor.
		 * @param index			Binding index
		 * @param type			Descriptor type
		 * @param count			Array size
		 * @param stages		Pipeline stage flags
		 * @throws IllegalArgumentException if the pipeline {@link #stages} is empty
		 */
		public Binding {
			if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stages specified for binding");
			Check.zeroOrMore(index);
			Check.notNull(type);
			Check.oneOrMore(count);
			stages = Set.copyOf(stages);
		}

		/**
		 * Populates a layout binding descriptor.
		 */
		private void populate(VkDescriptorSetLayoutBinding info) {
			info.binding = index;
			info.descriptorType = type;
			info.descriptorCount = count;
			info.stageFlags = IntegerEnumeration.mask(stages);
		}

		/**
		 * Builder for a layout binding.
		 */
		public static class Builder {
			private int binding;
			private VkDescriptorType type;
			private int count = 1;
			private final Set<VkShaderStage> stages = new HashSet<>();

			/**
			 * Sets the index of this binding.
			 * @param binding Binding index
			 */
			public Builder binding(int binding) {
				this.binding = zeroOrMore(binding);
				return this;
			}

			/**
			 * Sets the descriptor type for this binding.
			 * @param type Descriptor type
			 */
			public Builder type(VkDescriptorType type) {
				this.type = notNull(type);
				return this;
			}

			/**
			 * Sets the array count of this binding.
			 * @param count Array count
			 */
			public Builder count(int count) {
				this.count = oneOrMore(count);
				return this;
			}

			/**
			 * Adds a shader stage to this binding.
			 * @param stage Shader stage
			 */
			public Builder stage(VkShaderStage stage) {
				stages.add(notNull(stage));
				return this;
			}

			/**
			 * Constructs this binding.
			 * @return New layout binding
			 */
			public Binding build() {
				return new Binding(binding, type, count, stages);
			}
		}
	}

	/**
	 * An <i>entry</i> records the resource for each binding in this descriptor set.
	 */
	private class Entry {
		private final Binding binding;
		private boolean dirty = true;
		private Resource res;

		/**
		 * Constructor.
		 * @param binding Binding descriptor
		 */
		private Entry(Binding binding) {
			this.binding = notNull(binding);
		}

		/**
		 * @return Whether this entry has been modified
		 */
		boolean isDirty() {
			return dirty;
		}

		/**
		 * Populates the given descriptor set update record from this entry.
		 * @param write Descriptor set update record
		 */
		void populate(VkWriteDescriptorSet write) {
			// Validate
			final DescriptorSet set = DescriptorSet.this;
			if(res == null) {
				throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%d", set, binding.index));
			}

			// Init write descriptor
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = set.handle();
			write.descriptorCount = 1; // TODO - res.size()
			write.dstArrayElement = 0; // TODO

			// Populate resource
			res.populate(write);

			// Update flag
			assert dirty;
			dirty = false;
		}
	}

	private final Handle handle;
	private final Layout layout;
	private final Map<Binding, Entry> entries;

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	public DescriptorSet(Handle handle, Layout layout) {
		this.handle = notNull(handle);
		this.layout = notNull(layout);
		this.entries = layout.bindings.values().stream().collect(toMap(Function.identity(), Entry::new));
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Layout for this descriptor set
	 */
	public Layout layout() {
		return layout;
	}

	/**
	 * @param binding Binding descriptor
	 * @return Entry for this given binding
	 */
	private Entry entry(Binding binding) {
		return entries.get(binding);
	}

	/**
	 * @return Modified entries in this descriptor set
	 */
	private Stream<Entry> modified() {
		return entries
				.values()
				.stream()
				.filter(Entry::isDirty);
	}

	/**
	 * Retrieves the resource in this descriptor set for the given binding.
	 * @param binding Binding
	 * @return Resource or {@code null} if not populated
	 * @throws IllegalArgumentException if the binding does not belong to the layout of this descriptor set
	 * @see #set(Binding, Resource)
	 */
	public Resource resource(Binding binding) {
		final Entry entry = entry(binding);
		if(entry == null) throw new IllegalArgumentException(String.format("Invalid binding for this descriptor set: binding=%s set=%s", binding, this));
		return entry.res;
	}

	/**
	 * Sets the resource in this descriptor set for the given binding.
	 * @param binding 	Binding
	 * @param res		Resource
	 * @throws IllegalArgumentException if the binding does not belong to the layout of this descriptor set
	 * @see #set(Collection, Binding, Resource)
	 */
	public void set(Binding binding, Resource res) {
		final Entry entry = entry(binding);
		entry.res = notNull(res);
		entry.dirty = true;
	}

	/**
	 * Helper - Bulk implementation of the {@link #set(Binding, Resource)} method.
	 * @param sets			Descriptor sets
	 * @param binding		Binding
	 * @param res			Resource
	 * @throws IllegalArgumentException if the binding does not belong to the layout of all descriptor sets
	 */
	public static void set(Collection<DescriptorSet> sets, Binding binding, Resource res) {
		for(DescriptorSet ds : sets) {
			ds.set(binding, res);
		}
	}

	/**
	 * Updates the resources for the given descriptor sets.
	 * @param dev				Logical device
	 * @param descriptors		Descriptor sets to update
	 * @return Number of updated descriptor sets
	 */
	public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
		// Enumerate dirty resources
		final var writes = descriptors
				.stream()
				.flatMap(DescriptorSet::modified)
				.collect(StructureHelper.collector(VkWriteDescriptorSet::new, Entry::populate));

		// Ignore if nothing to update
		if((writes == null) || (writes.length == 0)) {
			return 0;
		}

		// Apply update
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);

		return writes.length;
	}
	// TODO - test return value

	/**
	 * Creates a pipeline bind command for this descriptor set.
	 * @param layout Pipeline layout
	 * @return New bind command
	 */
	public Command bind(PipelineLayout layout) {
		return bind(layout, List.of(this));
	}

	/**
	 * Creates a pipeline bind command for the given descriptor sets.
	 * @param layout		Pipeline layout
	 * @param sets			Descriptor sets
	 * @return New bind command
	 */
	public static Command bind(PipelineLayout layout, Collection<DescriptorSet> sets) {
		return (api, cmd) -> api.vkCmdBindDescriptorSets(
				cmd,
				VkPipelineBindPoint.GRAPHICS,
				layout,
				0,					// First set
				sets.size(),
				NativeObject.toArray(sets),
				0,					// Dynamic offset count
				null				// Dynamic offsets
		);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("resources", entries.values())
				.build();
	}

	/**
	 * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
	 */
	public static class Pool extends AbstractVulkanObject {
		private final Set<DescriptorSet> sets = new HashSet<>();
		private final int max;

		/**
		 * Constructor.
		 * @param handle		Pool handle
		 * @param dev			Logical device
		 * @param max			Maximum number of descriptor sets that <b>can</b> be allocated by this pool
		 */
		Pool(Pointer handle, DeviceContext dev, int max) {
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
		public synchronized List<DescriptorSet> allocate(List<Layout> layouts) {
			// Check pool size
			final int size = layouts.size();
			if(sets.size() + size > max) {
				throw new IllegalArgumentException("Number of descriptor sets exceeds the maximum for this pool");
			}

			// Build allocation descriptor
			final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
			info.descriptorPool = this.handle();
			info.descriptorSetCount = size;
			info.pSetLayouts = NativeObject.toArray(layouts);

			// Allocate descriptors sets
			final DeviceContext dev = this.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = lib.factory().array(size);
			check(lib.vkAllocateDescriptorSets(dev, info, handles));

			// Create descriptor sets
			final IntFunction<DescriptorSet> ctor = index -> {
				final Handle handle = new Handle(handles[index]);
				return new DescriptorSet(handle, layouts.get(index));
			};
			final var allocated = IntStream
					.range(0, handles.length)
					.mapToObj(ctor)
					.collect(toList());

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
		public List<DescriptorSet> allocate(Layout layout, int num) {
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
			check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.toArray(sets)));
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
		protected Destructor<Pool> destructor(VulkanLibrary lib) {
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
			public Pool build(DeviceContext dev) {
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
				else {
					if(limit > max) {
						throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: limit=%d max=%d", limit, max));
					}
				}

				// Init pool descriptor
				final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
				info.flags = IntegerEnumeration.mask(flags);
				info.poolSizeCount = pool.size();
				info.pPoolSizes = StructureHelper.first(pool.entrySet(), VkDescriptorPoolSize::new, Builder::populate);
				info.maxSets = max;

				// Allocate pool
				final VulkanLibrary lib = dev.library();
				final PointerByReference handle = lib.factory().pointer();
				check(lib.vkCreateDescriptorPool(dev, info, null, handle));

				// Create pool
				return new Pool(handle.getValue(), dev, max);
			}

			/**
			 * Populates a descriptor pool size from a map entry.
			 */
			private static void populate(Map.Entry<VkDescriptorType, Integer> entry, VkDescriptorPoolSize size) {
				size.type = entry.getKey();
				size.descriptorCount = entry.getValue();
			}
		}
	}

	/**
	 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
	 * @see Resource
	 */
	public static class Layout extends AbstractVulkanObject {
		/**
		 * Creates a descriptor set layout.
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 * @return New descriptor set layout
		 * @throws IllegalArgumentException for if the bindings are empty
		 * @throws IllegalStateException for a duplicate binding index
		 */
		public static Layout create(DeviceContext dev, List<Binding> bindings) {
			// Init layout descriptor
			final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
			info.bindingCount = bindings.size();
			info.pBindings = StructureHelper.first(bindings, VkDescriptorSetLayoutBinding::new, Binding::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateDescriptorSetLayout(dev, info, null, handle));

			// Create layout
			return new Layout(handle.getValue(), dev, bindings);
		}

		private final Map<Integer, Binding> bindings;

		/**
		 * Constructor.
		 * @param handle		Layout handle
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 * @throws IllegalStateException for a duplicate binding index
		 */
		Layout(Pointer handle, DeviceContext dev, List<Binding> bindings) {
			super(handle, dev);
			Check.notEmpty(bindings);
			this.bindings = bindings.stream().collect(toMap(Binding::index, Function.identity()));
		}

		/**
		 * Looks up a binding descriptor.
		 * @param index Binding index
		 * @return Binding
		 */
		public Binding binding(int index) {
			return bindings.get(index);
		}

		@Override
		protected Destructor<Layout> destructor(VulkanLibrary lib) {
			return lib::vkDestroyDescriptorSetLayout;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("handle", this.handle())
					.append("bindings", bindings.values())
					.build();
		}
	}
}
