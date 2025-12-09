package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import java.util.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
	/**
	 * A descriptor set <i>resource</i> defines an object that can be applied to this descriptor set.
	 */
	public interface Resource {
		/**
		 * @return Descriptor type
		 */
		VkDescriptorType type();

		/**
		 * Builds the descriptor for this resource.
		 * @return Resource descriptor
		 */
		NativeStructure descriptor();
	}

	/**
	 * A <i>binding</i> defines the properties of a resource in this descriptor set.
	 */
	public record Binding(int index, VkDescriptorType type, int count, Set<VkShaderStageFlags> stages) {
		/**
		 * Constructor.
		 * @param index			Binding index
		 * @param type			Descriptor type
		 * @param count			Array size
		 * @param stages		Pipeline stage flags
		 * @throws IllegalArgumentException if the pipeline {@link #stages} are empty
		 */
		public Binding {
			requireZeroOrMore(index);
			requireNonNull(type);
			requireOneOrMore(count);
			if(stages.isEmpty()) {
				throw new IllegalArgumentException("No pipeline stages specified for binding");
			}
			stages = Set.copyOf(stages);
		}

		/**
		 * Populates a binding descriptor.
		 */
		private VkDescriptorSetLayoutBinding populate() {
			final var binding = new VkDescriptorSetLayoutBinding();
			binding.binding = index;
			binding.descriptorType = type;
			binding.descriptorCount = count;
			binding.stageFlags = new EnumMask<>(stages);
			// TODO - binding.pImmutableSamplers
			// https://registry.khronos.org/VulkanSC/specs/1.0-extensions/man/html/VkDescriptorSetLayoutBinding.html
			return binding;
		}

		/**
		 * Builder for a descriptor set binding.
		 */
		public static class Builder {
			private int binding;
			private VkDescriptorType type;
			private int count = 1;
			private final Set<VkShaderStageFlags> stages = new HashSet<>();

			/**
			 * Sets the index of this binding.
			 * @param binding Binding index
			 */
			public Builder binding(int binding) {
				this.binding = binding;
				return this;
			}

			/**
			 * Sets the descriptor type for this binding.
			 * @param type Descriptor type
			 */
			public Builder type(VkDescriptorType type) {
				this.type = type;
				return this;
			}

			/**
			 * Sets the array count of this binding.
			 * @param count Array count
			 */
			public Builder count(int count) {
				this.count = count;
				return this;
			}

			/**
			 * Adds a shader stage to this binding.
			 * @param stage Shader stage
			 */
			public Builder stage(VkShaderStageFlags stage) {
				stages.add(stage);
				return this;
			}

			/**
			 * Constructs this binding.
			 * @return New binding
			 */
			public Binding build() {
				return new Binding(binding, type, count, stages);
			}
		}
	}

	private final Handle handle;
	private final Map<Binding, Resource> entries = new HashMap<>();
	private final Set<Binding> dirty = new HashSet<>();

	/**
	 * Constructor.
	 * @param handle		Descriptor set
	 * @param bindings		Bindings
	 */
	DescriptorSet(Handle handle, Collection<Binding> bindings) {
		this.handle = requireNonNull(handle);
		init(bindings);
	}

	private void init(Collection<Binding> bindings) {
		for(Binding b : bindings) {
			entries.put(b, null);
			dirty.add(b);
		}
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * Retrieves the current resource for the given binding.
	 * @param binding Binding
	 * @return Descriptor resource for the given binding or {@code null} if not populated
	 */
	public Resource get(Binding binding) {
		return entries.get(binding);
	}

	/**
	 * Sets the resource for the given binding in this descriptor set.
	 * @param binding		Binding
	 * @param resource		Resource
	 * @throws IllegalArgumentException if the {@link #binding} does not belong to the layout of this descriptor set
	 * @throws IllegalArgumentException if the {@link #resource} does not match the {@link Binding#type()}
	 */
	public void set(Binding binding, Resource resource) {
		requireNonNull(binding);
		requireNonNull(resource);
		if(!entries.containsKey(binding)) {
			throw new IllegalArgumentException("Invalid binding for this set: binding=%s set=%s".formatted(binding, this));
		}
		if(resource.type() != binding.type) {
			throw new IllegalArgumentException("Invalid resource for binding: binding=%s resource=%s".formatted(binding, resource));
		}

		entries.put(binding, resource);
		dirty.add(binding);
	}

	/**
	 * Helper.
	 * Sets the resource for a group of descriptor sets.
	 * @param group			Descriptor sets
	 * @param binding		Binding
	 * @param resource		Resource
	 * @see #set(Binding, DescriptorResource)
	 */
	public static void set(Collection<DescriptorSet> group, Binding binding, Resource resource) {
		for(DescriptorSet set : group) {
			set.set(binding, resource);
		}
	}

	/**
	 * Builds an update descriptor for the given entry.
	 */
	private VkWriteDescriptorSet populate(Map.Entry<Binding, Resource> entry) {
		// Validate
		final Binding binding = entry.getKey();
		final Resource resource = entry.getValue();
		if(resource == null) {
			throw new IllegalStateException("Resource not populated: set=%s binding=%s".formatted(this, binding));
		}

		// Init write descriptor
		final var write = new VkWriteDescriptorSet();
		write.sType = VkStructureType.WRITE_DESCRIPTOR_SET;
		write.dstBinding = binding.index();
		write.descriptorType = binding.type();
		write.dstSet = DescriptorSet.this.handle();
		write.descriptorCount = 1;		// Number of elements in resource
		write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

		// Init resource descriptor
		switch(resource.descriptor()) {
			case VkDescriptorImageInfo image -> write.pImageInfo = image;
			case VkDescriptorBufferInfo buffer -> write.pBufferInfo = buffer;
			default -> throw new UnsupportedOperationException("Unsupported resource descriptor: " + resource);
		}

		return write;
	}

	/**
	 * Builds the update descriptors for the modified entries in this set.
	 */
	private Stream<VkWriteDescriptorSet> populate() {
		return entries
				.entrySet()
				.stream()
				.filter(e -> dirty.contains(e.getKey()))
				.map(this::populate);
	}

	/**
	 * Updates the resources of the given descriptor sets.
	 * @param device	Logical device
	 * @param sets		Descriptor sets to update
	 * @return Number of updated descriptor sets
	 * @throws IllegalStateException if any resource has not been populated
	 * @see #set(Binding, DescriptorResource)
	 */
	public static int update(LogicalDevice device, Collection<DescriptorSet> sets) {
		// Enumerate pending updates
		final VkWriteDescriptorSet[] updates = sets
				.stream()
				.flatMap(DescriptorSet::populate)
				.toArray(VkWriteDescriptorSet[]::new);

		// Ignore if nothing to update
		if(updates.length == 0) {
			return 0;
		}

		// Apply updates
		final Library library = device.library();
		library.vkUpdateDescriptorSets(device, updates.length, updates, 0, null);

		// Mark as done
		for(DescriptorSet set : sets) {
			set.dirty.clear();
		}

		return updates.length;
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
	public static Command bind(PipelineLayout layout, List<DescriptorSet> sets) {
		final Library library = layout.device().library();

		return buffer -> library.vkCmdBindDescriptorSets(
				buffer,
				VkPipelineBindPoint.GRAPHICS,
				layout,
				0,					// First set
				sets.size(),
				sets.toArray(DescriptorSet[]::new),
				0,					// Dynamic offset count
				null				// Dynamic offsets
		);
	}

	@Override
	public String toString() {
		return String.format("DescriptorSet[%s]", this.handle());
	}

	/**
	 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
	 */
	public static class Layout extends VulkanObject {
		/**
		 * Creates a descriptor set layout.
		 * @param device		Logical device
		 * @param bindings		Bindings
		 * @param flags			Creation flags
		 * @return New descriptor set layout
		 * @throws IllegalArgumentException if the bindings are empty or contain duplicate indices
		 */
		public static Layout create(LogicalDevice device, Collection<Binding> bindings, Set<VkDescriptorSetLayoutCreateFlags> flags) {
			// Init layout descriptor
			final var info = new VkDescriptorSetLayoutCreateInfo();
			info.sType = VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
			info.flags = new EnumMask<>(flags);
			info.bindingCount = bindings.size();
			info.pBindings = bindings.stream().map(Binding::populate).toArray(VkDescriptorSetLayoutBinding[]::new);

			// Allocate layout
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateDescriptorSetLayout(device, info, null, pointer);

			// Create layout
			return new Layout(pointer.handle(), device, bindings);
		}

		private final Collection<Binding> bindings;

		/**
		 * Constructor.
		 * @param handle		Layout handle
		 * @param device		Logical device
		 * @param bindings		Bindings
		 */
		private Layout(Handle handle, LogicalDevice device, Collection<Binding> bindings) {
			super(handle, device);
			validate(bindings);
			this.bindings = List.copyOf(bindings);
		}

		private static void validate(Collection<Binding> bindings) {
    		final long count = bindings.stream().map(Binding::index).distinct().count();
    		if(count != bindings.size()) {
    			throw new IllegalArgumentException("Binding indices must be unique: " + bindings);
    		}
		}

		/**
		 * @return Bindings
		 */
		public Collection<Binding> bindings() {
			return bindings;
		}

		@Override
		protected Destructor<Layout> destructor() {
			final Library library = this.device().library();
			return library::vkDestroyDescriptorSetLayout;
		}
	}

	/**
	 * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
	 */
	public static class Pool extends VulkanObject {
		private final int max;
		private final Library library;

		/**
		 * Constructor.
		 * @param handle		Pool handle
		 * @param device		Logical device
		 * @param max			Maximum number of descriptor sets that can be allocated from this pool
		 * @param library		Descriptor set library
		 */
		Pool(Handle handle, LogicalDevice device, int max, Library library) {
			super(handle, device);
			this.max = requireOneOrMore(max);
			this.library = requireNonNull(library);
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
		public List<DescriptorSet> allocate(List<Layout> layouts) {
			requireNotEmpty(layouts);
			// TODO - validate layouts vs pool?

			// Build allocation descriptor
			final int count = layouts.size();
			final var info = new VkDescriptorSetAllocateInfo();
			info.sType = VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO;
			info.descriptorPool = this.handle();
			info.descriptorSetCount = count;
			info.pSetLayouts = NativeObject.handles(layouts);

			// Allocate descriptors sets
			final LogicalDevice device = this.device();
			final Library library = device.library();
			final Handle[] handles = new Handle[count];
			library.vkAllocateDescriptorSets(device, info, handles);

			// Create descriptor sets
			return IntStream
					.range(0, count)
					.mapToObj(n -> new DescriptorSet(handles[n], layouts.get(n).bindings()))
					.toList();
		}

		/**
		 * Convenience method to allocate a number of descriptor sets with the given layout.
		 * @param count			Number of sets to allocate
		 * @param layout		Descriptor layout
		 * @return New descriptor sets
		 */
		public List<DescriptorSet> allocate(int count, Layout layout) {
			return allocate(Collections.nCopies(count, layout));
		}

		/**
		 * Releases the given sets back to this pool.
		 * @param sets Sets to release
		 */
		public void free(Collection<DescriptorSet> sets) {
			final DescriptorSet[] array = sets.toArray(DescriptorSet[]::new);
			library.vkFreeDescriptorSets(this.device(), this, array.length, array);
		}

		/**
		 * Resets this pool and releases <b>all</b> allocated descriptor sets.
		 */
		public void reset() {
			library.vkResetDescriptorPool(this.device(), this, 0);
		}

		@Override
		protected Destructor<Pool> destructor() {
			return library::vkDestroyDescriptorPool;
		}

		/**
		 * Builder for a descriptor set pool.
		 */
		public static class Builder {
			private final Map<VkDescriptorType, Integer> pool = new HashMap<>();
			private final Set<VkDescriptorPoolCreateFlags> flags = new HashSet<>();
			private Integer max;

			/**
			 * Adds a descriptor type to this pool.
			 * @param type		Descriptor set type
			 * @param count		Number of available sets of this type
			 */
			public Builder add(VkDescriptorType type, int count) {
				requireNonNull(type);
				requireOneOrMore(count);
				pool.put(type, count);
				return this;
			}

			/**
			 * Sets the maximum number of sets that <b>can</b> be allocated from this pool.
			 * @param max Maximum number of sets
			 */
			public Builder max(int max) {
				this.max = requireOneOrMore(max);
				return this;
			}

			/**
			 * Adds a creation flag for this pool.
			 * @param flag Flag
			 */
			public Builder flag(VkDescriptorPoolCreateFlags flag) {
				requireNonNull(flag);
				flags.add(flag);
				return this;
			}

			/**
			 * Constructs this pool.
			 * @param device Logical device
			 * @return New descriptor-set pool
			 * @throws IllegalArgumentException if the pool empty or the size exceeds the configured maximum
			 */
			public Pool build(LogicalDevice device) {
				// Determine logical maximum number of sets that can be allocated
				final int limit = limit(pool);

				// Init pool descriptor
				final VkDescriptorPoolCreateInfo info = build(limit);

				// Allocate pool
				final Library library = device.library();
				final Pointer pointer = new Pointer();
				library.vkCreateDescriptorPool(device, info, null, pointer);

				// Create pool
				return new Pool(pointer.handle(), device, info.maxSets, library);
			}

			/**
			 * Determines the maximum number of sets that can be allocated from the given pool
			 */
			private static int limit(Map<VkDescriptorType, Integer> pool) {
				return pool
						.values()
						.stream()
						.mapToInt(Integer::intValue)
						.max()
						.orElseThrow(() -> new IllegalArgumentException("No pool sizes specified"));
			}

			/**
			 * Builds the pool descriptor.
			 * @param limit Maximum number of sets
			 */
			private VkDescriptorPoolCreateInfo build(int limit) {
				// Init pool descriptor
				final var info = new VkDescriptorPoolCreateInfo();
				info.sType = VkStructureType.DESCRIPTOR_POOL_CREATE_INFO;
				info.flags = new EnumMask<>(flags);

				// Initialise maximum number of sets that can be allocated
				if(max == null) {
					info.maxSets = limit;
				}
				else {
    				if(max > limit) {
    					throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: limit=%d max=%d", limit, max));
    				}
    				info.maxSets = max;
				}

				// Populate pool sizes table
				info.poolSizeCount = pool.size();
				info.pPoolSizes = pool
						.entrySet()
						.stream()
						.map(Builder::populate)
						.toArray(VkDescriptorPoolSize[]::new);

				return info;
			}

			/**
			 * @return Pool size description
			 */
			private static VkDescriptorPoolSize populate(Map.Entry<VkDescriptorType, Integer> entry) {
				final var size = new VkDescriptorPoolSize();
				size.type = entry.getKey();
				size.descriptorCount = entry.getValue();
				return size;
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
		VkResult vkCreateDescriptorSetLayout(LogicalDevice device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pSetLayout);

		/**
		 * Destroys a descriptor set layout.
		 * @param device				Logical device
		 * @param descriptorSetLayout	Layout
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorSetLayout(LogicalDevice device, Layout descriptorSetLayout, Handle pAllocator);

		/**
		 * Creates a descriptor set pool.
		 * @param device				Logical device
		 * @param pCreateInfo			Descriptor
		 * @param pAllocator			Allocator
		 * @param pDescriptorPool		Returned pool handle
		 * @return Result
		 */
		VkResult vkCreateDescriptorPool(LogicalDevice device, VkDescriptorPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pDescriptorPool);

		/**
		 * Destroys a descriptor set pool.
		 * @param device				Logical device
		 * @param descriptorPool		Pool
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorPool(LogicalDevice device, Pool descriptorPool, Handle pAllocator);

		/**
		 * Allocates a number of descriptor sets from a given pool.
		 * @param device				Logical device
		 * @param pAllocateInfo			Allocation descriptor
		 * @param pDescriptorSets		Returned descriptor set handles
		 * @return Result
		 */
		VkResult vkAllocateDescriptorSets(LogicalDevice device, VkDescriptorSetAllocateInfo pAllocateInfo, @Updated Handle[] pDescriptorSets);

		/**
		 * Resets all descriptor sets in the given pool, i.e. recycles the resources back to the pool and releases the descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param flags					Unused
		 * @return Result
		 */
		VkResult vkResetDescriptorPool(LogicalDevice device, Pool descriptorPool, int flags);

		/**
		 * Releases allocated descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param descriptorSetCount	Number of descriptor sets
		 * @param pDescriptorSets		Descriptor sets
		 * @return Result
		 */
		VkResult vkFreeDescriptorSets(LogicalDevice device, Pool descriptorPool, int descriptorSetCount, DescriptorSet[] pDescriptorSets);

		/**
		 * Updates the resources for one-or-more descriptor sets.
		 * @param device				Logical device
		 * @param descriptorWriteCount	Number of updates
		 * @param pDescriptorWrites		Update descriptors
		 * @param descriptorCopyCount	Number of copies
		 * @param pDescriptorCopies		Copy descriptors
		 */
		void vkUpdateDescriptorSets(LogicalDevice device, int descriptorWriteCount, VkWriteDescriptorSet pDescriptorWrites[], int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

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
		void vkCmdBindDescriptorSets(Command.Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, DescriptorSet[] pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
	}
}
