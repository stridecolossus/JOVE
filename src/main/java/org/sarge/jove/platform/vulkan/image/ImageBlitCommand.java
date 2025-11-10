package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.Map.Entry;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;

/**
 * The <i>image blit command</i> copies regions of an image, potentially performing format conversion, scaling and filtering.
 * @author Sarge
 */
public final class ImageBlitCommand implements Command {
	private final Image src, dest;
	private final VkImageLayout srcLayout, destLayout;
	private final VkImageBlit[] regions;
	private final VkFilter filter;
	private final Image.Library library;

	/**
	 * Constructor.
	 * @param src			Source image
	 * @param srcLayout		Source image layout
	 * @param dest			Destination image
	 * @param destLayout	Destination image layout
	 * @param regions		Copy regions
	 * @param filter		Filter
	 * @param library		Image library
	 */
	ImageBlitCommand(Image src, VkImageLayout srcLayout, Image dest, VkImageLayout destLayout, VkImageBlit[] regions, VkFilter filter, Image.Library library) {
		this.src = requireNonNull(src);
		this.srcLayout = requireNonNull(srcLayout);
		this.dest = requireNonNull(dest);
		this.destLayout = requireNonNull(destLayout);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.filter = requireNonNull(filter);
		this.library = requireNonNull(library);
	}

	@Override
	public void execute(Buffer buffer) {
		library.vkCmdBlitImage(buffer, src, srcLayout, dest, destLayout, regions.length, regions, filter);
	}

	/**
	 * Builder for an image blit command.
	 */
	public static class Builder {
		/**
		 * Blit copy region.
		 */
		public record BlitRegion(Subresource subresource, Extents min, Extents max) {
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
		private VkImageBlit populate(Entry<BlitRegion, BlitRegion> entry) {
			final var blit = new VkImageBlit();

			final BlitRegion src = entry.getKey();
			blit.srcSubresource = Subresource.layers(src.subresource);
			blit.srcOffsets = offsets(src);

			final BlitRegion dest = entry.getValue();
			blit.dstSubresource = Subresource.layers(dest.subresource);
			blit.dstOffsets = offsets(dest);

			return blit;
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
		 * @param library Image library
		 * @return New blit command
		 * @throws IllegalArgumentException if the source and destination images have not been specified, or no copy regions are specified
		 */
		public ImageBlitCommand build(Image.Library library) {
			final VkImageBlit[] array = regions
					.entrySet()
					.stream()
					.map(this::populate)
					.toArray(VkImageBlit[]::new);

			return new ImageBlitCommand(src, srcLayout, dest, destLayout, array, filter, library);
		}
	}
}
