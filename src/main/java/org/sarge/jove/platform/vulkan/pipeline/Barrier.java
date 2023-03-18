package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Structure;

/**
 * A <i>pipeline barrier</i> is a command used to synchronize access to memory resources within the pipeline.
 * <p>
 * A barrier is comprised of memory barriers for one-or-more of the following:
 * <ul>
 * <li>global memory</li>
 * <li>buffers</li>
 * <li>images</li>
 * </ul>
 * The {@link Builder} provides sub-builders for each of these cases.
 * <p>
 * Example for a barrier used to transition an image to be ready for sampling:
 * <pre>
 * Image image = ...
 * Barrier barrier = new Barrier.Builder()
 *     .source(VkPipelineStage.TRANSFER)
 *     .destination(VkPipelineStage.FRAGMENT_SHADER)
 *     .image(image)
 *         oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
 *         newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
 *         source(VkAccess.TRANSFER_WRITE)
 *         destination(VkAccess.SHADER_READ)
 *         build()
 *     .build();</pre>
 * <p>
 * @author Sarge
 */
public final class Barrier implements Command {
	private final BitMask<VkPipelineStage> src, dest;
	private final BitMask<VkDependencyFlag> flags;
	private final VkImageMemoryBarrier[] images;
	private final VkBufferMemoryBarrier[] buffers;
	private final VkMemoryBarrier[] memory;

	/**
	 * Constructor.
	 * @param src			Source pipeline stages
	 * @param dest			Destination pipeline stages
	 * @param flags			Dependency flags
	 * @param memory		Memory barriers
	 * @param buffers		Buffer memory barriers
	 * @param images		Image memory barriers
	 */
	private Barrier(Set<VkPipelineStage> src, Set<VkPipelineStage> dest, Set<VkDependencyFlag> flags, VkMemoryBarrier[] memory, VkBufferMemoryBarrier[] buffers, VkImageMemoryBarrier[] images) {
		this.src = new BitMask<>(src);
		this.dest = new BitMask<>(dest);
		this.flags = new BitMask<>(flags);
		this.memory = memory;
		this.buffers = buffers;
		this.images = images;
	}

	@Override
	public void record(VulkanLibrary lib, Buffer buffer) {
		lib.vkCmdPipelineBarrier(
				buffer,
				src, dest,
				flags,
				length(memory), memory,
				length(buffers), buffers,
				length(images), images
		);
	}

	/**
	 * @return Length of the given array
	 */
	private static int length(Structure[] array) {
		if(array == null) {
			return 0;
		}
		else {
			return array.length;
		}
	}

	/**
	 * Builder for a pipeline barrier.
	 */
	public static class Builder {
		private final Set<VkPipelineStage> srcStages = new HashSet<>();
		private final Set<VkPipelineStage> destStages = new HashSet<>();
		private final Set<VkDependencyFlag> flags = new HashSet<>();
		private final List<MemoryBarrierBuilder<?>> memory = new ArrayList<>();
		private final List<BufferBarrierBuilder> buffers = new ArrayList<>();
		private final List<ImageBarrierBuilder> images = new ArrayList<>();

		/**
		 * Adds a source pipeline stage for this barrier.
		 * @param stage Source pipeline stage
		 */
		public Builder source(VkPipelineStage stage) {
			Check.notNull(stage);
			srcStages.add(stage);
			return this;
		}

		/**
		 * Adds a destination pipeline stage for this barrier.
		 * @param stage Destination pipeline stage
		 */
		public Builder destination(VkPipelineStage stage) {
			Check.notNull(stage);
			destStages.add(stage);
			return this;
		}

		/**
		 * Adds a dependency flag for this barrier.
		 * @param flag Dependency flag
		 */
		public Builder dependency(VkDependencyFlag flag) {
			Check.notNull(flag);
			flags.add(flag);
			return this;
		}

