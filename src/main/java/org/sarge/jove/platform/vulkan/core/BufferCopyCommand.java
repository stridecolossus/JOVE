package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;

/**
 * A <i>buffer copy command</i> is used to transfer data between Vulkan buffers.
 * @author Sarge
 */
public class BufferCopyCommand implements Command {
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
	 * @throws IllegalArgumentException if {@link #regions} is empty
	 */
	private BufferCopyCommand(VulkanBuffer src, VulkanBuffer dest, VkBufferCopy[] regions) {
		if(regions.length == 0) {
			throw new IllegalArgumentException("No copy regions specified");
		}
		this.src = requireNonNull(src);
		this.dest = requireNonNull(dest);
		this.regions = requireNonNull(regions);
	}

	@Override
	public void execute(Command.Buffer buffer) {
		final VulkanBuffer.Library library = src.device().library();
		library.vkCmdCopyBuffer(buffer, src, dest, regions.length, regions);
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

			private VkBufferCopy build() {
				final var copy = new VkBufferCopy();
				copy.srcOffset = srcOffset;
				copy.dstOffset = destOffset;
				copy.size = size;
				return copy;
			}
		}

		private final List<CopyRegion> regions = new ArrayList<>();
		private VulkanBuffer src, dest;

		/**
		 * Sets the source buffer.
		 * @param src Source buffer
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlag#TRANSFER_SRC}
		 */
		public Builder source(VulkanBuffer src) {
			src.require(VkBufferUsageFlag.TRANSFER_SRC);
			this.src = requireNonNull(src);
			return this;
		}

		/**
		 * Sets the destination buffer.
		 * @param dest Destination buffer
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlag#TRANSFER_DST}
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
			if(src == dest) {
				throw new IllegalArgumentException("Cannot copy to self");
			}

			// Build copy regions array
			final var array = regions
					.stream()
					.map(CopyRegion::build)
					.toArray(VkBufferCopy[]::new);

			// Create copy command
			return new BufferCopyCommand(src, dest, array);
		}
	}
}
