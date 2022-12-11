package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * <p>
 * Example for a texture sampler:
 * <pre>
 * // Define binding for a sampler
 * Binding binding = new Binding.Builder()
 *     .binding(0)
 *     .type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
 *     .stage(VkShaderStage.FRAGMENT)
 *     .build()
 *
 * // Create layout for a sampler
 * Layout layout = Layout.create(List.of(binding, ...));
 *
 * // Create descriptor pool for a double-buffered swapchain
 * Pool pool = new Pool.Builder(dev)
 *  	.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2)
 *  	.max(2)
 *  	.build();
 *
 * // Create descriptor set
 * DescriptorSet ds = pool.allocate(layout);
 *
 * // Create a sampler resource
 * Sampler sampler = ...
 * View view = ...
 * DescriptorResource res = sampler.resource(view);
 *
 * // Populate the sampler
 * ds.set(binding, res);
 *
 * // Apply updates
 * DescriptorSet.update(dev, Set.of(ds));
 *
 * // Create a command to bind the descriptor set to the render sequence
 * Command bind = ds.bind(pipelineLayout);
 * </pre>
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
	/**
	 * A <i>binding</i> defines the properties of a resource in a descriptor set.
	 */
	public record Binding(int index, VkDescriptorType type, int count, Set<VkShaderStage> stages) {
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
		 * Populates a binding descriptor.
		 */
		void populate(VkDescriptorSetLayoutBinding info) {
			info.binding = index;
			info.descriptorType = type;
			info.descriptorCount = count;
			info.stageFlags = BitMask.reduce(stages);
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
			 * Sets the index of this binding (default is binding zero).
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
			 * Sets the array count of this binding (default is one).
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
			 * @see Binding#Binding(int, VkDescriptorType, int, Set)
			 */
			public Binding build() {
				return new Binding(binding, type, count, stages);
			}
		}
	}

	private final Handle handle;
	private final Layout layout;
	private final Map<Binding, DescriptorResource> entries = new HashMap<>();
	private final Set<Binding> modified = new HashSet<>();

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	DescriptorSet(Handle handle, Layout layout) {
		this.handle = notNull(handle);
		this.layout = notNull(layout);
		init();
	}

	/**
	 * Initialises all entries as dirty.
	 */
	private void init() {
		modified.addAll(layout.bindings);
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
	 * Sets the resource in this descriptor set for the given binding.
	 * @param binding 	Binding
	 * @param res		Resource
	 * @throws IllegalArgumentException if {@link #binding} does not belong to this descriptor set
	 * @throws IllegalArgumentException if {@link #res} is not the expected type for the binding
	 * @see #set(Collection, Binding, DescriptorResource)
	 */
	public void set(Binding binding, DescriptorResource res) {
		if(!layout.bindings.contains(binding)) {
			throw new IllegalArgumentException(String.format("Invalid binding for this set: binding=%s this=%s", binding, this));
		}
		if(binding.type() != res.type()) {
			throw new IllegalArgumentException(String.format("Invalid resource for this binding: expected=%s actual=%s", binding.type(), res.type()));
		}
		entries.put(binding, res);
		modified.add(binding);
	}

	/**
	 * Bulk implementation to set a resource for a group of descriptor sets.
	 * @param sets			Descriptor sets
	 * @param binding		Binding
	 * @param res			Resource
	 * @see #set(Binding, DescriptorResource)
	 */
	public static void set(Collection<DescriptorSet> sets, Binding binding, DescriptorResource res) {
		for(DescriptorSet ds : sets) {
			ds.set(binding, res);
		}
	}

	/**
	 * Transient update details.
	 */
	private record Update(DescriptorSet set, Binding binding, DescriptorResource res) {
		/**
		 * Populates the Vulkan structure for this update.
		 */
		private void populate(VkWriteDescriptorSet write) {
			// Validate
			if(res == null) throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%s", set, binding));
			assert set.modified.contains(binding);

			// Init write descriptor
			write.sType = VkStructureType.WRITE_DESCRIPTOR_SET;
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = set.handle();
			write.descriptorCount = 1;		// Number of elements in resource
			write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

			// Init resource descriptor
			switch(res.build()) {
				case VkDescriptorImageInfo image -> write.pImageInfo = image;
				case VkDescriptorBufferInfo buffer -> write.pBufferInfo = buffer;
				default -> throw new UnsupportedOperationException("Unsupported resource descriptor: " + res);
			}
			// TODO - pTexelBuffer
		}

		/**
		 * Marks this update as completed.
		 */
		private void clear() {
			final boolean removed = set.modified.remove(binding);
			assert removed;
		}
	}

	/**
	 * @return Pending updates for this descriptor set
	 */
	private Stream<Update> updates() {
		return layout.bindings
				.stream()
				.filter(modified::contains)
				.map(b -> new Update(this, b, entries.get(b)));
	}

	/**
	 * Updates the resources of the given descriptor sets.
	 * @param dev				Logical device
	 * @param descriptors		Descriptor sets to update
	 * @return Number of updated descriptor sets
	 * @throws IllegalStateException if any resource has not been populated
	 * @see #set(Binding, DescriptorResource)
	 */
	public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
		// Enumerate pending updates
		final List<Update> updates = descriptors
				.stream()
				.flatMap(DescriptorSet::updates)
				.toList();

		// Ignore if nothing to update
		if(updates.isEmpty()) {
			return 0;
		}

		// Apply updates
		final VkWriteDescriptorSet[] writes = StructureCollector.array(updates, new VkWriteDescriptorSet(), Update::populate);
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);
		// TODO - DS copies?

		// Mark as updated
		for(Update update : updates) {
			update.clear();
		}

		return writes.length;
	}

	/**
	 * Creates a bind command for this descriptor set.
	 * @param layout Pipeline layout
	 * @return New bind command
	 * @see #bind(PipelineLayout, Collection)
	 */
	public Command bind(PipelineLayout layout) {
		return bind(layout, List.of(this));
	}

	/**
	 * Creates a bind command for the given descriptor sets.
	 * @param layout		Pipeline layout
	 * @param sets			Descriptor sets
	 * @return New bind command
	 */
	public static Command bind(PipelineLayout layout, Collection<DescriptorSet> sets) {
		final Pointer array = NativeObject.array(sets);
		return (api, cmd) -> api.vkCmdBindDescriptorSets(
				cmd,
				VkPipelineBindPoint.GRAPHICS,
				layout,
				0,					// First set
				sets.size(),
				array,
				0,					// Dynamic offset count
				null				// Dynamic offsets
		);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(handle)
				.append(entries.values())
				.build();
	}

	/**
	 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
	 */
	public static class Layout extends AbstractVulkanObject {
		/**
		 * Creates a descriptor set layout.
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 * @return New descriptor set layout
		 * @throws IllegalArgumentException if the bindings are empty or contain duplicate indices
		 */
		public static Layout create(DeviceContext dev, Collection<Binding> bindings) {
			// Check binding indices
			final long count = bindings.stream().map(Binding::index).distinct().count();
			if(count != bindings.size()) {
				throw new IllegalArgumentException("Binding indices must be unique: " + bindings);
			}

			// Init layout descriptor
			final var info = new VkDescriptorSetLayoutCreateInfo();
			info.bindingCount = bindings.size();
			info.pBindings = StructureCollector.pointer(bindings, new VkDescriptorSetLayoutBinding(), Binding::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateDescriptorSetLayout(dev, info, null, ref));

			// Create layout
			return new Layout(new Handle(ref), dev, bindings);
		}

		private final Collection<Binding> bindings;

		/**
		 * Constructor.
		 * @param handle		Layout handle
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 */
		Layout(Handle handle, DeviceContext dev, Collection<Binding> bindings) {
			super(handle, dev);
			Check.notEmpty(bindings);
			this.bindings = List.copyOf(bindings);
		}

		/**
		 * @return Bindings
		 */
		public Collection<Binding> bindings() {
			return bindings;
		}

		@Override
		protected Destructor<Layout> destructor(VulkanLibrary lib) {
			return lib::vkDestroyDescriptorSetLayout;
		}

		@Override
		public int hashCode() {
			return bindings.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Layout that) &&
					this.bindings.equals(that.bindings());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append(bindings)
					.build();
		}
	}

	/**
	 * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
	 */
	public static class Pool extends AbstractVulkanObject {
		private final int max;

		/**
		 * Constructor.
		 * @param handle		Pool handle
		 * @param dev			Logical device
		 * @param max			Maximum number of descriptor sets that can be allocated from this pool
		 */
		Pool(Handle handle, DeviceContext dev, int max) {
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
		public Collection<DescriptorSet> allocate(List<Layout> layouts) {
			Check.notEmpty(layouts);

			// Build allocation descriptor
			final int count = layouts.size();
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
		public Collection<DescriptorSet> allocate(Layout... layouts) {
			return allocate(Arrays.asList(layouts));
		}

		/**
		 * Convenience method to allocate a number of descriptor sets each with the given layout.
		 * @param count			Number of sets to allocate
		 * @param layout		Descriptor layout
		 * @return New descriptor sets
		 */
		public Collection<DescriptorSet> allocate(int count, Layout layout) {
			return allocate(Collections.nCopies(count, layout));
		}

		/**
		 * Releases the given sets back to this pool.
		 * @param sets Sets to release
		 */
		public void free(Collection<DescriptorSet> sets) {
			final DeviceContext dev = this.device();
			final Library lib = dev.library();
			check(lib.vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.array(sets)));
		}

		/**
		 * Resets this pool and releases <b>all</b> allocated descriptor sets.
		 */
		public void reset() {
			final DeviceContext dev = this.device();
			final Library lib = dev.library();
			check(lib.vkResetDescriptorPool(dev, this, 0));
		}

		@Override
		protected Destructor<Pool> destructor(VulkanLibrary lib) {
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
		 * Builder for a descriptor set pool.
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
				else
				if(limit > max) {
					throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: limit=%d max=%d", limit, max));
				}

				// Init pool descriptor
				final var info = new VkDescriptorPoolCreateInfo();
				info.flags = BitMask.reduce(flags);
				info.poolSizeCount = pool.size();
				info.pPoolSizes = StructureCollector.pointer(pool.entrySet(), new VkDescriptorPoolSize(), Builder::populate);
				info.maxSets = max;

				// Allocate pool
				final VulkanLibrary lib = dev.library();
				final PointerByReference ref = dev.factory().pointer();
				check(lib.vkCreateDescriptorPool(dev, info, null, ref));

				// Create pool
				return new Pool(new Handle(ref), dev, max);
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

	/**
	 * Descriptor set API.
	 */
	interface Library {
		/**
		 * Creates a descriptor set layout.
		 * @param device				Logical device
		 * @param pCreateInfo			Create descriptor
		 * @param pAllocator			Allocator
		 * @param pSetLayout			Returned layout handle
		 * @return Result
		 */
		int vkCreateDescriptorSetLayout(DeviceContext device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);

		/**
		 * Destroys a descriptor set layout.
		 * @param device				Logical device
		 * @param descriptorSetLayout	Layout
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorSetLayout(DeviceContext device, Layout descriptorSetLayout, Pointer pAllocator);

		/**
		 * Creates a descriptor set pool.
		 * @param device				Logical device
		 * @param pCreateInfo			Descriptor
		 * @param pAllocator			Allocator
		 * @param pDescriptorPool		Returned pool
		 * @return Result
		 */
		int vkCreateDescriptorPool(DeviceContext device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);

		/**
		 * Destroys a descriptor set pool.
		 * @param device				Logical device
		 * @param descriptorPool		Pool
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorPool(DeviceContext device, Pool descriptorPool, Pointer pAllocator);

		/**
		 * Allocates a number of descriptor sets from a given pool.
		 * @param device				Logical device
		 * @param pAllocateInfo			Allocation descriptor
		 * @param pDescriptorSets		Returned descriptor set handles
		 * @return Result
		 */
		int vkAllocateDescriptorSets(DeviceContext device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);

		/**
		 * Resets all descriptor sets in the given pool, i.e. recycles the resources back to the pool and releases the descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param flags					Unused
		 * @return Result
		 */
		int vkResetDescriptorPool(DeviceContext device, Pool descriptorPool, int flags);

		/**
		 * Releases allocated descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param descriptorSetCount	Number of descriptor sets
		 * @param pDescriptorSets		Descriptor set handles
		 * @return Result
		 */
		int vkFreeDescriptorSets(DeviceContext device, Pool descriptorPool, int descriptorSetCount, Pointer pDescriptorSets);

		/**
		 * Updates the resources for one-or-more descriptor sets.
		 * @param device				Logical device
		 * @param descriptorWriteCount	Number of updates
		 * @param pDescriptorWrites		Update descriptors
		 * @param descriptorCopyCount	Number of copies
		 * @param pDescriptorCopies		Copy descriptors
		 */
		void vkUpdateDescriptorSets(DeviceContext device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

		/**
		 * Binds one-or-more descriptor sets to the given pipeline.
		 * @param commandBuffer			Command buffer
		 * @param pipelineBindPoint		Bind point
		 * @param layout				Pipeline layout
		 * @param firstSet				Index of the first descriptor set
		 * @param descriptorSetCount	Number of descriptor sets
		 * @param pDescriptorSets		Descriptor sets to update
		 * @param dynamicOffsetCount	Number of dynamic offsets
		 * @param pDynamicOffsets		Dynamic offsets
		 */
		void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, Pointer pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
	}
}