		/**
		 * Starts a memory barrier.
		 * @return Memory barrier builder
		 */
		public MemoryBarrierBuilder<?> memory() {
			return new MemoryBarrierBuilder<>();
		}

		/**
		 * Starts a buffer memory barrier.
		 * @param buffer Vulkan buffer
		 * @return Buffer barrier builder
		 */
		public BufferBarrierBuilder buffer(VulkanBuffer buffer) {
			Check.notNull(buffer);
			return new BufferBarrierBuilder(buffer);
		}

		/**
		 * Starts an image memory barrier.
		 * @param image Vulkan image
		 * @return Image barrier builder
		 */
		public ImageBarrierBuilder image(Image image) {
			Check.notNull(image);
			return new ImageBarrierBuilder(image);
		}

		/**
		 * Constructs this pipeline barrier.
		 * @return New barrier
		 */
		public Barrier build() {
			final var memoryArray = StructureCollector.array(memory, new VkMemoryBarrier(), MemoryBarrierBuilder::populate);
			final var bufferArray = StructureCollector.array(buffers, new VkBufferMemoryBarrier(), BufferBarrierBuilder::populate);
			final var imagesArray = StructureCollector.array(images, new VkImageMemoryBarrier(), ImageBarrierBuilder::populate);
			return new Barrier(srcStages, destStages, flags, memoryArray, bufferArray, imagesArray);
		}

		/**
		 * Builder for a memory barrier.
		 */
		public class MemoryBarrierBuilder<T extends MemoryBarrierBuilder<T>> {
			protected final Set<VkAccess> srcAccess = new HashSet<>();
			protected final Set<VkAccess> destAccess = new HashSet<>();

			protected MemoryBarrierBuilder() {
			}

			/**
			 * Adds a source access flag.
			 * @param flag Source access flag
			 */
			@SuppressWarnings("unchecked")
			public T source(VkAccess flag) {
				Check.notNull(flag);
				srcAccess.add(flag);
				return (T) this;
			}

			/**
			 * Adds a destination access flag.
			 * @param flag Destination access flag
			 */
			@SuppressWarnings("unchecked")
			public T destination(VkAccess flag) {
				Check.notNull(flag);
				destAccess.add(flag);
				return (T) this;
			}

			/**
			 * Populates the descriptor for this memory barrier.
			 */
			private void populate(VkMemoryBarrier barrier) {
				barrier.srcAccessMask = new BitMask<>(srcAccess);
				barrier.dstAccessMask = new BitMask<>(destAccess);
			}

			/**
			 * @return Parent builder
			 */
			public Builder build() {
				memory.add(this);
				return Builder.this;
			}
		}

		/**
		 * Intermediate builder for barriers with queue family ownership transitions.
		 * @param <T> Sub-builder
		 */
		@SuppressWarnings("unchecked")
		private abstract class AbstractBarrierBuilder<T extends AbstractBarrierBuilder<T>> extends MemoryBarrierBuilder<T> {
			protected int srcFamily = Family.IGNORED;
			protected int destFamily = Family.IGNORED;

			/**
			 * Sets the source for a queue family ownership transition.
			 * @param src Source queue family
			 */
			public T source(Family src) {
				this.srcFamily = src.index();
				return (T) this;
			}

			/**
			 * Sets the destination for a queue family ownership transition.
			 * @param dest Destination queue family
			 */
			public T destination(Family dest) {
				this.destFamily = dest.index();
				return (T) this;
			}
		}

		/**
		 * Builder for a buffer memory barrier.
		 */
		public class BufferBarrierBuilder extends AbstractBarrierBuilder<BufferBarrierBuilder> {
			private final VulkanBuffer buffer;
			private long offset;
			private long size = VulkanBuffer.VK_WHOLE_SIZE;

			private BufferBarrierBuilder(VulkanBuffer buffer) {
				this.buffer = notNull(buffer);
			}

