package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;

/**
 * An <i>image copy</i> command is used to copy between images.
 * @author Sarge
 */
public class ImageCopyCommand implements Command {
	/**
	 * Creates a copy command for the whole of the image.
	 * @param source			Source
	 * @param destination			Destination
	 * @param library		Image library
	 * @return Copy command
	 */
	public static ImageCopyCommand of(Image source, Image destination, Image.Library library) {
		final Descriptor descriptor = source.descriptor();
		// TODO - check same extents?
		final CopyRegion region = new CopyRegion(descriptor, Extents.ZERO, descriptor, Extents.ZERO, descriptor.extents());
		return new ImageCopyCommand.Builder(source, destination).region(region).build(library);
	}

	private final Image source, destination;
	private final VkImageLayout srcLayout, destLayout;
	private final VkImageCopy[] regions;
	private final Image.Library library;

	/**
	 * Constructor.
	 * @param source			Source image
	 * @param destination		Destination image
	 * @param srcLayout			Source layout
	 * @param destLayout		Destination layout
	 * @param regions			Copy regions
	 * @param library			Image library
	 */
	private ImageCopyCommand(Image source, Image destination, VkImageLayout srcLayout, VkImageLayout destLayout, List<VkImageCopy> regions, Image.Library library) {
		this.source = requireNonNull(source);
		this.destination = requireNonNull(destination);
		this.srcLayout = requireNonNull(srcLayout);
		this.destLayout = requireNonNull(destLayout);
		this.regions = regions.toArray(VkImageCopy[]::new);
		this.library = requireNonNull(library);
	}

	@Override
	public void execute(Buffer buffer) {
		library.vkCmdCopyImage(buffer, source, srcLayout, destination, destLayout, regions.length, regions);
	}

	/**
	 * Transient copy region.
	 */
	public record CopyRegion(Subresource source, Extents srcOffset, Subresource destination, Extents destOffset, Extents extent) {
		/**
		 * Constructor.
		 * @param source				Source sub-resource
		 * @param srcOffset			Source offsets
		 * @param destination		Destination subresource
		 * @param destOffset		Destination offsets
		 * @param extent			Copy extent
		 */
		public CopyRegion {
			requireNonNull(source);
			requireNonNull(srcOffset);
			requireNonNull(destination);
			requireNonNull(destOffset);
			requireNonNull(extent);
		}

		/**
		 * @return Copy region descriptor
		 */
		private VkImageCopy populate() {
			final var copy = new VkImageCopy();
			copy.srcSubresource = Subresource.layers(source);
			copy.srcOffset = srcOffset.toOffset();
			copy.dstSubresource = Subresource.layers(destination);
			copy.dstOffset = destOffset.toOffset();
			copy.extent = extent.toExtent();
			return copy;
		}
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private final Image source, destination;
		private VkImageLayout srcLayout = TRANSFER_SRC_OPTIMAL;
		private VkImageLayout destLayout = TRANSFER_DST_OPTIMAL;
		private final List<CopyRegion> regions = new ArrayList<>();

		/**
		 * Constructor.
		 * @param source			Source image
		 * @param destination		Destination image
		 */
		public Builder(Image source, Image destination) {
			if(source == destination) {
				throw new IllegalArgumentException("Cannot copy to self");
			}
			this.source = requireNonNull(source);
			this.destination = requireNonNull(destination);
		}
		// TODO - check format features of the images?

		/**
		 * Sets the layout of the source image.
		 * @param srcLayout Source layout
		 */
		public Builder source(VkImageLayout srcLayout) {
			validate(srcLayout, TRANSFER_SRC_OPTIMAL);
			this.srcLayout = requireNonNull(srcLayout);
			return this;
		}

		/**
		 * Sets the layout of the destination image.
		 * @param destayout Destination layout
		 */
		public Builder destination(VkImageLayout destLayout) {
			validate(destLayout, TRANSFER_DST_OPTIMAL);
			this.destLayout = requireNonNull(destLayout);
			return this;
		}

		private static void validate(VkImageLayout layout, VkImageLayout valid) {
			final boolean ok = switch(layout) {
				case GENERAL, SHARED_PRESENT_KHR -> true;
				default -> layout == valid;
			};
			if(!ok) {
				throw new IllegalArgumentException("Invalid image layout: " + layout);
			}
		}

		/**
		 * Adds a copy region.
		 * @param region Copy region
		 */
		public Builder region(CopyRegion region) {
			regions.add(requireNonNull(region));
			return this;
		}

		/**
		 * Constructs this copy command.
		 * @param library Image library
		 * @return Copy command
		 * @throws IllegalArgumentException if no copy regions have been configured
		 */
		public ImageCopyCommand build(Image.Library library) {
			if(regions.isEmpty()) {
				throw new IllegalArgumentException("No copy regions specified");
			}
			final List<VkImageCopy> array = regions.stream().map(CopyRegion::populate).toList();
			return new ImageCopyCommand(source, destination, srcLayout, destLayout, array, library);
		}
	}
}
