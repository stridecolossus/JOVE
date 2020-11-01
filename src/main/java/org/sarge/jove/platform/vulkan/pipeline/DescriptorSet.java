package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
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
import org.sarge.jove.platform.vulkan.util.Resource;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
 *  // Update descriptor set with a resource
 *  Resource res = ...
 *  set.update(binding, res).apply();
 *
 *  // Update a group of sets
 *  new UpdateBuilder()
 *  	.add(set, binding, res)
 *  	...
 *  	.apply(dev);
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
	 * Creates an update comprising multiple resources for this descriptor set.
	 * @param <T> Resource type
	 * @param binding 		Binding
	 * @param res 			Resources to update
	 * @return New update
	 */
	public <T extends Structure> Update<T> update(Layout.Binding binding, Collection<Resource<T>> res) {
		return new Update<>(binding, res);
	}

	/**
	 * Creates an update for this descriptor set.
	 * @param <T> Resource type
	 * @param binding 		Binding
	 * @param res 			Resource to update
	 * @return New update
	 */
	public <T extends Structure> Update<T> update(Layout.Binding binding, Resource<T> res) {
		return update(binding, Set.of(res));
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
				info.pPoolSizes = VulkanStructure.populate(VkDescriptorPoolSize::new, entries.entrySet(), Builder::populate);
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
	 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
	 * @see Resource
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
			info.pBindings = VulkanStructure.populate(VkDescriptorSetLayoutBinding::new, bindings, Binding::populate);

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

	/**
	 * A <i>descriptor set update</i> initialises the resources of this descriptor.
	 * @param <T> Resource type
	 * @see Resource
	 */
	public class Update<T extends Structure> {
		private final Layout.Binding binding;
		private final Collection<Resource<T>> res;

		/**
		 * Constructor.
		 * @param binding			Binding
		 * @param resources			Resources to updates
		 * @throws IllegalArgumentException if the resources are empty, the binding is invalid for the given descriptor set, or the descriptor type does not match the resource
		 */
		private Update(Layout.Binding binding, Collection<Resource<T>> resources) {
			// Validate
			if(resources.isEmpty()) throw new IllegalArgumentException("Empty updates");
			if(!layout.bindings.containsValue(binding)) throw new IllegalArgumentException("Invalid binding for descriptor set");

			// Validate updates
			for(Resource<?> res : resources) {
				if(res.type() != binding.type) {
					throw new IllegalArgumentException(String.format("Invalid descriptor type: expected=%s actual=%s", binding.type, res.type()));
				}
			}

			this.binding = notNull(binding);
			this.res = Set.copyOf(resources);
		}

		/**
		 * Applies this update.
		 */
		public void apply() {
			final var set = DescriptorSet.this;
			new UpdateBuilder()
					.add(set, binding, res)
					.apply(set.layout().device());
		}

		/**
		 * Populates the given write descriptor.
		 * @param write Write descriptor
		 */
		void populate(VkWriteDescriptorSet write) {
			// Init write descriptor
			write.dstBinding = binding.binding;
			write.descriptorType = binding.type;
			write.dstSet = DescriptorSet.this.handle();
			write.dstArrayElement = 0;

			// Add resource array
			final Resource<T> instance = res.iterator().next();
			final T array = VulkanStructure.populate(instance.identity(), res, Resource::populate);
			instance.apply(array, write);
			write.descriptorCount = res.size();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("binding", binding)
					.append("updates", res.size())
					.build();
		}
	}

	/**
	 * An <i>update builder</i> aggregates and applies a group of updates as a single API invocation.
	 */
	public static class UpdateBuilder {
		private final List<Update<?>> updates = new ArrayList<>();

		/**
		 * Adds a group of updates to the given descriptor sets.
		 * @param <T> Resource type
		 * @param sets			Descriptor sets to update
		 * @param binding		Binding
		 * @param res			Resources
		 */
		public <T extends Structure> UpdateBuilder add(Collection<DescriptorSet> sets, Binding binding, Collection<Resource<T>> res) {
			for(DescriptorSet set : sets) {
				final Update<?> update = set.update(binding, res);
				updates.add(update);
			}
			return this;
		}

		/**
		 * Adds an update to the given descriptor sets.
		 * @param <T> Resource type
		 * @param sets			Descriptor sets to update
		 * @param binding		Binding
		 * @param res			Resource
		 */
		public <T extends Structure> UpdateBuilder add(Collection<DescriptorSet> sets, Binding binding, Resource<T> res) {
			return add(sets, binding, List.of(res));
		}

		/**
		 * Adds a group of updates to the given descriptor set.
		 * @param <T> Resource type
		 * @param set			Descriptor set to update
		 * @param binding		Binding
		 * @param res			Resources
		 */
		public <T extends Structure> UpdateBuilder add(DescriptorSet set, Binding binding, Collection<Resource<T>> res) {
			return add(List.of(set), binding, res);
		}

		/**
		 * Adds an update to the given descriptor set.
		 * @param <T> Resource type
		 * @param set			Descriptor set to update
		 * @param binding		Binding
		 * @param res			Resource
		 */
		public <T extends Structure> UpdateBuilder add(DescriptorSet set, Binding binding, Resource<T> res) {
			return add(set, binding, List.of(res));
		}

		/**
		 * Applies a set of descriptor set updates.
		 * @param dev Logical device
		 * @throws IllegalArgumentException if no updates have been added
		 */
		public void apply(LogicalDevice dev) {
			if(updates.isEmpty()) throw new IllegalArgumentException("Empty updates");
			final var array = VulkanStructure.populateArray(VkWriteDescriptorSet::new, updates, Update::populate);
			dev.library().vkUpdateDescriptorSets(dev.handle(), array.length, array, 0, null);
		}
	}
}
