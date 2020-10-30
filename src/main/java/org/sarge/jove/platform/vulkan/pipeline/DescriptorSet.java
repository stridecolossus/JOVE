package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Layout.Binding;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * <p>
 * Example for a fragment shader texture sampler:
 * <pre>
 * 	LogicalDevice dev = ...
 *
 *  // Define binding for a sampler at binding zero
 *  Binding binding = new Binding.Builder()
 * 		.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
 * 		.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
 * 		.build()
 *
 *  // Create layout for a sampler at binding zero
 *  Layout layout = Layout.create(List.of(binding, ...));
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
 *  // Update descriptor set
 *  new DescriptorSet.Update.Builder()
 *		.descriptors(descriptors)
 *		.add(0, sampler.update(texture))
 *  	.build();
 * </pre>
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
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

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Descriptor layout
	 */
	public Layout layout() {
		return layout;
	}

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
		final Pointer[] handles = Handle.toArray(sets);

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
	 * An <i>update</i> initialises a descriptor set.
	 */
	public static abstract class Update {
		private final VkDescriptorType type;

		/**
		 * Constructor.
		 * @param type Expected descriptor type
		 */
		protected Update(VkDescriptorType type) {
			this.type = notNull(type);
		}

		/**
		 * @return Expected descriptor type for this update
		 */
		public VkDescriptorType type() {
			return type;
		}

		/**
		 * Populates the given descriptor for this update.
		 * @param write Update write descriptor
		 */
		protected abstract void apply(VkWriteDescriptorSet write);

		/**
		 * Builder for a descriptor set update.
		 * <p>
		 * Usage:
		 * <pre>
		 *  LogicalDevice dev = ...
		 *  Update sampler = ...
		 *  Update uniform = ...
		 *
		 *  new Builder()
		 *  	// Update two resources
		 *  	.descriptor(set)
		 *  	.add(0, sampler)
		 *  	.add(1, uniform)
		 *  	// Update a group of descriptors
		 *  	.descriptors(sets)
		 *  	.add(2, ...)
		 *  	// Apply updates
		 *  	.update(dev);
		 * </pre>
		 */
		public static class Builder {
			/**
			 * Transient update entry.
			 */
			private static record Entry(DescriptorSet set, Binding binding, Update update) {
				// Empty
			}

			private final List<Entry> updates = new ArrayList<>();
			private Collection<DescriptorSet> sets;

			/**
			 * Sets the descriptor set to be updated.
			 * @param set Descriptor set to update
			 */
			public Builder descriptor(DescriptorSet set) {
				return descriptors(List.of(set));
			}

			/**
			 * Sets the descriptor sets to be updated.
			 * @param set Descriptor sets to update
			 */
			public Builder descriptors(Collection<DescriptorSet> sets) {
				this.sets = Set.copyOf(notEmpty(sets));
				return this;
			}

			/**
			 * Adds the given update for the currently specified descriptor sets.
			 * @param binding		Binding index
			 * @param update 		Update to apply
			 * @throws IllegalStateException if no descriptor sets have been specified
			 * @throws IllegalArgumentException if the binding index or type of update is invalid for any descriptor set
			 */
			public Builder add(int index, Update update) {
				if(sets == null) throw new IllegalStateException("No descriptor sets specified for this update");

				for(DescriptorSet set : sets) {
					// Lookup binding descriptor for this set
					final Binding binding = set.layout().binding(index);
					if(binding == null) {
						throw new IllegalArgumentException(String.format("Invalid binding index for descriptor set: index=%d set=%s", binding, set));
					}

					// Check set matches the update type
					if(binding.type != update.type()) {
						throw new IllegalArgumentException(String.format("Invalid update for descriptor set: type=%s set=%s", update.type(), set));
					}

					// Add entry
					updates.add(new Entry(set, binding, update));
				}

				return this;
			}

			/**
			 * Applies this update.
			 * @param dev Logical device
			 * @throws IllegalStateException if no updates have been specified
			 */
			public void update(LogicalDevice dev) {
				if(updates.isEmpty()) throw new IllegalStateException("No updates specified");

				// Allocate contiguous memory block of write descriptors for the updates
				final VkWriteDescriptorSet[] writes = (VkWriteDescriptorSet[]) new VkWriteDescriptorSet().toArray(updates.size());

				// Populate write descriptors
				for(int n = 0; n < writes.length; ++n) {
					// Populate write descriptor for this entry
					final Entry entry = updates.get(n);
					final VkWriteDescriptorSet write = writes[n];
					write.dstBinding = entry.binding.binding;
					write.descriptorType = entry.binding.type;
					write.dstSet = entry.set.handle();
					write.descriptorCount = 1;
					write.dstArrayElement = 0;

					// Populate update descriptor
					entry.update.apply(write);
				}

				// Apply updates
				dev.library().vkUpdateDescriptorSets(dev.handle(), writes.length, writes, 0, null);
			}
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
			info.pSetLayouts = Handle.toPointerArray(layouts);

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
			check(dev.library().vkFreeDescriptorSets(dev.handle(), this.handle(), sets.size(), Handle.toArray(sets)));
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
			private final Map<VkDescriptorType, Integer> entries = new HashMap<>();
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
				Check.notNull(type);
				Check.oneOrMore(count);
				entries.put(type, count);
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
				final int total = entries.values().stream().mapToInt(Integer::intValue).sum();
				if(entries.isEmpty()) throw new IllegalArgumentException("No pool sizes specified");
				if(total > max) throw new IllegalArgumentException(String.format("Total available descriptor sets exceeds the specified maximum: total=%d max=%d", total, max));

				// Init pool descriptor
				final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
				info.flags = IntegerEnumeration.mask(flags);
				info.poolSizeCount = entries.size();
				info.pPoolSizes = VulkanStructure.array(VkDescriptorPoolSize::new, entries.entrySet(), Builder::populate);
				info.maxSets = max;

				// Allocate pool
				final VulkanLibrary lib = dev.library();
				final PointerByReference handle = lib.factory().pointer();
				check(lib.vkCreateDescriptorPool(dev.handle(), info, null, handle));

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
	 * A <i>descriptor set layout</i> specifies the structure of descriptors that can be bound to a pipeline.
	 */
	public static class Layout extends AbstractVulkanObject {
		/**
		 * Descriptor for a binding in this layout.
		 */
		public static record Binding(int binding, VkDescriptorType type, int count, Set<VkShaderStageFlag> stages) {
			/**
			 * Constructor.
			 * @param binding		Binding index
			 * @param type			Descriptor type
			 * @param count			Array size
			 * @param stages		Pipeline stage flags
			 * @throws IllegalArgumentException if pipeline stages is empty
			 */
			public Binding(int binding, VkDescriptorType type, int count, Set<VkShaderStageFlag> stages) {
				if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stages specified for binding");
				this.binding = zeroOrMore(binding);
				this.type = notNull(type);
				this.count = oneOrMore(count);
				this.stages = Set.copyOf(stages);
			}

			/**
			 * Populates a layout binding descriptor.
			 */
			private void populate(VkDescriptorSetLayoutBinding info) {
				info.binding = binding;
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
				private final Set<VkShaderStageFlag> stages = new HashSet<>();

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
				public Builder stage(VkShaderStageFlag stage) {
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
		 * Creates a descriptor set layout.
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 * @return New descriptor set layout
		 * @throws IllegalArgumentException for if the bindings are empty
		 * @throws IllegalStateException for a duplicate binding index
		 */
		public static Layout create(LogicalDevice dev, List<Binding> bindings) {
			// Init layout descriptor
			final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
			info.bindingCount = bindings.size();
			info.pBindings = VulkanStructure.array(VkDescriptorSetLayoutBinding::new, bindings, Binding::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateDescriptorSetLayout(dev.handle(), info, null, handle));

			// Create layout
			return new Layout(handle.getValue(), dev, bindings);
		}

		private final Map<Integer, Binding> bindings;

		/**
		 * Constructor.
		 * @param handle		Layout handle
		 * @param dev			Logical device
		 * @param bindings		Bindings
		 */
		Layout(Pointer handle, LogicalDevice dev, List<Binding> bindings) {
			super(handle, dev, dev.library()::vkDestroyDescriptorSetLayout);
			Check.notEmpty(bindings);
			this.bindings = bindings.stream().collect(toMap(Binding::binding, Function.identity()));
		}

		/**
		 * Looks up a binding descriptor.
		 * @param index Binding index
		 * @return Binding
		 */
		protected Binding binding(int index) {
			return bindings.get(index);
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
