package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkAccessFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineStageFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.Queue;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.platform.vulkan.util.ImageSubResourceBuilder;
import org.sarge.jove.util.Check;

/**
 * A <i>pipeline barrier</i> is used to synchronize access to resources within a pipeline or to perform image layout transitions.
 * @author Sarge
 */
public class Barrier extends ImmediateCommand {
	private final int src, dest;
	private final VkImageMemoryBarrier[] images;

	/**
	 * Constructor.
	 * @param src			Source pipeline stages
	 * @param dest			Destination pipeline stages
	 * @param images		Image memory barriers
	 */
	public Barrier(Set<VkPipelineStageFlag> src, Set<VkPipelineStageFlag> dest, List<VkImageMemoryBarrier> images) {
		this.src = IntegerEnumeration.mask(src);
		this.dest = IntegerEnumeration.mask(dest);
		this.images = images.toArray(VkImageMemoryBarrier[]::new);
	}

	@Override
	public void execute(VulkanLibrary lib, Handle handle) {
		// TODO - others
		lib.vkCmdPipelineBarrier(handle, src, dest, 0, 0, null, 0, null, images.length, images);
	}

	/**
	 * Builder for a pipeline barrier.
	 */
	public static class Builder {
		private final Set<VkPipelineStageFlag> srcStages = new HashSet<>();
		private final Set<VkPipelineStageFlag> destStages = new HashSet<>();
		private final List<VkImageMemoryBarrier> images = new ArrayList<>();
		// TODO
		// - buffer memory barriers
		// - memory barriers

		/**
		 * Adds a source pipeline stage for this barrier.
		 * @param stage Source pipeline stage
		 */
		public Builder source(VkPipelineStageFlag stage) {
			Check.notNull(stage);
			srcStages.add(stage);
			return this;
		}

		/**
		 * Adds a destination pipeline stage for this barrier.
		 * @param stage Destination pipeline stage
		 */
		public Builder destination(VkPipelineStageFlag stage) {
			Check.notNull(stage);
			destStages.add(stage);
			return this;
		}

		/**
		 * Starts a memory barrier for the given image.
		 * @return New builder for an image barrier
		 */
		public ImageBarrierBuilder barrier(Image image) {
			return new ImageBarrierBuilder(image);
		}

		/**
		 * Constructs this pipeline barrier.
		 * @return New barrier
		 */
		public Barrier build() {
			if(images.isEmpty()) throw new IllegalArgumentException("Barrier is empty");
			return new Barrier(srcStages, destStages, images);
		}

		/**
		 * Builder for an image transition barrier.
		 */
		public class ImageBarrierBuilder {
			private final Image image;
			private final Set<VkAccessFlag> src = new HashSet<>();
			private final Set<VkAccessFlag> dest = new HashSet<>();
			private VkImageLayout oldLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
			private VkImageLayout newLayout;
			private final ImageSubResourceBuilder<ImageBarrierBuilder> subresource = new ImageSubResourceBuilder<>(this);

			/**
			 * Constructor.
			 * @param image Image
			 */
			ImageBarrierBuilder(Image image) {
				this.image = notNull(image);
			}

			/**
			 * Adds a source access flag.
			 * @param access Source access flag
			 */
			public ImageBarrierBuilder source(VkAccessFlag flag) {
				src.add(flag);
				return this;
			}

			/**
			 * Adds a destination access flag.
			 * @param destStages Destination access flag
			 */
			public ImageBarrierBuilder destination(VkAccessFlag flag) {
				dest.add(flag);
				return this;
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
			 * @return Builder for the image sub-resource range
			 */
			public ImageSubResourceBuilder<ImageBarrierBuilder> subresource() {
				return subresource;
			}
			// TODO - check vs parent image

			/**
			 * Constructs this image memory barrier.
			 * @return New image memory barrier
			 */
			public Builder build() {
				// Validate
				if(newLayout == null) throw new IllegalArgumentException("New layout not specified");
				if(newLayout == oldLayout) throw new IllegalArgumentException("Previous and next layouts cannot be the same");

				// Create descriptor
				final VkImageMemoryBarrier barrier = new VkImageMemoryBarrier();
				barrier.image = image.handle();
				barrier.subresourceRange = subresource.range();
				barrier.srcAccessMask = IntegerEnumeration.mask(src);
				barrier.dstAccessMask = IntegerEnumeration.mask(dest);
				barrier.oldLayout = oldLayout;
				barrier.newLayout = newLayout;

				// TODO
				barrier.srcQueueFamilyIndex = Queue.Family.IGNORED;
				barrier.dstQueueFamilyIndex = Queue.Family.IGNORED;

				// Add barrier
				images.add(barrier);

				return Builder.this;
			}
		}
	}
}
