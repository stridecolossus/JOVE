package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import java.util.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier.BarrierType.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>pipeline barrier</i> is a command used to synchronize access to memory resources within the pipeline.
 * <p>
 * A barrier is comprised of memory barriers for one-or-more of the following:
 * <ul>
 * <li>global memory</li>
 * <li>buffers</li>
 * <li>images</li>
 * </ul>
 * @author Sarge
 */
public class Barrier implements Command {
	private final Pipeline.Library library;
	private final EnumMask<VkPipelineStage> src, dest;
	private final EnumMask<VkDependencyFlag> flags;
	private final VkImageMemoryBarrier[] images;
	private final VkBufferMemoryBarrier[] buffers;
	private final VkMemoryBarrier[] memory;

	/**
	 * Constructor.
	 * @param library		Pipeline library
	 * @param source		Source pipeline stages
	 * @param destination	Destination pipeline stages
	 * @param flags			Dependency flags
	 * @param memory		Memory barriers
	 * @param buffers		Buffer memory barriers
	 * @param images		Image memory barriers
	 */
	private Barrier(Pipeline.Library library, Set<VkPipelineStage> source, Set<VkPipelineStage> destination, Set<VkDependencyFlag> flags, VkMemoryBarrier[] memory, VkBufferMemoryBarrier[] buffers, VkImageMemoryBarrier[] images) {
		this.library = requireNonNull(library);
		this.src = new EnumMask<>(source);
		this.dest = new EnumMask<>(destination);
		this.flags = new EnumMask<>(flags);
		this.memory = memory;
		this.buffers = buffers;
		this.images = images;
	}

	@Override
	public void execute(Buffer buffer) {
		library.vkCmdPipelineBarrier(
				buffer,
				src,
				dest,
				flags,
				memory.length,
				memory,
				buffers.length,
				buffers,
				images.length,
				images
		);
	}

	/**
	 * Builder for a barrier.
	 */
	public static class Builder {
		private final Set<VkPipelineStage> source = new HashSet<>();
		private final Set<VkPipelineStage> destination = new HashSet<>();
		private final Set<VkDependencyFlag> flags = new HashSet<>();
		private final List<VkMemoryBarrier> memory = new ArrayList<>();
		private final List<VkBufferMemoryBarrier> buffers = new ArrayList<>();
		private final List<VkImageMemoryBarrier> images = new ArrayList<>();

		/**
		 * Adds a source pipeline stage.
		 * @param stage Source pipeline stage
		 */
		public Builder source(VkPipelineStage stage) {
			source.add(stage);
			return this;
		}

		/**
		 * Adds a destination pipeline stage.
		 * @param stage Destination pipeline stage
		 */
		public Builder destination(VkPipelineStage stage) {
			destination.add(stage);
			return this;
		}

		/**
		 * Adds a dependency flag.
		 * @param flag Dependency flag
		 */
		public Builder flag(VkDependencyFlag flag) {
			flags.add(flag);
			return this;
		}

		/**
		 * Adds a barrier.
		 * @param source			Source access mask
		 * @param destination		Destination access mask
		 * @param barrier			Barrier
		 */
		public Builder add(Set<VkAccess> source, Set<VkAccess> destination, BarrierType barrier) {
			final var src = new EnumMask<>(source);
			final var dest = new EnumMask<>(destination);
			switch(barrier) {
    			case MemoryBarrier mem		-> memory.add(mem.populate(src, dest));
    			case BufferBarrier buffer	-> buffers.add(buffer.populate(src, dest));
    			case ImageBarrier  image	-> images.add(image.populate(src, dest));
    		}
			return this;
		}

		/**
		 * Constructs this barrier.
		 * @param library Pipeline library
		 * @return New barrier
		 */
		public Barrier build(Pipeline.Library library) {
			return new Barrier(
					library,
					source,
					destination,
					flags,
					memory.toArray(VkMemoryBarrier[]::new),
					buffers.toArray(VkBufferMemoryBarrier[]::new),
					images.toArray(VkImageMemoryBarrier[]::new)
			);
		}
	}

	/**
	 * Types of barrier.
	 */
	public sealed interface BarrierType {
		/**
		 * Populates the descriptor for this barrier.
		 * @param srcAccess		Source access flags
		 * @param destAccess	Destination access flags
		 * @return Descriptor for this barrier
		 */
		NativeStructure populate(EnumMask<VkAccess> srcAccess, EnumMask<VkAccess> destAccess);

