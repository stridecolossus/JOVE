package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;

/**
 * A <i>buffer copy command</i> is used to transfer data between Vulkan buffers.
 * @author Sarge
 */
public final class BufferCopyCommand implements Command {
	/**
	 * Creates a command to copy between the given buffers.
	 * @param src		Source buffer
	 * @param dest		Destination buffer
	 * @return Copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if the given buffers are not a valid source and destination
	 */
	public static BufferCopyCommand of(VulkanBuffer src, VulkanBuffer dest) {
		return new Builder()
				.source(src)
				.destination(dest)
				.region(src.length())
				.build();
	}

	private final VkBufferCopy[] regions;
	private final VulkanBuffer src, dest;

	/**
	 * Constructor.
	 * @param src			Source
	 * @param dest			Destination
	 * @param regions		Copy regions
	 */
	private BufferCopyCommand(VulkanBuffer src, VulkanBuffer dest, VkBufferCopy[] regions) {
		this.src = requireNonNull(src);
		this.dest = requireNonNull(dest);
		this.regions = requireNonNull(regions);
	}

	@Override
	public void execute(VulkanLibrary lib, CommandBuffer buffer) {
		lib.vkCmdCopyBuffer(buffer, src, dest, regions.length, regions);
	}

	/**
	 * Inverts this copy command.
	 * @return Inverse copy command
	 * @throws IllegalStateException if the buffers are not a valid source and destination
	 */
	public Command invert() {
		src.require(VkBufferUsageFlag.TRANSFER_DST);
		dest.require(VkBufferUsageFlag.TRANSFER_SRC);
		return new BufferCopyCommand(dest, src, regions);
	}

	/**
	 * Builder for a buffer copy command.
	 */
	public static class Builder {
		/**
		 * Transient copy region.
		 */
		private record CopyRegion(long srcOffset, long destOffset, long size) {
			private CopyRegion {
				requireZeroOrMore(srcOffset);
				requireZeroOrMore(destOffset);
				requireZeroOrMore(size);
			}

			private void populate(VkBufferCopy copy) {
				copy.srcOffset = srcOffset;
				copy.dstOffset = destOffset;
				copy.size = size;
			}
		}

		private final List<CopyRegion> regions = new ArrayList<>();
		private VulkanBuffer src, dest;

		/**
		 * Sets the source buffer.
		 * @param src Source buffer
		 * @throws IllegalStateException if the buffer is not a copy source
		 */
		public Builder source(VulkanBuffer src) {
			src.require(VkBufferUsageFlag.TRANSFER_SRC);
			this.src = requireNonNull(src);
			return this;
		}

		/**
		 * Sets the destination buffer.
		 * @param dest Destination buffer
		 * @throws IllegalStateException if the buffer is not a copy destination
		 */
		public Builder destination(VulkanBuffer dest) {
			dest.require(VkBufferUsageFlag.TRANSFER_DST);
			this.dest = requireNonNull(dest);
			return this;
		}

		/**
		 * Adds a copy region.
		 * @param srcOffset			Source buffer offset
		 * @param destOffset		Destination buffer offset
		 * @param size				Region length
		 * @throws IllegalArgumentException if the copy region is invalid for either buffer
		 */
		public Builder region(long srcOffset, long destOffset, long size) {
			// Validate
			requireOneOrMore(size);
			src.checkOffset(srcOffset + size - 1);
			dest.checkOffset(destOffset + size - 1);

			// Create copy region descriptor
			regions.add(new CopyRegion(srcOffset, destOffset, size));

			return this;
		}

		/**
		 * Adds a copy region with zero offsets in the buffers.
		 * @param size Region length
		 * @throws IllegalArgumentException if the copy region is invalid for either buffer
		 * @see #region(long, long, long)
		 */
		public Builder region(long size) {
			return region(0, 0, size);
		}

		/**
		 * Constructs this copy command.
		 * @return New buffer copy command
		 * @throws IllegalArgumentException if the buffers have not been populated, are the same object, or no copy regions have been specified
		 */
		public BufferCopyCommand build() {
			// Validate
			requireNonNull(src);
			requireNonNull(dest);
			if(src == dest) throw new IllegalArgumentException("Cannot copy to self");
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");

			// Create copy command
			//final VkBufferCopy[] array = regions.stream().collect(StructureCollector.array(new VkBufferCopy(), CopyRegion::populate));
			final VkBufferCopy[] array = null; // TODO StructureCollector.array(regions, new VkBufferCopy(), CopyRegion::populate);
			return new BufferCopyCommand(src, dest, array);
		}
	}
}
