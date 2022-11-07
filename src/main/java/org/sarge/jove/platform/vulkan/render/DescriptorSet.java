package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toMap;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout.Binding;
import org.sarge.jove.util.*;

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
 * DescriptorLayout layout = DescriptorLayout.create(List.of(binding, ...));
 *
 * // Create descriptor pool for a double-buffered swapchain
 * DescriptorPool pool = new DescriptorPool.Builder(dev)
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
	 * Resource entry for a given binding.
	 */
	private class ResourceEntry {
		private final Binding binding;
		private DescriptorResource res;
		private boolean dirty = true;

		private ResourceEntry(Binding binding) {
			this.binding = binding;
		}

		/**
		 * Updates this entry and marks it as modified.
		 * @param res Descriptor resource
		 */
		private void set(DescriptorResource res) {
			// Check expected resource type
			if(binding.type() != res.type()) {
				throw new IllegalArgumentException(String.format("Invalid resource for this binding: expected=%s actual=%s", binding.type(), res.type()));
			}

			// Update entry
			this.res = notNull(res);
			this.dirty = true;
		}

		/**
		 * Populates the Vulkan structure for this update.
		 */
		private void populate(VkWriteDescriptorSet write) {
			// Validate
			if(res == null) throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%s", DescriptorSet.this, binding));
			assert dirty;

			// Init write descriptor
			write.sType = VkStructureType.WRITE_DESCRIPTOR_SET;
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = DescriptorSet.this.handle();
			write.descriptorCount = 1;		// Number of elements in resource
			write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

			// Init resource descriptor
			res.populate(write);

			// Mark as updated
			dirty = false;
		}
	}

	private final Handle handle;
	private final DescriptorLayout layout;
	private final Map<Binding, ResourceEntry> entries;

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	DescriptorSet(Handle handle, DescriptorLayout layout) {
		this.handle = notNull(handle);
		this.layout = notNull(layout);
		this.entries = layout.bindings().stream().collect(toMap(Function.identity(), ResourceEntry::new));
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
	 * Sets the resource in this descriptor set for the given binding.
	 * @param binding 	Binding
	 * @param res		Resource
	 * @throws IllegalArgumentException if {@link #binding} does not belong to this descriptor set
	 * @throws IllegalArgumentException if {@link #res} is not the expected type for the binding
	 * @see #set(Collection, Binding, DescriptorResource)
	 */
	public void set(Binding binding, DescriptorResource res) {
		final ResourceEntry entry = entries.get(binding);
		if(entry == null) {
			throw new IllegalArgumentException(String.format("Invalid binding for this set: binding=%s this=%s", binding, this));
		}
		entry.set(res);
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
	 * @return Modified entries for this descriptor set
	 */
	private Stream<ResourceEntry> modified() {
		return entries
				.values()
				.stream()
				.filter(e -> e.dirty);
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
		// Enumerate modified sets
		final var modified = descriptors
				.stream()
				.flatMap(DescriptorSet::modified)
				.toList();

		// Ignore if nothing to update
		if(modified.isEmpty()) {
			return 0;
		}

		// Apply updates
		final VkWriteDescriptorSet[] writes = StructureCollector.array(modified, new VkWriteDescriptorSet(), ResourceEntry::populate);
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);
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
		final PointerArray array = NativeObject.array(sets);
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
		void vkDestroyDescriptorSetLayout(DeviceContext device, DescriptorLayout descriptorSetLayout, Pointer pAllocator);

		/**
		 * Creates a descriptor-set pool.
		 * @param device				Logical device
		 * @param pCreateInfo			Descriptor
		 * @param pAllocator			Allocator
		 * @param pDescriptorPool		Returned pool
		 * @return Result
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
		int vkResetDescriptorPool(DeviceContext device, DescriptorPool descriptorPool, int flags);

		/**
		 * Releases allocated descriptor sets.
		 * @param device				Logical device
		 * @param descriptorPool		Descriptor set pool
		 * @param descriptorSetCount	Number of descriptor sets
		 * @param pDescriptorSets		Descriptor set handles
		 * @return Result
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
