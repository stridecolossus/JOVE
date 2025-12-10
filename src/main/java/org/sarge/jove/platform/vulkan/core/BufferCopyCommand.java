package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlags.*;
import static org.sarge.jove.util.Validation.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkBufferCopy;

/**
 * A <i>buffer copy command</i> is used to transfer data between Vulkan buffers.
 * @author Sarge
 */
public class BufferCopyCommand implements Command {
	/**
	 * Creates a command to copy between the given buffers.
	 * @param source			Source buffer
	 * @param destination		Destination buffer
	 * @return Copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if the given buffers are not a valid source and destination
	 */
	public static BufferCopyCommand of(VulkanBuffer source, VulkanBuffer destination) {
		return new Builder()
				.source(source)
				.destination(destination)
				.region(source.length())
				.build();
	}

	private final VkBufferCopy[] regions;
	private final VulkanBuffer source, destination;

	/**
	 * Constructor.
	 * @param source			Source
	 * @param destination		Destination
	 * @param regions			Copy regions
	 * @throws IllegalArgumentException if {@link #regions} is empty
	 */
	private BufferCopyCommand(VulkanBuffer source, VulkanBuffer destination, VkBufferCopy[] regions) {
		if(source == destination) {
			throw new IllegalArgumentException("Cannot copy to self");
		}
		if(regions.length == 0) {
			throw new IllegalArgumentException("No copy regions specified");
		}
		this.source = requireNonNull(source);
		this.destination = requireNonNull(destination);
		this.regions = requireNonNull(regions);
	}

	@Override
	public void execute(Command.Buffer buffer) {
		final VulkanBuffer.Library library = source.device().library();
		library.vkCmdCopyBuffer(buffer, source, destination, regions.length, regions);
	}

	/**
	 * Inverts this copy command.
	 * @return Inverse copy command
	 * @throws IllegalStateException if the buffers are not a valid source and destination
	 */
	public Command invert() {
		source.require(TRANSFER_DST);
		destination.require(TRANSFER_SRC);
		return new BufferCopyCommand(destination, source, regions);
	}

	/**
	 * Builder for a buffer copy command.
	 */
	public static class Builder {
		private final List<VkBufferCopy> regions = new ArrayList<>();
		private VulkanBuffer source, destination;

		/**
		 * Sets the source buffer.
		 * @param source Source buffer
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlag#TRANSFER_SRC}
		 */
		public Builder source(VulkanBuffer source) {
			source.require(TRANSFER_SRC);
			this.source = requireNonNull(source);
			return this;
		}

		/**
		 * Sets the destination buffer.
		 * @param destination Destination buffer
		 * @throws IllegalStateException if the buffer is not a {@link VkBufferUsageFlag#TRANSFER_DST}
		 */
		public Builder destination(VulkanBuffer destination) {
			destination.require(TRANSFER_DST);
			this.destination = requireNonNull(destination);
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
			requireZeroOrMore(srcOffset);
			requireZeroOrMore(destOffset);
			requireOneOrMore(size);
			source.checkOffset(srcOffset + size - 1);
			destination.checkOffset(destOffset + size - 1);

			// Build copy region
			final var copy = new VkBufferCopy();
			copy.srcOffset = srcOffset;
			copy.dstOffset = destOffset;
			copy.size = size;

			// Add to command
			regions.add(copy);

			return this;
		}

		/**
		 * Adds a copy region with zero offsets in the buffers.
		 * @param size Region length
		 * @throws IllegalArgumentException if the copy region is invalid for either buffer
		 * @see #region(long, long, long)
		 */
		public Builder region(long size) {
			return region(0L, 0L, size);
		}

		/**
		 * Constructs this copy command.
		 * @return Buffer copy command
		 * @throws IllegalArgumentException if the buffers have not been populated, are the same object, or no copy regions have been specified
		 */
		public BufferCopyCommand build() {
			final var array = regions.toArray(VkBufferCopy[]::new);
			return new BufferCopyCommand(source, destination, array);
		}
	}
}