		/**
		 * Memory barrier.
		 */
		public record MemoryBarrier() implements BarrierType {
			@Override
			public VkMemoryBarrier populate(EnumMask<VkAccess> srcAccess, EnumMask<VkAccess> destAccess) {
				final var barrier = new VkMemoryBarrier();
				barrier.srcAccessMask = srcAccess;
				barrier.dstAccessMask = destAccess;
				return barrier;
			}
    	}

		/**
		 * Buffer barrier.
		 */
    	public record BufferBarrier(VulkanBuffer buffer, long offset, long size, Family source, Family destination) implements BarrierType {
    		/**
    		 * Constructor.
    		 * @param buffer			Buffer
    		 * @param offset			Offset
    		 * @param size				Buffer length or {@link VulkanBuffer#VK_WHOLE_SIZE} for the entire buffer
    		 * @param source			Source queue family
    		 * @param destination		Destination queue family
    		 * @throws IllegalArgumentException if the {@link #offset} or {@link #size} are too large for the buffer
    		 * @see VulkanBuffer#checkOffset(long)
    		 */
    		public BufferBarrier {
    			requireNonNull(buffer);
    			requireZeroOrMore(offset);
    			requireZeroOrMore(size);
    			requireNonNull(source);
    			requireNonNull(destination);

    			buffer.checkOffset(offset);			// TODO - was offset + 1 ???

    			if(size != VulkanBuffer.VK_WHOLE_SIZE) {
    				buffer.checkOffset(offset + size);
    			}
    		}

    		/**
    		 * Convenience constructor for the whole of the buffer.
    		 * @param buffer Buffer
    		 * @see VulkanBuffer#VK_WHOLE_SIZE
    		 * @see Family#IGNORED
    		 */
    		public BufferBarrier(VulkanBuffer buffer) {
    			this(buffer, 0L, VulkanBuffer.VK_WHOLE_SIZE, Family.IGNORED, Family.IGNORED);
    		}

    		@Override
			public VkBufferMemoryBarrier populate(EnumMask<VkAccess> srcAccess, EnumMask<VkAccess> destAccess) {
				final var barrier = new VkBufferMemoryBarrier();
				barrier.buffer = buffer.handle();
				barrier.offset = offset;
				barrier.size = size;
				barrier.srcAccessMask = srcAccess;
				barrier.dstAccessMask = destAccess;
				barrier.srcQueueFamilyIndex = source.index();
				barrier.dstQueueFamilyIndex = destination.index();
				return barrier;
			}
    	}

    	/**
    	 * Image barrier.
    	 */
    	public record ImageBarrier(Image image, Subresource subresource, VkImageLayout oldLayout, VkImageLayout newLayout, Family source, Family destination) implements BarrierType {
    		/**
    		 * Constructor.
    		 * @param image				Image
    		 * @param subresource		Subresource
    		 * @param oldLayout			Old layout
    		 * @param newLayout			New layout
    		 * @param source			Source family
    		 * @param destination		Destination family
    		 * @throws IllegalArgumentException if the old and new layouts are the same
    		 */
    		public ImageBarrier {
    			requireNonNull(image);
    			requireNonNull(subresource);
    			requireNonNull(oldLayout);
    			requireNonNull(newLayout);
    			requireNonNull(source);
    			requireNonNull(destination);

    			if(newLayout == oldLayout) {
					throw new IllegalArgumentException("Previous and next layouts cannot be the same");
				}
    		}

    		/**
    		 * Convenience constructor with minimal configuration.
    		 * @param image			Image and subresource
    		 * @param oldLayout		Old layout
    		 * @param newLayout		New layout
    		 * @throws IllegalArgumentException if the old and new layouts are the same
    		 * @see Family#IGNORED
    		 */
    		public ImageBarrier(Image image, VkImageLayout oldLayout, VkImageLayout newLayout) {
    			this(image, image.descriptor(), oldLayout, newLayout, Family.IGNORED, Family.IGNORED);
    		}

    		@Override
    		public VkImageMemoryBarrier populate(EnumMask<VkAccess> srcAccess, EnumMask<VkAccess> destAccess) {
				final var barrier = new VkImageMemoryBarrier();
				barrier.image = image.handle();
				barrier.srcAccessMask = srcAccess;
				barrier.dstAccessMask = destAccess;
				barrier.oldLayout = oldLayout;
				barrier.newLayout = newLayout;
				barrier.subresourceRange = Subresource.range(subresource);
				barrier.srcQueueFamilyIndex = source.index();
				barrier.dstQueueFamilyIndex = destination.index();
				return barrier;
			}
    	}
	}
}
