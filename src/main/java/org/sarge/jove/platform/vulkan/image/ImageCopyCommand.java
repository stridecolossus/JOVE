package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.Level;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor.Extents;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

/**
 * An <i>image copy command</i> is used to copy an image to/from a Vulkan buffer.
 * @author Sarge
 */
public class ImageCopyCommand implements Command {
	private final Image image;
	private final VulkanBuffer buffer;
	private final boolean inverse;
	private final VkBufferImageCopy[] regions;
	private final VkImageLayout layout;

	/**
	 * Constructor.
	 * @param image				Image
	 * @param buffer			Buffer
	 * @param inverse			Whether copy direction is inverted
	 * @param regions			Copy region(s)
	 * @param layout			Image layout
	 * @throws IllegalStateException if the buffer cannot be used for copy operations
	 */
	private ImageCopyCommand(Image image, VulkanBuffer buffer, boolean inverse, VkBufferImageCopy[] regions, VkImageLayout layout) {
		this.image = notNull(image);
		this.buffer = notNull(buffer);
		this.inverse = inverse;
		this.regions = Arrays.copyOf(regions, regions.length);
		this.layout = notNull(layout);
		validate();
	}

	private void validate() {
		buffer.require(inverse ? VkBufferUsageFlag.TRANSFER_DST : VkBufferUsageFlag.TRANSFER_SRC);
	}
	// TODO - validation
	// dstImageLayout must be VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL, or VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR
	// srcImageLayout must be VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL, or VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR

	@Override
	public void execute(VulkanLibrary lib, Command.Buffer cb) {
		lib.vkCmdCopyBufferToImage(cb, buffer, image, layout, regions.length, regions);
	}

	/**
	 * Inverts this command to copy <i>from</i> the buffer <i>to</i> the image.
	 * @return Inverse copy command
	 */
	public Command invert() {
		buffer.require(VkBufferUsageFlag.TRANSFER_DST);
		return (lib, cmd) -> lib.vkCmdCopyImageToBuffer(cmd, image, layout, buffer, regions.length, regions);
	}

	/**
	 * A <i>copy region</i> specifies a portion of the image to be copied.
	 */
	public record CopyRegion(long offset, int length, int height, SubResource res, VkOffset3D imageOffset, Extents extents) {
		/**
		 * Creates a copy region for the whole of the given image.
		 * @param descriptor Image descriptor
		 * @return New copy region
		 */
		public static CopyRegion of(ImageDescriptor descriptor) {
			return new CopyRegion.Builder()
					.subresource(descriptor)
					.extents(descriptor.extents())
					.build();
		}

		/**
		 * Constructor.
		 * @param offset			Buffer offset
		 * @param length			Row length
		 * @param height			Row height
		 * @param res				Sub-resource
		 * @param imageOffset		Image offset
		 * @param extents			Image extents
		 * @throws IllegalArgumentException if the length/height is non-zero but smaller than the given extents
		 * @throws IllegalArgumentException if the sub-resource has more than one aspect
		 */
		public CopyRegion {
			Check.zeroOrMore(offset);
			Check.zeroOrMore(length);
			Check.zeroOrMore(height);
			Check.notNull(res);
			Check.notNull(imageOffset);
			Check.notNull(extents);
			validate(length, extents.size().width());
			validate(height, extents.size().height());
			if(res.aspects().size() != 1) throw new IllegalArgumentException("Sub-resource must have a single aspect: " + res);
		}

		private static void validate(int value, int min) {
			if((value > 0) && (value < min)) {
				throw new IllegalArgumentException(String.format("Invalid length/height: value=%d min=%d", value, min));
			}
		}

		/**
		 * Populates the copy descriptor.
		 */
		private void populate(VkBufferImageCopy copy) {
			copy.bufferOffset = offset;
			copy.bufferRowLength = length;
			copy.bufferImageHeight = height;
			copy.imageSubresource = SubResource.toLayers(res);
			copy.imageOffset = imageOffset;
			copy.imageExtent = extents.toExtent();
		}

		/**
		 * Builder for a copy region.
		 */
		public static class Builder {
			private long offset;
			private int length;
			private int height;
			private SubResource subresource;
			private VkOffset3D imageOffset = new VkOffset3D();
			private Extents extents;

