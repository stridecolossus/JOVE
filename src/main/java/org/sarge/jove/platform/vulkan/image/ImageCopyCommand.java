package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.platform.util.StructureCollector;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.ImmediateCommand;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.Image.*;
import org.sarge.lib.util.Check;

/**
 * An <i>image copy command</i> is used to copy between images.
 * @author Sarge
 */
public class ImageCopyCommand extends ImmediateCommand {
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
		this.src = notNull(src);
		this.dest = notNull(dest);
		this.srcLayout = notNull(srcLayout);
		this.destLayout = notNull(destLayout);
		this.regions = regions.clone();
	}

	@Override
	public void execute(VulkanLibrary lib, Buffer buffer) {
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
			Check.notNull(src);
			Check.notNull(srcOffset);
			Check.notNull(dest);
			Check.notNull(destOffset);
			Check.notNull(extents);
		}

		/**
		 * Populates the copy region descriptor.
		 */
		private void populate(VkImageCopy copy) {
			copy.srcSubresource = src.toLayers();
			copy.dstSubresource = dest.toLayers();
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
		private VkImageLayout srcLayout = VkImageLayout.TRANSFER_SRC_OPTIMAL;
		private VkImageLayout destLayout = VkImageLayout.TRANSFER_DST_OPTIMAL;
		private final List<CopyRegion> regions = new ArrayList<>();

		/**
		 * Constructor.
		 * @param src		Source image
		 * @param dest		Destination image
		 */
		public Builder(Image src, Image dest) {
			if(src == dest) throw new IllegalArgumentException("Cannot copy to self");
			this.src = notNull(src);
			this.dest = notNull(dest);
		}

		/**
		 * Sets the layout of the source image.
		 * @param srcLayout Source layout
		 */
		public Builder source(VkImageLayout srcLayout) {
			this.srcLayout = notNull(srcLayout);
			// TODO - TRANSFER_SRC_OPTIMAL | GENERAL | SHARED_PRESENT
			// TODO - check format features?
			return this;
		}

		/**
		 * Sets the layout of the destination image.
		 * @param destayout Destination layout
		 */
		public Builder destination(VkImageLayout destLayout) {
			this.destLayout = notNull(destLayout);
			// TODO - TRANSFER_DST_OPTIMAL | GENERAL | SHARED_PRESENT
			// TODO - check format features?
			return this;
		}

		/**
		 * Adds a copy region.
		 * @param region Copy region
		 */
		public Builder region(CopyRegion region) {
			regions.add(notNull(region));
			return this;
		}

		/**
		 * Constructs this copy command.
		 * @return New copy command
		 * @throws IllegalArgumentException if no copy regions have been configured
		 */
		public ImageCopyCommand build() {
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");
			final VkImageCopy[] array = StructureCollector.array(regions, new VkImageCopy(), CopyRegion::populate);
			return new ImageCopyCommand(src, dest, srcLayout, destLayout, array);
		}
	}
}
