package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout.Binding;
import org.sarge.jove.util.StructureHelper;

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
 * // Create descriptor pool for double-buffered swapchain
 * DescriptorPool pool = new DescriptorPool.Builder(dev)
 *  	.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2)
 *  	.max(2)
 *  	.build();
 *
 * // Create descriptor
 * DescriptorSet descriptor = pool.allocate(layout);
 *
 * // Create a sampler resource
 * Sampler sampler = ...
 * View view = ...
 * DescriptorResource res = sampler.resource(view);
 *
 * // Populate resource
 * descriptor.set(binding, res);
 * DescriptorSet.update(dev, Set.of(descriptor));
 * </pre>
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
	private final Handle handle;
	private final DescriptorLayout layout;
	private final Map<Binding, DescriptorResource> entries = new HashMap<>();
	private final Set<Binding> modified = new HashSet<>();

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	DescriptorSet(Pointer handle, DescriptorLayout layout) {
		this.handle = new Handle(handle);
		this.layout = notNull(layout);
		this.modified.addAll(layout.bindings());
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
	 */
	public void set(Binding binding, DescriptorResource res) {
		// Check binding belongs to this set
		if(!layout.bindings().contains(binding)) {
			throw new IllegalArgumentException(String.format("Invalid binding for this set: binding=%s this=%s", binding, this));
		}

		// Check expected resource type
		if(binding.type() != res.type()) {
			throw new IllegalArgumentException(String.format("Invalid resource for this binding: expected=%s actual=%s", binding.type(), res.type()));
		}

		// Update entry
		entries.put(binding, res);
		modified.add(binding);
	}

	/**
	 * Bulk implementation to sets the resource in a group of descriptor sets for the given binding.
	 * @param sets			Descriptor sets
	 * @param binding		Binding
	 * @param res			Resource
	 * @see #set(ResourceBinding, DescriptorResource)
	 */
	public static void set(Collection<DescriptorSet> sets, Binding binding, DescriptorResource res) {
		for(DescriptorSet ds : sets) {
			ds.set(binding, res);
		}
	}

	/**
	 * Transient modified descriptor set record.
	 */
	private record Modified(DescriptorSet set, Binding binding, DescriptorResource res) {
		/**
		 * Constructor.
		 */
		private Modified {
			if(res == null) throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%s", set, binding));
		}

		/**
		 * Populates a modified descriptor set entry.
		 */
		void populate(VkWriteDescriptorSet write) {
			// Init write descriptor
			write.sType = VkStructureType.WRITE_DESCRIPTOR_SET;
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = set.handle();
			write.descriptorCount = 1;		// Number of elements in resource
			write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

			// Init resource descriptor
			res.populate(write);
		}
	}

	/**
	 * @return Modified entries in this descriptor set
	 */
	private Stream<Modified> modified() {
		return modified
				.stream()
				.map(e -> new Modified(this, e, entries.get(e)));
	}

	/**
	 * Updates the resources for the given descriptor sets.
	 * @param dev				Logical device
	 * @param descriptors		Descriptor sets to update
	 * @return Number of updated descriptor sets
	 */
	public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
		// Enumerate modified sets
		final List<Modified> modified = descriptors
				.stream()
				.flatMap(DescriptorSet::modified)
				.toList();

		// Ignore if nothing to update
		if(modified.isEmpty()) {
			return 0;
		}

		// Apply update
		final VkWriteDescriptorSet[] writes = StructureHelper.array(modified, VkWriteDescriptorSet::new, Modified::populate);
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);

		// Reset updated sets
		modified
				.stream()
				.map(Modified::set)
				.map(e -> e.modified)
				.forEach(Set::clear);

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
				.append(handle)
				.append(entries)
				.append("modified", !modified.isEmpty())
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
