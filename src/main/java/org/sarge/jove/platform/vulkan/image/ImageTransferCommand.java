package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.Level;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.ImmediateCommand;
import org.sarge.jove.platform.vulkan.image.Image.*;
import org.sarge.jove.util.StructureCollector;
import org.sarge.lib.util.Check;

/**
 * An <i>image transfer command</i> is used to copy an image to/from a Vulkan buffer.
 * @author Sarge
 */
public final class ImageTransferCommand extends ImmediateCommand {
	private final Image image;
	private final VulkanBuffer buffer;
	private final boolean write;
	private final VkBufferImageCopy[] regions;
	private final VkImageLayout layout;

	/**
	 * Constructor.
	 * @param image				Image
	 * @param buffer			Buffer
	 * @param write				Whether this command copies the buffer <b>to</b> the image or vice-versa
	 * @param regions			Copy region(s)
	 * @param layout			Image layout
	 * @throws IllegalStateException if the {@link #buffer} or the image {@link #layout} are not valid for this transfer operation
	 */
	ImageTransferCommand(Image image, VulkanBuffer buffer, boolean write, VkBufferImageCopy[] regions, VkImageLayout layout) {
		this.image = notNull(image);
		this.buffer = notNull(buffer);
		this.write = write;
		this.regions = Arrays.copyOf(regions, regions.length);
		this.layout = notNull(layout);
		validateBuffer();
		validateLayout();
	}

	private void validateBuffer() {
		buffer.require(write ? VkBufferUsageFlag.TRANSFER_SRC : VkBufferUsageFlag.TRANSFER_DST);
	}

	private void validateLayout() {
		final boolean valid = switch(layout) {
			case GENERAL, SHARED_PRESENT_KHR -> true;
			case TRANSFER_DST_OPTIMAL -> write;
			case TRANSFER_SRC_OPTIMAL -> !write;
			default -> false;
		};
		if(!valid) throw new IllegalStateException("Invalid image layout for copy operation: write=%s layout=%s".formatted(write, layout));
	}

	@Override
	public void record(VulkanLibrary lib, Command.Buffer cmd) {
		if(write) {
			lib.vkCmdCopyBufferToImage(cmd, buffer, image, layout, regions.length, regions);
		}
		else {
			lib.vkCmdCopyImageToBuffer(cmd, image, layout, buffer, regions.length, regions);
		}
	}

	/**
	 * Inverts the direction of this command.
	 * @return Inverse copy command
	 * @throws IllegalStateException if the buffer is not valid for this transfer operation
	 */
	public ImageTransferCommand invert() {
		return new ImageTransferCommand(image, buffer, !write, regions, layout);
	}

	/**
	 * A <i>copy region</i> specifies a portion of the image to be copied.
	 */
	public record CopyRegion(long offset, Dimensions row, SubResource subresource, Extents imageOffset, Extents extents) {
		/**
		 * Creates a copy region for the whole of the given image.
		 * @param descriptor Image descriptor
		 * @return New copy region
		 */
		public static CopyRegion of(Descriptor descriptor) {
			return new CopyRegion.Builder()
					.subresource(descriptor)
					.extents(descriptor.extents())
					.build();
		}

		/**
		 * Constructor.
		 * @param offset			Buffer offset
		 * @param row				Row length/height (texels) or {@code zero} to use the same dimensions as the image
		 * @param subresource		Sub-resource
		 * @param imageOffset		Image offset
		 * @param extents			Image extents
		 * @throws IllegalArgumentException if {@code row} is non-zero but smaller than the given extents
		 * @throws IllegalArgumentException if the sub-resource has more than one aspect
		 */
		public CopyRegion {
			Check.zeroOrMore(offset);
			Check.notNull(row);
			Check.notNull(subresource);
			Check.notNull(imageOffset);
			Check.notNull(extents);
			if(subresource.aspects().size() != 1) {
				throw new IllegalArgumentException("Sub-resource must have a single aspect: " + subresource);
			}
			if(!validate(row, extents.size())) {
				throw new IllegalArgumentException(String.format("Row length/height cannot be smaller than image extents: row=%s extents=%s", row, extents));
			}
		}

		private static boolean validate(Dimensions row, Dimensions size) {
			if((row.width() == 0) || (row.height() == 0)) {
				return true;
			}
			else {
				return row.compareTo(size) >= 0;
			}
		}

		/**
		 * Populates the copy descriptor.
		 */
		void populate(VkBufferImageCopy copy) {
			copy.bufferOffset = offset;
			copy.bufferRowLength = row.width();
			copy.bufferImageHeight = row.height();
			copy.imageSubresource = SubResource.toLayers(subresource);
			copy.imageOffset = imageOffset.toOffset();
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
			private Extents imageOffsets = Extents.ZERO;
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
			 * @param imageOffset Image offsets
			 * @throws IndexOutOfBoundsException if the offset array does not contain three values
			 */
			public Builder imageOffsets(Extents imageOffsets) {
				this.imageOffsets = notNull(imageOffsets);
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
				imageOffsets(new Extents(new Dimensions(rect.x(), rect.y())));
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
				return new CopyRegion(offset, new Dimensions(length, height), subresource, imageOffsets, extents);
			}
		}
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private VulkanBuffer buffer;
		private Image image;
		private boolean write = true;
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
		 * By default the buffer is copied <b>to</b> the image.
		 */
		public Builder invert() {
			write = !write;
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
			final Descriptor descriptor = this.image.descriptor();
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
		 * @throws IllegalStateException if the buffer or the image layout are not valid for this transfer operation
		 */
		public ImageTransferCommand build() {
			// Validate
			Check.notNull(image);
			Check.notNull(buffer);
			Check.notNull(layout);
			if(regions.isEmpty()) throw new IllegalArgumentException("No copy regions specified");

			// Populate copy regions
			final VkBufferImageCopy[] array = StructureCollector.array(regions, new VkBufferImageCopy(), CopyRegion::populate);

			// Create copy command
			return new ImageTransferCommand(image, buffer, write, array, layout);
		}
	}
}
