package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * <p>
 * Example for a fragment shader texture sampler:
 * <pre>
 * 	LogicalDevice dev = ...
 *
 *  // Define layout for a sampler at binding zero
 *  Layout layout = new Layout.Builder(dev)
 *  	.binding(0)
 *  		.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
 *  		.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
 *  		.build()
 *  	.build();
 *
 *  // Create descriptor pool for three swapchain images
 *  Pool pool = new Pool.Builder(dev)
 *  	.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
 *  	.max(3)
 *  	.build();
 *
 *  // Create descriptor sets
 *  List<DescriptorSet> sets = pool.allocate(layout, 3);
 *
 *  Sampler sampler = ...
 *  View view = ...
 *
 *  // Init sampler
 *  for(DescriptorSet set : sets) {
 *  	set.sampler(0, sampler, view);
 *  }
 * </pre>
 * @author Sarge
 */
public class DescriptorSet {
	private final Handle handle;
	private final Layout layout;

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	DescriptorSet(Handle handle, Layout layout) {
		this.handle = notNull(handle);
		this.layout = notNull(layout);
	}

	/**
	 * @return Descriptor set handle
	 */
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Descriptor layout
	 */
	public Layout layout() {
		return layout;
	}

//	// TODO - builder/configure helper for this
//	// TODO - blog: add this and bind command
//	/**
//	 * Binds a sampler to this descriptor set.
//	 * @param binding 		Binding index
//	 * @param sampler 		Sampler
//	 * @param view			Image view
//	 */
//	public void sampler(int binding, Sampler sampler, View view) {
//		// TODO
//		final VkDescriptorSetLayoutBinding entry = layout.bindings.get(binding);
//		if(entry == null) throw new IllegalArgumentException("");
//		if(entry.descriptorType != VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) throw new IllegalArgumentException("");
//
//		// array size = entry.descriptorCount
//
//		final VkDescriptorImageInfo image = new VkDescriptorImageInfo(); // TODO - by ref?
//		image.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
//		image.imageView = view.handle();
//		image.sampler = sampler.handle();
//
//		// Init update descriptor
//		final VkWriteDescriptorSet info = new VkWriteDescriptorSet();
//		info.dstBinding = entry.binding;
//		info.descriptorType = entry.descriptorType;
//
//		info.dstSet = this.handle();
//		info.dstArrayElement = 0; // TODO
//
//		info.descriptorCount = 1; // TODO - size of images array (was entry.descriptorCount)
//		info.pImageInfo = StructureHelper.structures(List.of(image));
//
//		info.pBufferInfo = null;
//		info.pTexelBufferView = null;
//
//		// Update descriptor set
//		final LogicalDevice dev = layout.device();
//		dev.library().vkUpdateDescriptorSets(dev.handle(), 1, new VkWriteDescriptorSet[]{info}, 0, null);
//		// TODO - move this to a helper/builder for group of updates
//	}
//
//	// TODO - uniform buffers
////	final VkDescriptorBufferInfo info = new VkDescriptorBufferInfo();
////	info.buffer = ((PointerHandle) buffer).handle(); // TODO - nasty!
////	info.offset = zeroOrMore(offset);
////	info.range = oneOrMore(size);
////	update(binding, VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, write -> write.pBufferInfo = info);

	/**
	 * Helper - Creates a pipeline bind command for this descriptor set.
	 * @param layout Pipeline layout
	 * @return New bind command
	 */
	public Command bind(Pipeline.Layout layout) {
		return bind(layout, List.of(this));
	}

