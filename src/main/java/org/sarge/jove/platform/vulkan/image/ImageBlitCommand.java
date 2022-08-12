package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.Map.Entry;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.Image.Extents;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

/**
 * The <i>image blit command</i> copies regions of an image, potentially performing format conversion, scaling and filtering.
 * @author Sarge
 */
public class ImageBlitCommand implements Command {
	private final Image src, dest;
	private final VkImageLayout srcLayout, destLayout;
	private final VkImageBlit[] regions;
	private final VkFilter filter;

	/**
	 * Constructor.
	 * @param src			Source image
	 * @param srcLayout		Source image layout
	 * @param dest			Destination image
	 * @param destLayout	Destination image layout
	 * @param regions		Copy regions
	 * @param filter		Filter
	 */
	ImageBlitCommand(Image src, VkImageLayout srcLayout, Image dest, VkImageLayout destLayout, VkImageBlit[] regions, VkFilter filter) {
		this.src = notNull(src);
		this.srcLayout = notNull(srcLayout);
		this.dest = notNull(dest);
		this.destLayout = notNull(destLayout);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.filter = notNull(filter);
	}

	@Override
	public void execute(VulkanLibrary lib, Buffer buffer) {
		lib.vkCmdBlitImage(buffer, src, srcLayout, dest, destLayout, regions.length, regions, filter);
	}

	/**
	 * Builder for an image blit command.
	 */
	public static class Builder {
		/**
		 * Blit copy region.
		 */
		public record BlitRegion(SubResource subresource, Extents min, Extents max) {
			/**
			 * Minimum blit offset.
			 */
			public static final Extents MIN_OFFSET = new Extents(new Dimensions(0, 0));

			/**
			 * Constructor.
			 * @param subresource		Region sub-resource
			 * @param min				Minimal offset
			 * @param max				Maximum offset
			 */
			public BlitRegion {
				Check.notNull(subresource);
				Check.notNull(min);
				Check.notNull(max);
			}
		}

		private Image src, dest;
		private VkImageLayout srcLayout = VkImageLayout.TRANSFER_SRC_OPTIMAL;
		private VkImageLayout destLayout = VkImageLayout.TRANSFER_DST_OPTIMAL;
		private final Map<BlitRegion, BlitRegion> regions = new HashMap<>();
		private VkFilter filter = VkFilter.LINEAR;

		/**
		 * Sets the source image.
		 * @param src Source image
		 */
		public Builder source(Image src) {
			this.src = notNull(src);
			return this;
		}

		/**
		 * Sets the destination image.
		 * @param dest Destination image
		 */
		public Builder destination(Image dest) {
			this.dest = notNull(dest);
			return this;
		}

		/**
		 * Adds a blit copy region.
		 * @param src Source blit region
		 * @param dest Destination blit region
		 */
		public Builder region(BlitRegion src, BlitRegion dest) {
			Check.notNull(src);
			Check.notNull(dest);
			regions.put(src, dest);
			return this;
		}

		/**
		 * Sets the image filter (default is {@link VkFilter#LINEAR}).
		 * @param filter Filter
		 */
		public Builder filter(VkFilter filter) {
			this.filter = notNull(filter);
			return this;
		}

		/**
		 * Populates a blit descriptor from the source/destination regions.
		 */
		private void populate(Entry<BlitRegion, BlitRegion> entry, VkImageBlit blit) {
			// Populate source region
			final BlitRegion src = entry.getKey();
			blit.srcSubresource = SubResource.toLayers(src.subresource);
			blit.srcOffsets = offsets(src);

			// Populate destination region
			final BlitRegion dest = entry.getValue();
			blit.dstSubresource = SubResource.toLayers(dest.subresource);
			blit.dstOffsets = offsets(dest);
		}

		/**
		 * Helper - Populates the blit offsets array.
		 */
		private static VkOffset3D[] offsets(BlitRegion region) {
			final VkOffset3D[] offsets = new VkOffset3D[2];
			offsets[0] = region.min.toOffset();
			offsets[1] = region.max.toOffset();
			return offsets;
		}

		/**
		 * Constructs this blit command.
		 * @return New blit command
		 * @throws IllegalArgumentException if the source and destination images have not been specified, or no copy regions are specified
		 */
		public ImageBlitCommand build() {
			// Validate
			if(src == null) throw new IllegalArgumentException("No source image specified");
			if(dest == null) throw new IllegalArgumentException("No destination image specified");
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");

			// Create copy regions array
			final VkImageBlit[] array = StructureHelper.array(regions.entrySet(), VkImageBlit::new, this::populate);

			// Create command
			return new ImageBlitCommand(src, srcLayout, dest, destLayout, array, filter);
		}
	}
}
