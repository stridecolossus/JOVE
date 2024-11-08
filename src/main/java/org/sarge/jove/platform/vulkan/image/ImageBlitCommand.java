package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.Map.Entry;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.StructureCollector;
import static org.sarge.lib.Validation.*;

/**
 * The <i>image blit command</i> copies regions of an image, potentially performing format conversion, scaling and filtering.
 * @author Sarge
 */
public final class ImageBlitCommand implements Command {
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
		this.src = requireNonNull(src);
		this.srcLayout = requireNonNull(srcLayout);
		this.dest = requireNonNull(dest);
		this.destLayout = requireNonNull(destLayout);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.filter = requireNonNull(filter);
	}

	@Override
	public void record(VulkanLibrary lib, Buffer buffer) {
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
			 * Creates a blit region for the whole of the given image.
			 * @param image Image
			 * @return Whole image blit region
			 */
			public static BlitRegion of(Image image) {
				return new BlitRegion(image.descriptor(), Extents.ZERO, image.descriptor().extents());
			}

			/**
			 * Constructor.
			 * @param subresource		Region sub-resource
			 * @param min				Minimal offset
			 * @param max				Maximum offset
			 */
			public BlitRegion {
				requireNonNull(subresource);
				requireNonNull(min);
				requireNonNull(max);
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
			this.src = requireNonNull(src);
			return this;
		}

		/**
		 * Sets the destination image.
		 * @param dest Destination image
		 */
		public Builder destination(Image dest) {
			this.dest = requireNonNull(dest);
			return this;
		}

		/**
		 * Adds a blit copy region.
		 * @param src Source blit region
		 * @param dest Destination blit region
		 */
		public Builder region(BlitRegion src, BlitRegion dest) {
			requireNonNull(src);
			requireNonNull(dest);
			regions.put(src, dest);
			return this;
		}

		/**
		 * Adds a blit copy region for the whole of the configured source and destination images.
		 * @throws NullPointerException if the source and destination image have not been configured
		 */
		public Builder region() {
			return region(BlitRegion.of(src), BlitRegion.of(dest));
		}

		/**
		 * Sets the image filter (default is {@link VkFilter#LINEAR}).
		 * @param filter Filter
		 */
		public Builder filter(VkFilter filter) {
			this.filter = requireNonNull(filter);
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
			final var offsets = new VkOffset3D[2];
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
			final VkImageBlit[] array = StructureCollector.array(regions.entrySet(), new VkImageBlit(), this::populate);

			// Create command
			return new ImageBlitCommand(src, srcLayout, dest, destLayout, array, filter);
		}
	}
}