			/**
			 * Sets the buffer offset.
			 * @param offset Buffer offset (bytes)
			 */
			public Builder offset(long offset) {
				this.offset = zeroOrMore(offset);
				return this;
			}

			/**
			 * Sets the buffer row length.
			 * @param length Row length (texels)
			 */
			public Builder length(int length) {
				this.length = zeroOrMore(length);
				return this;
			}

			/**
			 * Sets the image height.
			 * @param height Image height (texels)
			 */
			public Builder height(int height) {
				this.height = zeroOrMore(height);
				return this;
			}

			/**
			 * Sets the image offset (default is no offset).
			 * @param offset Image offset
			 */
			public Builder offset(VkOffset3D offset) {
				this.imageOffset = notNull(offset);
				return this;
			}

			/**
			 * Sets the image extents.
			 * @param extents Image extents
			 */
			public Builder extents(Extents extents) {
				this.extents = notNull(extents);
				return this;
			}

			/**
			 * Convenience method to set the image offset and extent to the given rectangle.
			 * @param rect Copy rectangle
			 */
			public Builder region(Rectangle rect) {
				imageOffset.x = rect.x();
				imageOffset.y = rect.y();
				extents(new Extents(rect.dimensions()));
				return this;
			}

			/**
			 * Sets the sub-resource for this copy command.
			 * @param subresource Sub-resource
			 */
			public Builder subresource(SubResource subresource) {
				this.subresource = notNull(subresource);
				return this;
			}

			/**
			 * Constructs this copy region.
			 * @return New copy region
			 */
			public CopyRegion build() {
				return new CopyRegion(offset, length, height, subresource, imageOffset, extents);
			}
		}
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private VulkanBuffer buffer;
		private Image image;
		private boolean inverse;
		private VkImageLayout layout;
		private final List<CopyRegion> regions = new ArrayList<>();

		/**
		 * Sets the source/destination buffer.
		 * @param buffer Buffer
		 */
		public Builder buffer(VulkanBuffer buffer) {
			this.buffer = notNull(buffer);
			return this;
		}

		/**
		 * Sets the image.
		 * @param image Image
		 */
		public Builder image(Image image) {
			this.image = notNull(image);
			return this;
		}

		/**
		 * Inverts the direction of this builder.
		 */
		public Builder invert() {
			inverse = !inverse;
			return this;
		}

		/**
		 * Sets the image layout.
		 * @param layout Image layout
		 */
		public Builder layout(VkImageLayout layout) {
			this.layout = notNull(layout);
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
		 * Helper - Adds copy regions for <b>all</b> layers and MIP levels of the given image.
		 * @param image Image
		 * @throws NullPointerException if the image texture has not been populated
		 */
		public Builder region(ImageData image) {
			final ImageDescriptor descriptor = this.image.descriptor();
			final int count = descriptor.layerCount();
			final Level[] levels = image.levels().toArray(Level[]::new);
			for(int level = 0; level < levels.length; ++level) {
				// Determine extents for this MIP level
				final Extents extents = descriptor.extents().mip(level);

				// Load layers for this MIP level
				for(int layer = 0; layer < count; ++layer) {
					// Build sub-resource
					final SubResource res = new SubResource.Builder(descriptor)
							.baseArrayLayer(layer)
							.mipLevel(level)
							.build();

					// Determine layer offset within this level
					final int offset = levels[level].offset(layer, count);

					// Create copy region
					final CopyRegion region = new CopyRegion.Builder()
							.offset(offset)
							.subresource(res)
							.extents(extents)
							.build();

					// Add region
					region(region);
				}
			}

			return this;
		}

		/**
		 * Constructs this copy command.
		 * If no regions are specified the resultant command copies the <b>whole</b> of the image.
		 * @return New copy command
		 * @throws IllegalArgumentException if the image, buffer or image layout have not been populated, or if no copy regions have been specified
		 */
		public ImageCopyCommand build() {
			// Validate
			Check.notNull(image);
			Check.notNull(buffer);
			Check.notNull(layout);
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");

			// Populate copy regions
			final VkBufferImageCopy[] array = StructureHelper.array(regions, VkBufferImageCopy::new, CopyRegion::populate);

			// Create copy command
			return new ImageCopyCommand(image, buffer, inverse, array, layout);
		}
	}
}
