package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.sarge.jove.material.Shader;
import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.vulkan.ImageView.VulkanSampler;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictList;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> defines resources (uniform buffers, samplers, etc) used by a {@link Shader}.
 * @author Sarge
 */
public class DescriptorSet extends PointerHandle {
	private final Layout layout;

	/**
	 * Constructor.
	 * @param handle 		Handle
	 * @param layout		Layout
	 */
	protected DescriptorSet(Pointer handle, Layout layout) {
		super(handle);
		this.layout = notNull(layout);
	}

	public Layout layout() {
		return layout;
	}

	/**
	 * @return Command to bind this descriptor set
	 */
	public Command bind(Pipeline.Layout layout) {
		return (lib, cmd) -> lib.vkCmdBindDescriptorSets(cmd, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, layout.handle(), 0, 1, new Pointer[]{super.handle()}, 0, null);
	}
	// TODO - refers to pipeline.layout! maybe factor this out somehow?

	/**
	 * Updates this descriptor set with the given uniform buffer.
	 * @param binding		Binding index
	 * @param buffer		Uniform buffer
	 * @param offset		Offset
	 * @param size			Buffer size
	 * @throws IllegalArgumentException if this descriptor set is not a uniform buffer
	 */
	public void uniform(int binding, DataBuffer buffer, int offset, long size) {
		final VkDescriptorBufferInfo info = new VkDescriptorBufferInfo();
		info.buffer = ((PointerHandle) buffer).handle(); // TODO - nasty!
		info.offset = zeroOrMore(offset);
		info.range = oneOrMore(size);
		update(binding, VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, write -> write.pBufferInfo = info);
	}

	/**
	 * TODO
	 * @param binding
	 * @param view
	 * @param sampler
	 */
	public void sampler(int binding, VulkanSampler sampler) {
		final VkDescriptorImageInfo info = new VkDescriptorImageInfo();
		info.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
		info.imageView = sampler.view().handle();
		info.sampler = sampler.handle();
		update(binding, VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, write -> write.pImageInfo = info);
	}

	/**
	 *
	 * @param id			Binding ID
	 * @param type			Expected descriptor type
	 * @param updater		Update callback
	 */
	private void update(int id, VkDescriptorType type, Consumer<VkWriteDescriptorSet> updater) {
		// Lookup layout binding
		// TODO - lookup map
		final VkDescriptorSetLayoutBinding binding = layout.bindings.stream().filter(b -> b.binding == id).findAny().orElseThrow(() -> new IllegalArgumentException("Binding not present: " + id));

		// Validate
		if(type != binding.descriptorType) throw new IllegalArgumentException(String.format("Incorrect descriptor set update: type=%s expected=%s", type, binding.descriptorType));

		// Init update descriptor
		final VkWriteDescriptorSet write = new VkWriteDescriptorSet();
		write.dstSet = super.handle();
		write.dstBinding = binding.binding;
		write.descriptorType = binding.descriptorType;
		write.descriptorCount = binding.descriptorCount;
		write.dstArrayElement = 0; // TODO

//		write.pBufferInfo = null;
//		write.pImageInfo = null;
//		write.pTexelBufferView = null;

		// Populate appropriate field
		updater.accept(write);

		// Update descriptor set
		final LogicalDevice dev = layout.device();
		final VulkanLibraryDescriptorSet lib = dev.vulkan().library();
		lib.vkUpdateDescriptorSets(dev.handle(), 1, new VkWriteDescriptorSet[]{write}, 0, null);
	}
	// TODO - back to updater approach to group updates into one call

	/**
	 * A <i>descriptor set layout</i> defines the structure of a set resource descriptors that can be bound to a {@link Pipeline}.
	 */
	public static class Layout extends LogicalDeviceHandle {
		private final List<VkDescriptorSetLayoutBinding> bindings;

		/**
		 * Constructor.
		 * @param handle 		Layout handle
		 * @param dev			Logical device
		 * @param bindings		Binding descriptors
		 */
		Layout(Pointer handle, LogicalDevice dev, List<VkDescriptorSetLayoutBinding> bindings) {
			super(handle, dev, lib -> lib::vkDestroyDescriptorSetLayout);
			this.bindings = List.copyOf(bindings);
		}