			/**
			 * Sets the offset into the buffer memory.
			 * @param offset Buffer offset
			 * @throws IllegalArgumentException if the given offset is larger than the buffers memory
			 */
			public BufferBarrierBuilder offset(long offset) {
				this.offset = zeroOrMore(offset);
				validate(offset + 1);
				return this;
			}

			/**
			 * Sets the size of the backing memory of the buffer to use (default is the whole buffer).
			 * @param size Buffer size (bytes)
			 * @throws IllegalArgumentException if the given size is larger than the buffers memory
			 */
			public BufferBarrierBuilder size(long size) {
				this.size = oneOrMore(size);
				validate(size);
				return this;
			}

			/**
			 * @throws IllegalArgumentException if the given size is larger then the buffers memory
			 */
			private void validate(long size) {
				if(size > buffer.length()) {
					throw new IllegalArgumentException(String.format("Invalid size/offset for buffer memory barrier: size=%d buffer=%s", size, buffer));
				}
			}

			/**
			 * Populates the descriptor for this buffer barrier.
			 */
			private void populate(VkBufferMemoryBarrier barrier) {
				barrier.buffer = buffer.handle();
				barrier.offset = offset;
				barrier.size = size;
				barrier.srcAccessMask = new BitMask<>(srcAccess);
				barrier.dstAccessMask = new BitMask<>(destAccess);
				barrier.srcQueueFamilyIndex = srcFamily;
				barrier.dstQueueFamilyIndex = destFamily;
			}

			/**
			 * @throws IllegalArgumentException if the configured offset and size are invalid for this buffer
			 */
			@Override
			public Builder build() {
				if(size != VulkanBuffer.VK_WHOLE_SIZE) {
					validate(offset + size);
				}
				buffers.add(this);
				return Builder.this;
			}
		}

		/**
		 * Builder for an image barrier.
		 */
		public class ImageBarrierBuilder extends AbstractBarrierBuilder<ImageBarrierBuilder> {
			private final Image image;
			private VkImageLayout oldLayout = VkImageLayout.UNDEFINED;
			private VkImageLayout newLayout;
			private SubResource subresource;

			private ImageBarrierBuilder(Image image) {
				this.image = notNull(image);
				this.subresource = image.descriptor();
			}

			/**
			 * Sets the previous layout.
			 * @param oldLayout Previous layout
			 */
			public ImageBarrierBuilder oldLayout(VkImageLayout oldLayout) {
				this.oldLayout = notNull(oldLayout);
				return this;
			}

			/**
			 * Sets the new layout.
			 * @param newLayout New layout
			 */
			public ImageBarrierBuilder newLayout(VkImageLayout newLayout) {
				this.newLayout = notNull(newLayout);
				return this;
			}

			/**
			 * Sets the image sub-resource.
			 * @param subresource Sub-resource
			 */
			public ImageBarrierBuilder subresource(SubResource subresource) {
				this.subresource = notNull(subresource);
				return this;
			}

			/**
			 * Populates an image barrier descriptor.
			 */
			private void populate(VkImageMemoryBarrier barrier) {
				barrier.image = image.handle();
				barrier.srcAccessMask = new BitMask<>(srcAccess);
				barrier.dstAccessMask = new BitMask<>(destAccess);
				barrier.oldLayout = oldLayout;
				barrier.newLayout = newLayout;
				barrier.subresourceRange = SubResource.toRange(subresource);
				barrier.srcQueueFamilyIndex = srcFamily;
				barrier.dstQueueFamilyIndex = destFamily;
			}

			/**
			 * @throws IllegalArgumentException if the new layout has not been specified or is the same as the previous layout
			 */
			@Override
			public Builder build() {
				if(newLayout == null) throw new IllegalArgumentException("New layout not specified");
				if(newLayout == oldLayout) throw new IllegalArgumentException("Previous and next layouts cannot be the same");
				images.add(this);
				return Builder.this;
			}
		}
	}
}
