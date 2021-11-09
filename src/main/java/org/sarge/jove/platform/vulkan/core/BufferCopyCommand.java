package org.sarge.jove.platform.vulkan.core;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsage;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

/**
 * A <i>buffer copy command</i> is used to transfer data between two buffers.
 * @author Sarge
 */
public class BufferCopyCommand implements Command {
	/**
	 * Helper - Creates a command to copy between the given buffers.
	 * @param src		Source buffer
	 * @param dest		Destination buffer
	 * @return New buffer copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if the given buffers are not a valid source and destination
	 */
	public static BufferCopyCommand of(VulkanBuffer src, VulkanBuffer dest) {
		return new Builder()
				.source(src)
				.destination(dest)
				.region(0, 0, src.length())
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
		this.src = notNull(src);
		this.dest = notNull(dest);
		this.regions = notNull(regions);
	}

	@Override
	public void execute(VulkanLibrary lib, Buffer buffer) {
		lib.vkCmdCopyBuffer(buffer, src, dest, regions.length, regions);
	}

	/**
	 * Inverts this copy command.
	 * @return Inverse copy command
	 * @throws IllegalStateException if the buffers are not a valid source and destination
	 */
	public Command invert() {
		src.require(VkBufferUsage.TRANSFER_DST);
		dest.require(VkBufferUsage.TRANSFER_SRC);
		return new BufferCopyCommand(dest, src, regions);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("src", src)
				.append("dest", dest)
				.append("regions", regions.length)
				.build();
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
				Check.zeroOrMore(srcOffset);
				Check.zeroOrMore(destOffset);
				Check.zeroOrMore(size);
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
		 */
		public BufferCopyCommand.Builder source(VulkanBuffer src) {
			src.require(VkBufferUsage.TRANSFER_SRC);
			this.src = notNull(src);
			return this;
		}

		/**
		 * Sets the destination buffer.
		 * @param dest Destination buffer
		 */
		public BufferCopyCommand.Builder destination(VulkanBuffer dest) {
			dest.require(VkBufferUsage.TRANSFER_DST);
			this.dest = notNull(dest);
			return this;
		}

		/**
		 * Adds a copy region.
		 * @param srcOffset			Source buffer offset
		 * @param destOffset		Destination buffer offset
		 * @param size				Region length
		 * @throws IllegalArgumentException if the copy region is invalid for either buffer
		 */
		public BufferCopyCommand.Builder region(long srcOffset, long destOffset, long size) {
			// Validate
			Check.oneOrMore(size);
			validate(src, srcOffset, size);
			validate(dest, destOffset, size);

			// Create copy region descriptor
			regions.add(new CopyRegion(srcOffset, destOffset, size));

			return this;
		}

		private static void validate(VulkanBuffer buffer, long offset, long size) {
			Check.zeroOrMore(offset);
			if(offset + size > buffer.length()) {
				throw new IllegalArgumentException(String.format("Invalid buffer offset: offset=%d size=%d buffer=%s", offset, size, buffer));
			}
		}

		/**
		 * Constructs this copy command.
		 * @return New buffer copy command
		 * @throws IllegalArgumentException if the buffers have not been populated, are the same object, or no copy regions have been specified
		 */
		public BufferCopyCommand build() {
			// Validate
			Check.notNull(src);
			Check.notNull(dest);
			if(src == dest) throw new IllegalArgumentException("Cannot copy to self");
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");

			// Create copy command
			final VkBufferCopy[] array = StructureHelper.array(regions, VkBufferCopy::new, CopyRegion::populate);
			return new BufferCopyCommand(src, dest, array);
		}
	}
}