		/**
		 * @return Binding descriptors
		 */
		public List<VkDescriptorSetLayoutBinding> bindings() {
			return bindings;
		}

		/**
		 * Builder for a descriptor set layout.
		 * <p>
		 * Usage:
		 * <pre>
		 * Layout layout = new Builder(dev)
		 * 		// Add a uniform buffer
		 * 		.binding(1)
		 * 			.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
		 *			.stage(VkShaderStageFlag.VK_SHADER_STAGE_ALL)
		 *		// Add a sampler (at binding index 2)
		 *		.binding()
		 *			.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_SAMPLER)
		 *			.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
		 *			.size(2)
		 *      .build();
		 * </pre>
		 */
		public static class Builder {
			private final LogicalDevice dev;

			private final List<VkDescriptorSetLayoutBinding> bindings = new ArrayList<>();

			private VkDescriptorSetLayoutBinding current;
			private int next;

			/**
			 * Constructor.
			 * @param dev Logical device
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Starts a new layout binding.
			 * @param binding Binding index
			 * @throws IllegalArgumentException if a binding index is a duplicate (assumes bindings are declared in ascending index order)
			 */
			public Builder binding(int binding) {
				// Validate
				if(binding < next) throw new IllegalArgumentException("Invalid or duplicate binding index: " + binding);
				if(current != null) {
					verify(current);
				}

				// Init binding
				current = new VkDescriptorSetLayoutBinding();
				current.binding = binding;
				current.descriptorCount = 1;

				// Add binding
				bindings.add(current);
				next = binding + 1;
				return this;
			}

			/**
			 * Verifies a descriptor set binding.
			 */
			private static void verify(VkDescriptorSetLayoutBinding binding) {
				if(binding.descriptorType == null) throw new IllegalArgumentException("Binding requires a descriptor type: " + binding.binding);
				if(binding.stageFlags == 0) throw new IllegalArgumentException("No shader stages specified: " + binding.binding);
			}

			/**
			 * Starts a new layout binding at the <i>next</i> binding index.
			 */
			public Builder binding() {
				return binding(next);
			}

			/**
			 * Sets the type of this layout binding.
			 * @param type Type
			 */
			public Builder type(VkDescriptorType type) {
				current.descriptorType = type;
				return this;
			}

			/**
			 * Sets the size of this layout binding  (for an array type).
			 * @param size Array size
			 */
			public Builder size(int size) {
				current.descriptorCount = size;
				return this;
			}

			/**
			 * Adds a shader stage for this layout binding.
			 * @param stage Shader stage
			 */
			public Builder stage(VkShaderStageFlag stage) {
				current.stageFlags |= stage.value();
				return this;
			}

			/**
			 * Constructs this layout.
			 * @return New descriptor set layout
			 */
			public Layout build() {
				// Validate bindings
				if(current == null) throw new IllegalArgumentException("No layout bindings specified");
				verify(current);

				// Init layout descriptor
				final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
				info.bindingCount = bindings.size();
				info.pBindings = StructureHelper.structures(bindings);

				// Allocate layout
				final Vulkan vulkan = dev.vulkan();
				final VulkanLibraryDescriptorSet lib = vulkan.library();
				final PointerByReference layout = vulkan.factory().reference();
				check(lib.vkCreateDescriptorSetLayout(dev.handle(), info, null, layout));

				// Create layout
				return new Layout(layout.getValue(), dev, bindings);
			}
		}
	}

	/**
	 * A <i>descriptor set pool</i> allocates and manages descriptor sets.
	 */
	public static class Pool extends LogicalDeviceHandle {
		private final int max;

		/**
		 * Constructor.
		 * @param handle 		Pool handle
		 * @param dev			Logical device
		 * @param max			Maximum number of descriptor sets
		 */
		Pool(Pointer handle, LogicalDevice dev, int max) {
			super(handle, dev, lib -> lib::vkDestroyDescriptorPool);
			this.max = oneOrMore(max);
		}

		/**
		 * @return Maximum number of descriptor sets
		 */
		public int max() {
			return max;
		}

