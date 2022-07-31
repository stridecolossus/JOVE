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
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set</i> specifies resources used during rendering, such as samplers and uniform buffers.
 * <p>
 * Example for a texture sampler:
 * <pre>
 * // Define binding for a sampler
 * ResourceBinding binding = new ResourceBinding.Builder()
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
 * // Create descriptors
 * List descriptors = pool.allocate(layout, 2);
 *
 * // Create a descriptor set resource for the sampler
 * View view = ...
 * DescriptorResource res = sampler.resource(view);
 * ...
 *
 * // Set resource
 * DescriptorSet first = descriptors.get(0);
 * first.set(binding, res);
 *
 * // Or bulk set resources in all sets
 * DescriptorSet.set(descriptors, binding, res);
 *
 * // Apply updates
 * DescriptorSet.update(dev, descriptors);
 * </pre>
 * @author Sarge
 */
public class DescriptorSet implements NativeObject {
	private final Handle handle;
	private final DescriptorLayout layout;
	private final Map<ResourceBinding, DescriptorResource> entries = new HashMap<>();
	private final Set<ResourceBinding> modified = new HashSet<>();

	/**
	 * Constructor.
	 * @param handle Descriptor set handle
	 * @param layout Layout
	 */
	DescriptorSet(Pointer handle, DescriptorLayout layout) {
		this.handle = new Handle(handle);
		this.layout = notNull(layout);
		init();
	}

	/**
	 * Initialises all entries as modified.
	 */
	private void init() {
		for(ResourceBinding binding : layout.bindings().values()) {
			entries.put(binding, null);
			modified.add(binding);
		}
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
	public void set(ResourceBinding binding, DescriptorResource res) {
		// Check binding is a member of this descriptor set
		if(!layout.bindings().values().contains(binding)) {
			throw new IllegalArgumentException(String.format("Invalid binding for this set: binding=%s this=%s", binding, this));
		}

		// Check expected resource
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
	public static void set(Collection<DescriptorSet> sets, ResourceBinding binding, DescriptorResource res) {
		for(DescriptorSet ds : sets) {
			ds.set(binding, res);
		}
	}

	/**
	 * Transient modified entry.
	 */
	private record Modified(DescriptorSet set, ResourceBinding binding, DescriptorResource res) {
		/**
		 * Constructor.
		 * @param set			Descriptor set
		 * @param binding		Binding
		 * @param res			Modified resource
		 * @throws IllegalStateException if the resource has not been populated
		 */
		private Modified {
			if(res == null) {
				throw new IllegalStateException(String.format("Resource not populated: set=%s binding=%s", set, binding));
			}
		}

		/**
		 * Populates a modified descriptor set entry.
		 */
		private void populate(VkWriteDescriptorSet write) {
			// Init write descriptor
			write.sType = VkStructureType.WRITE_DESCRIPTOR_SET; // TODO
			write.dstBinding = binding.index();
			write.descriptorType = binding.type();
			write.dstSet = set.handle();
			write.descriptorCount = 1;		// Number of elements in resource
			write.dstArrayElement = 0; 		// TODO - Starting element in the binding?

			// Init resource descriptor
			res.populate(write);
		}

		/**
		 * Marks this modification as updated.
		 */
		private void reset() {
			final var dirty = set.modified;
			assert !dirty.isEmpty();
			dirty.clear();
		}
	}

	/**
	 * @return Modified entries in this descriptor set
	 */
	private Stream<Modified> modified() {
		return entries
				.entrySet()
				.stream()
				.filter(e -> modified.contains(e.getKey()))
				.map(e -> new Modified(this, e.getKey(), e.getValue()));
	}

	/**
	 * Updates the resources for the given descriptor sets.
	 * @param dev				Logical device
	 * @param descriptors		Descriptor sets to update
	 * @return Number of updated descriptor sets
	 */
	public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
		// Enumerate dirty resources
		final List<Modified> dirty = descriptors
				.stream()
				.flatMap(DescriptorSet::modified)
				.toList();

		// Ignore if nothing to update
		if(dirty.isEmpty()) {
			return 0;
		}

		// Apply update
		final VkWriteDescriptorSet[] writes = StructureHelper.array(dirty, VkWriteDescriptorSet::new, Modified::populate);
		dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);

		// Reset updated entries
		for(Modified mod : dirty) {
			mod.reset();
		}

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
