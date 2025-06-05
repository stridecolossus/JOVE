package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;

/**
 * An <i>image copy command</i> is used to copy between images.
 * @author Sarge
 */
public final class ImageCopyCommand implements Command {
	/**
	 * Creates a copy command for the whole of the image.
	 * @param src		Source
	 * @param dest		Destination
	 * @return Copy command
	 */
	public static ImageCopyCommand of(Image src, Image dest) {
		final Descriptor descriptor = src.descriptor();
		// TODO - check same extents?
		final CopyRegion region = new CopyRegion(descriptor, Extents.ZERO, descriptor, Extents.ZERO, descriptor.extents());
		return new ImageCopyCommand.Builder(src, dest).region(region).build();
	}

	private final Image src, dest;
	private final VkImageLayout srcLayout, destLayout;
	private final VkImageCopy[] regions;

	/**
	 * Constructor.
	 * @param src			Source image
	 * @param dest			Destination image
	 * @param srcLayout		source layout
	 * @param destLayout	Destination layout
	 * @param regions		Copy regions
	 */
	private ImageCopyCommand(Image src, Image dest, VkImageLayout srcLayout, VkImageLayout destLayout, VkImageCopy[] regions) {
		this.src = requireNonNull(src);
		this.dest = requireNonNull(dest);
		this.srcLayout = requireNonNull(srcLayout);
		this.destLayout = requireNonNull(destLayout);
		this.regions = regions.clone();
	}

	@Override
	public void execute(VulkanLibrary lib, CommandBuffer buffer) {
		lib.vkCmdCopyImage(buffer, src, srcLayout, dest, destLayout, regions.length, regions);
	}

	/**
	 * Transient copy region.
	 */
	public record CopyRegion(SubResource src, Extents srcOffset, SubResource dest, Extents destOffset, Extents extents) {
		/**
		 * Constructor.
		 * @param src				Source sub-resource
		 * @param srcOffset			Source offsets
		 * @param dest				Destination sub-resource
		 * @param destOffset		Destination offsets
		 * @param extents			Copy extents
		 */
		public CopyRegion {
			requireNonNull(src);
			requireNonNull(srcOffset);
			requireNonNull(dest);
			requireNonNull(destOffset);
			requireNonNull(extents);
		}

		/**
		 * Populates the copy region descriptor.
		 */
		private void populate(VkImageCopy copy) {
			copy.srcSubresource = SubResource.toLayers(src);
			copy.dstSubresource = SubResource.toLayers(dest);
			copy.srcOffset = srcOffset.toOffset();
			copy.dstOffset = destOffset.toOffset();
			copy.extent = extents.toExtent();
		}
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private final Image src, dest;
		private VkImageLayout srcLayout = TRANSFER_SRC_OPTIMAL;
		private VkImageLayout destLayout = TRANSFER_DST_OPTIMAL;
		private final List<CopyRegion> regions = new ArrayList<>();

		/**
		 * Constructor.
		 * @param src		Source image
		 * @param dest		Destination image
		 */
		public Builder(Image src, Image dest) {
			if(src == dest) throw new IllegalArgumentException("Cannot copy to self");
			this.src = requireNonNull(src);
			this.dest = requireNonNull(dest);
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
			if(!ok) throw new IllegalArgumentException("Invalid image layout: " + layout);
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
		 * @return New copy command
		 * @throws IllegalArgumentException if no copy regions have been configured
		 */
		public ImageCopyCommand build() {
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");
			final VkImageCopy[] array = null; // TODO StructureCollector.array(regions, new VkImageCopy(), CopyRegion::populate);
			return new ImageCopyCommand(src, dest, srcLayout, destLayout, array);
		}
	}
}