		/**
		 * Allocates a number of descriptor sets from this pool.
		 * @param layouts Descriptor set layout(s)
		 * @return Descriptor sets
		 */
		public List<DescriptorSet> allocate(List<Layout> layouts) {
			// Build allocation descriptor
			final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
			info.descriptorPool = super.handle();
			info.descriptorSetCount = layouts.size();

			// Populate layouts
			final var pointers = layouts.stream().map(Layout::handle).collect(toList());
			info.pSetLayouts = StructureHelper.pointers(pointers);

			// Allocate descriptor sets
			final LogicalDevice dev = super.device();
			final Vulkan vulkan = dev.vulkan();
			final VulkanLibraryDescriptorSet lib = vulkan.library();
			final Pointer[] array = vulkan.factory().pointers(layouts.size());
			check(lib.vkAllocateDescriptorSets(dev.handle(), info, array));

			// Create descriptor sets
			final List<DescriptorSet> sets = new ArrayList<>();
			for(int n = 0; n < layouts.size(); ++n) {
				final DescriptorSet ds = new DescriptorSet(array[n], layouts.get(0));
				sets.add(ds);
			}
			return sets;
		}

		/**
		 * Resets this descriptor set pool.
		 */
		public void reset() {
			final LogicalDevice dev = super.device();
			final VulkanLibraryDescriptorSet lib = dev.vulkan().library();
			check(lib.vkResetDescriptorPool(dev.handle(), super.handle(), 0));
		}

		/**
		 * Releases descriptor sets and resources back to this pool.
		 * @param descriptors Descriptors to release
		 */
		public void free(Collection<DescriptorSet> descriptors) {
			final LogicalDevice dev = super.device();
			final Pointer[] array = descriptors.stream().map(Handle::handle).toArray(Pointer[]::new);
			final VulkanLibraryDescriptorSet lib = dev.vulkan().library();
			check(lib.vkFreeDescriptorSets(dev.handle(), super.handle(), array.length, array));
		}

		/**
		 * Builder for a descriptor set pool.
		 */
		public static class Builder {
			private final LogicalDevice dev;
			private final List<VkDescriptorPoolSize> entries = new StrictList<>();
			private int flags;
			private int max = 1;

			/**
			 * Constructor.
			 * @param dev Logical device
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Adds a descriptor type that this pool will contain.
			 * @param count			Number of this type
			 * @param type			Descriptor type
			 */
			public Builder add(int count, VkDescriptorType type) {
				final VkDescriptorPoolSize entry = new VkDescriptorPoolSize();
				entry.descriptorCount = oneOrMore(count);
				entry.type = notNull(type);
				entries.add(entry);
				return this;
			}

			/**
			 * Sets the maximum number of descriptors that are available.
			 * @param max Maximum number of descriptors
			 */
			public Builder max(int max) {
				this.max = oneOrMore(max);
				return this;
			}

			/**
			 * Adds a create flag for this pool.
			 * @param flag Pool create flag
			 */
			public Builder flag(VkDescriptorPoolCreateFlag flag) {
				this.flags |= flag.value();
				return this;
			}

			/**
			 * Constructs this pool.
			 * @return New descriptor pool
			 * @throws IllegalArgumentException if no descriptor types have been added
			 * @throws IllegalArgumentException if the maximum number of descriptor sets is not valid
			 * @see #add(int, VkDescriptorType)
			 * @see #max(int)
			 */
			public Pool build() {
				// Validate
				if(entries.isEmpty()) throw new IllegalArgumentException("No descriptor types specified");
				if(max < entries.size()) throw new IllegalArgumentException(""); // TODO

				// Init pool descriptor
				final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
				info.flags = flags;
				info.poolSizeCount = entries.size();
				info.pPoolSizes = StructureHelper.structures(entries);
				info.maxSets = max;

				// Allocate pool
				final Vulkan vulkan = dev.vulkan();
				final VulkanLibraryDescriptorSet lib = vulkan.library();
				final PointerByReference pool = vulkan.factory().reference();
				check(lib.vkCreateDescriptorPool(dev.handle(), info, null, pool));

				// Create pool
				return new Pool(pool.getValue(), dev, max);
			}
		}
	}
}