	/**
	 * Creates a pipeline bind command for the given descriptor sets.
	 * @param layout		Pipeline layout
	 * @param sets			Descriptor sets
	 * @return New bind command
	 */
	public static Command bind(Pipeline.Layout layout, Collection<DescriptorSet> sets) {
		final Pointer[] handles = Handle.toArray(sets, DescriptorSet::handle);

		return (api, cmd) -> api.vkCmdBindDescriptorSets(
				cmd,
				VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
				layout.handle(),
				0,					// First set
				1,					// Count // TODO - count = handles.length???
				handles,
				0,					// Dynamic offset count
				null				// Dynamic offsets
		);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof DescriptorSet that) &&
				handle.equals(that.handle) &&
				layout.equals(that.layout);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("layout", layout)
				.build();
	}

	/**
	 * Wrapper for an update to a descriptor set.
	 */
	public final static class Update {
		private final VkDescriptorType type;
		private final Consumer<VkWriteDescriptorSet> consumer;

		/**
		 * Constructor.
		 * @param type			Descriptor type
		 * @param consumer		Consumer to populate the write descriptor
		 */
		Update(VkDescriptorType type, Consumer<VkWriteDescriptorSet> consumer) {
			this.type = notNull(type);
			this.consumer = notNull(consumer);
		}

		/**
		 * @return Descriptor type for this update
		 */
		public VkDescriptorType type() {
			return type;
		}

		/**
		 * Applies to this update to the given write descriptor.
		 */
		void apply(VkWriteDescriptorSet write) {
			consumer.accept(write);
		}
	}

	/**
	 * The <i>updater</i> is used to initialise a group of descriptor sets.
	 */
	public static class Updater {
		private final Collection<DescriptorSet> sets;
		private final List<VkWriteDescriptorSet> writes = new ArrayList<>();

		/**
		 * Constructor.
		 * @param sets Descriptor sets to update
		 */
		public Updater(Collection<DescriptorSet> sets) {
			this.sets = notEmpty(sets);
		}

		/**
		 * Applies the given update.
		 * @param binding	Binding index
		 * @param update	Update
		 * @throws IllegalArgumentException if the binding or update is invalid for the descriptors
		 */
		public Updater update(int binding, Update update) {
			for(DescriptorSet set : sets) {
				// Lookup layout binding
				final VkDescriptorSetLayoutBinding entry = set.layout.bindings.get(binding);
				if(entry == null) throw new IllegalArgumentException("Invalid binding: " + binding);

				// Create update write descriptor
				final VkWriteDescriptorSet write = new VkWriteDescriptorSet();
				write.dstBinding = binding;
				write.dstSet = set.handle();
				write.descriptorType = entry.descriptorType;
				write.descriptorCount = 1;
				write.dstArrayElement = 0;

				write.pBufferInfo = null;
				write.pTexelBufferView = null;

				// Init entry for this descriptor
				if(update.type != entry.descriptorType) {
					throw new IllegalArgumentException(String.format("Invalid update type for descriptor: binding=%s expected=%s actual=%s", binding, entry.descriptorType, update.type));
				}
				update.apply(write);

				// Add update
				writes.add(write);
			}

			return this;
		}

		/**
		 * Applies this update.
		 * @param dev Logical device
		 * @throws IllegalArgumentException if no updates have been specified
		 */
		public void update(LogicalDevice dev) {
			// TODO - Why can't we just pass an array here?  WHY WHY WHY?
			if(writes.isEmpty()) throw new IllegalArgumentException("No updates specified");
			dev.library().vkUpdateDescriptorSets(dev.handle(), writes.size(), StructureHelper.structures(writes), 0, null);
		}
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
		 * @param max			Maximum number of descriptor sets
		 */
		Pool(Pointer handle, LogicalDevice dev, int max) {
			super(handle, dev, dev.library()::vkDestroyDescriptorPool);
			this.max = oneOrMore(max);
		}

		/**
		 * @return Maximum number of sets that can be allocated by this pool
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
		 * Allocates descriptor-sets for the given layouts.
		 * @return New descriptor-sets
		 * @throws IllegalArgumentException if the requested number of sets exceeds the maximum for this pool
		 */
		public synchronized List<DescriptorSet> allocate(List<Layout> layouts) {
			// Check pool size
			if(this.sets.size() + layouts.size() > max) {
				throw new IllegalArgumentException("Number of descriptor sets exceeds the maximum for this pool");
			}

			// Build allocation descriptor
			final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
			info.descriptorPool = this.handle();
			info.descriptorSetCount = layouts.size();
			info.pSetLayouts = toPointerArray(layouts);

			// Allocate descriptors sets
			final LogicalDevice dev = this.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = lib.factory().pointers(layouts.size());
			check(lib.vkAllocateDescriptorSets(dev.handle(), info, handles));

			// Create descriptor sets
			final List<DescriptorSet> sets = new ArrayList<>(handles.length);
			for(int n = 0; n < handles.length; ++n) {
				final Handle handle = new Handle(handles[n]);
				final DescriptorSet set = new DescriptorSet(handle, layouts.get(n));
				sets.add(set);
			}

			// Maintain allocated sets
			this.sets.addAll(sets);

			return sets;
		}

		/**
		 * Helper - Allocates a number of descriptor-sets all with the given layout.
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
			final LogicalDevice dev = this.device();
			check(dev.library().vkFreeDescriptorSets(dev.handle(), this.handle(), sets.size(), Handle.toArray(sets, DescriptorSet::handle)));
		}

		/**
		 * Releases <b>all</b> descriptor-sets allocated by this pool.
		 * @throws IllegalArgumentException if the pool is already empty
		 */
		public synchronized void free() {
			if(sets.isEmpty()) throw new IllegalArgumentException("Pool is already empty");
			final LogicalDevice dev = this.device();
			check(dev.library().vkResetDescriptorPool(dev.handle(), this.handle(), 0));
			sets.clear();
		}

		@Override
		public synchronized void destroy() {
			super.destroy();
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
			private final LogicalDevice dev;
			private final List<VkDescriptorPoolSize> entries = new ArrayList<>();
			private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
			private int max = 1;

			/**
			 * Constructor.
			 * @param dev Logical device
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Adds a number of available sets to this pool.
			 * @param type		Descriptor set type
			 * @param count		Number of available sets of this type
			 */
			public Builder add(VkDescriptorType type, int count) {
				final VkDescriptorPoolSize entry = new VkDescriptorPoolSize();
				entry.type = notNull(type);
				entry.descriptorCount = oneOrMore(count);
				entries.add(entry);
				return this;
			}

			/**
			 * Sets the maximum number of sets to allocate from this pool.
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
			 * @return New descriptor-set pool
			 * @throws IllegalArgumentException if the available sets is empty or the total number of exceeds the specified maximum
			 */
			public Pool build() {
				// Validate
				final int total = entries.stream().mapToInt(entry -> entry.descriptorCount).sum();
				if(entries.isEmpty()) throw new IllegalArgumentException("No pool sizes specified");
				if(total > max) throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: total=%d max=%d", total, max));

				// Init pool descriptor
				final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
				info.flags = IntegerEnumeration.mask(flags);
				info.poolSizeCount = entries.size();
				info.pPoolSizes = StructureHelper.structures(entries);
				info.maxSets = max;

				// Allocate pool
				final VulkanLibrary lib = dev.library();
				final PointerByReference handle = lib.factory().pointer();
				check(lib.vkCreateDescriptorPool(dev.handle(), info, null, handle));

				// Create pool
				return new Pool(handle.getValue(), dev, max);
			}
		}
	}

	/**
	 * A <i>descriptor set layout</i>
	 * TODO
	 */
	public static class Layout extends AbstractVulkanObject {
		private final Map<Integer, VkDescriptorSetLayoutBinding> bindings;

		/**
		 * Constructor.
		 * @param handle		Layout handle
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 */
		Layout(Pointer handle, LogicalDevice dev, List<VkDescriptorSetLayoutBinding> bindings) {
			super(handle, dev, dev.library()::vkDestroyDescriptorSetLayout);
			Check.notEmpty(bindings);
			this.bindings = bindings.stream().collect(toMap(entry -> entry.binding, Function.identity()));
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("handle", this.handle())
					.append("bindings", bindings.values().stream().map(Layout::toString).collect(toList()))
					.build();
		}

		private static String toString(VkDescriptorSetLayoutBinding binding) {
			return new ToStringBuilder(binding)
					.append("binding", binding.binding)
					.append("type", binding.descriptorType)
					.append("count", binding.descriptorCount)
					.append("stages", IntegerEnumeration.enumerate(VkPipelineStageFlag.class, binding.stageFlags))
					.build();
		}

		/**
		 * Builder for a descriptor-set layout.
		 */
		public static class Builder {
			private final LogicalDevice dev;
			private final LinkedList<VkDescriptorSetLayoutBinding> bindings = new LinkedList<>();

			/**
			 * Constructor.
			 * @param dev Logical device
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Starts a new layout binding.
			 * @return New layout binding builder
			 */
			public LayoutBindingBuilder binding(int index) {
				return new LayoutBindingBuilder().binding(index);
			}

			/**
			 * Starts a new layout binding at the next available binding index.
			 * @return New layout binding builder
			 */
			public LayoutBindingBuilder binding() {
				return binding(next());
			}

			/**
			 * @return Next available binding index
			 */
			private int next() {
				if(bindings.isEmpty()) {
					return 0;
				}
				else {
					final VkDescriptorSetLayoutBinding prev = bindings.getLast();
					return prev.binding + 1;
				}
			}

			/**
			 * Constructs this descriptor-set layout.
			 * @return New layout
			 */
			public Layout build() {
				// Init layout descriptor
				final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
				info.bindingCount = bindings.size();
				info.pBindings = StructureHelper.structures(bindings);

				// Allocate layout
				final VulkanLibrary lib = dev.library();
				final PointerByReference handle = lib.factory().pointer();
				check(lib.vkCreateDescriptorSetLayout(dev.handle(), info, null, handle));

				// Create layout
				return new Layout(handle.getValue(), dev, bindings);
			}

			/**
			 * Nested builder for a layout binding.
			 */
			public class LayoutBindingBuilder {
				private final Set<VkShaderStageFlag> stages = new HashSet<>();
				private int index;
				private VkDescriptorType type;
				private int count = 1;

				private LayoutBindingBuilder() {
				}

				/**
				 * Sets the binding index (default is the next available index in this layout).
				 * @param index Binding index
				 * @throws IllegalArgumentException if the binding index is already populated in this layout
				 */
				public LayoutBindingBuilder binding(int index) {
					if(bindings.stream().anyMatch(binding -> binding.binding == index)) throw new IllegalArgumentException("Duplicate binding index: " + index);
					this.index = zeroOrMore(index);
					return this;
				}

				/**
				 * Sets the binding index.
				 * @param binding Binding index
				 */
				public LayoutBindingBuilder type(VkDescriptorType type) {
					this.type = notNull(type);
					return this;
				}

				/**
				 * Sets the descriptor count (for an array type).
				 * @param count Descriptor count
				 */
				public LayoutBindingBuilder count(int count) {
					this.count = oneOrMore(count);
					return this;
				}

				/**
				 * Adds a pipeline stage for this binding.
				 * @param stage Pipeline stage
				 */
				public LayoutBindingBuilder stage(VkShaderStageFlag stage) {
					Check.notNull(stage);
					stages.add(stage);
					return this;
				}

				/**
				 * Constructs this layout binding.
				 * @return New descriptor-set layout binding
				 * @throws IllegalArgumentException if the descriptor type is not populated
				 * @throws IllegalArgumentException if no pipeline stages are specified
				 */
				public Builder build() {
					// Validate
					if(type == null) throw new IllegalArgumentException("Descriptor type not specified");
					if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stage(s) specified");

					// Build binding descriptor
					final VkDescriptorSetLayoutBinding binding = new VkDescriptorSetLayoutBinding();
					binding.binding = index;
					binding.descriptorType = type;
					binding.descriptorCount = count;
					binding.stageFlags = IntegerEnumeration.mask(stages);

					// Add binding
					bindings.add(binding);
					return Builder.this;
				}
			}
		}
	}
}
