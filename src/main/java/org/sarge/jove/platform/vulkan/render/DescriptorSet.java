package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkCopyDescriptorSet;
import org.sarge.jove.platform.vulkan.VkDescriptorBufferInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorImageInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetAllocateInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkWriteDescriptorSet;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
	 * An <i>entry</i> records the resource for each binding in this descriptor set.
	 */
	private class Entry {
		private final Binding binding;
		private boolean dirty = true;
		private DescriptorResource res;

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
		 * Marks this entry as updated.
		 */
		void clear() {
			assert dirty;
			dirty = false;
		}

		/**
		 * Populates the given descriptor set update record from this entry.
		 * @param write Descriptor set update record
		 */
		private void populate(VkWriteDescriptorSet write) {
			// Validate
			final DescriptorSet set = DescriptorSet.this;
			if(res == null) {
				throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%d", set, binding.index()));
			}

			// Init write descriptor
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = set.handle();
			write.descriptorCount = 1;		// Number of elements in resource
			write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

			// Init resource descriptor
			final Structure info = res.populate();
			if(info instanceof VkDescriptorBufferInfo buffer) {
				write.pBufferInfo = buffer;
			}
			else
			if(info instanceof VkDescriptorImageInfo image) {
				write.pImageInfo = image;
			}
			else {
				throw new UnsupportedOperationException("Unsupported descriptor resource: " + info.getClass());
			}
		}
	}

	private final Handle handle;
	private final DescriptorLayout layout;
	private final Map<Binding, Entry> entries;

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	public DescriptorSet(Handle handle, DescriptorLayout layout) {
		this.handle = notNull(handle);
		this.layout = notNull(layout);
		this.entries = layout.bindings().values().stream().collect(toMap(Function.identity(), Entry::new));
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Layout for this descriptor set
	 */
	public DescriptorLayout layout() {
		return layout;
	}

	/**
	 * @param binding Binding descriptor
	 * @return Entry for this given binding
	 */
	private Entry entry(Binding binding) {
		final Entry entry = entries.get(binding);
		if(entry == null) throw new IllegalArgumentException(String.format("Invalid binding for this descriptor set: binding=%s set=%s", binding, this));
		return entry;
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
	 * @see #set(Binding, DescriptorResource)
	 */
	public DescriptorResource resource(Binding binding) {
		final Entry entry = entry(binding);
		return entry.res;
	}

	/**
	 * Sets the resource in this descriptor set for the given binding.
	 * @param binding 	Binding
	 * @param res		Resource
	 * @throws IllegalArgumentException if the binding does not belong to the layout of this descriptor set
	 * @see #set(Collection, Binding, DescriptorResource)
	 */
	public void set(Binding binding, DescriptorResource res) {
		final Entry entry = entry(binding);
		if(binding.type() != res.type()) {
			throw new IllegalArgumentException(String.format("Invalid resource for this binding: expected=%s actual=%s", binding.type(), res.type()));
		}
		entry.res = notNull(res);
		entry.dirty = true;
	}

	/**
	 * Helper - Bulk implementation of the {@link #set(Binding, DescriptorResource)} method.
	 * @param sets			Descriptor sets
	 * @param binding		Binding
	 * @param res			Resource
	 * @throws IllegalArgumentException if the binding does not belong to the layout of all descriptor sets
	 */
	public static void set(Collection<DescriptorSet> sets, Binding binding, DescriptorResource res) {
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
				.peek(Entry::clear)
				.collect(StructureHelper.collector(VkWriteDescriptorSet::new, Entry::populate));

		// Ignore if nothing to update
		if((writes == null) || (writes.length == 0)) {
			return 0;
		}

		// Apply update
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);

		return writes.length;
	}

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
				NativeObject.array(sets),
				0,					// Dynamic offset count
				null				// Dynamic offsets
		);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(entries.values())
				.build();
	}

	/**
	 * Descriptor sets API.
	 */
	interface Library {
		/**
		 * Creates a descriptor set layout.
		 * @param device				Logical device
		 * @param pCreateInfo			Create descriptor
		 * @param pAllocator			Allocator
		 * @param pSetLayout			Returned layout handle
		 * @return Result code
		 */
		int vkCreateDescriptorSetLayout(DeviceContext device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);

		/**
		 * Destroys a descriptor set layout.
		 * @param device				Logical device
		 * @param descriptorSetLayout	Layout
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorSetLayout(DeviceContext device, DescriptorLayout descriptorSetLayout, Pointer pAllocator);

		/**
		 * Creates a descriptor-set pool.
		 * @param device				Logical device
		 * @param pCreateInfo			Descriptor
		 * @param pAllocator			Allocator
		 * @param pDescriptorPool		Returned pool
		 * @return Result code
		 */
		int vkCreateDescriptorPool(DeviceContext device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);

		/**
		 * Destroys a descriptor-set pool.
		 * @param device				Logical device
		 * @param descriptorPool		Pool
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorPool(DeviceContext device, DescriptorPool descriptorPool, Pointer pAllocator);

		/**
		 * Allocates a number of descriptor sets from a given pool.
		 * @param device				Logical device
		 * @param pAllocateInfo			Allocation descriptor
		 * @param pDescriptorSets		Returned descriptor set handles
		 * @return Result code
		 */
		int vkAllocateDescriptorSets(DeviceContext device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);

		/**
		 * Resets all descriptor sets in the given pool, i.e. recycles the resources back to the pool and releases the descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param flags					Unused
		 * @return Result code
		 */
		int vkResetDescriptorPool(DeviceContext device, DescriptorPool descriptorPool, int flags);

		/**
		 * Releases allocated descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param descriptorSetCount	Number of descriptor sets
		 * @param pDescriptorSets		Descriptor set handles
		 * @return Result code
		 */
		int vkFreeDescriptorSets(DeviceContext device, DescriptorPool descriptorPool, int descriptorSetCount, Pointer pDescriptorSets);

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
