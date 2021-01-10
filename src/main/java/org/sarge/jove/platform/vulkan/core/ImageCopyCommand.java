package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkOffset3D;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor.SubResourceBuilder;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.util.Check;

/**
 * An <i>image copy command</i> is used to copy and image to/from a vertex buffer.
 * @author Sarge
 */
public class ImageCopyCommand extends ImmediateCommand {
	private final Image image;
	private final VulkanBuffer buffer;
	private final VkBufferImageCopy[] regions;
	private final VkImageLayout layout;
	private final boolean toImage;

	/**
	 * Constructor.
	 * @param image			Image
	 * @param buffer		Buffer
	 * @param region		Region(s) descriptor
	 * @param layout		Image layout
	 * @param toImage		Whether copying <i>to</i> or <i>from</i> the image
	 * @throws IllegalStateException if the image or buffer is not a valid source/destination for this operation
	 * @see VulkanBuffer#require(VkBufferUsageFlag)
	 */
	private ImageCopyCommand(Image image, VulkanBuffer buffer, VkBufferImageCopy[] regions, VkImageLayout layout, boolean toImage) {
		Check.notEmpty(regions);
		this.image = notNull(image);
		this.buffer = notNull(buffer);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.layout = notNull(layout);
		this.toImage = toImage;
		validate();
	}

	/**
	 * @throws IllegalStateException for an invalid source/destination configuration.
	 */
	private void validate() {
		final VkBufferUsageFlag flag = toImage ? VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT : VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
		final VkImageLayout expected = toImage ? VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL : VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
		buffer.require(flag);
		if(this.layout != expected) throw new IllegalStateException(String.format("Invalid image layout: expected=%s actual=%s", expected, layout));
	}

	/**
	 * Test helper.
	 */
	protected VkBufferImageCopy[] regions() {
		return regions;
	}

	@Override
	public void execute(VulkanLibrary lib, Handle handle) {
		if(toImage) {
			lib.vkCmdCopyBufferToImage(handle, buffer.handle(), image.handle(), layout, regions.length, regions);
		}
		else {
			lib.vkCmdCopyImageToBuffer(handle, image.handle(), layout, buffer.handle(), regions.length, regions);
		}
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private Image image;
		private VkImageLayout layout;
		private final VkBufferImageCopy region = new VkBufferImageCopy();
		private SubResourceBuilder<Builder> subresource;
		private VulkanBuffer buffer;
		private boolean toImage = true;

		/**
		 * Sets the image to be copied.
		 * @param image Image
		 */
		public Builder image(Image image) {
			this.image = notNull(image);
			this.subresource = image.descriptor().builder(this);
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
		 * Sets the image offset.
		 * @param offset Image offset
		 */
		public Builder offset(VkOffset3D offset) {
			region.imageOffset = notNull(offset);
			return this;
		}

		/**
		 * @return Builder for the image sub-resource
		 * @throws IllegalStateException if the image has not been specified
		 */
		public SubResourceBuilder<Builder> subresource() {
			if(subresource == null) throw new IllegalStateException("Image has not been specified");
			return subresource;
		}

		/**
		 * Sets the source/destination buffer.
		 * @param buffer Buffer
		 */
		public Builder buffer(VulkanBuffer buffer) {
			this.buffer = notNull(buffer);
			return this;
		}

		/**
		 * Inverts the direction of this copy operation (default is <b>to</b> the image).
		 */
		public Builder invert() {
			toImage = !toImage;
			return this;
		}

		/**
		 * Constructs this copy command.
		 * @return New copy command
		 * @throws IllegalArgumentException if the image, buffer or image layout have not been populated
		 */
		public ImageCopyCommand build() {
			// Validate
			if(image == null) throw new IllegalArgumentException("Image not specified");
			if(buffer == null) throw new IllegalArgumentException("Data buffer not specified");
			if(layout == null) throw new IllegalArgumentException("Image layout not specified");

			// Populate descriptor
			subresource.populate(region.imageSubresource);
			image.descriptor().extents().populate(region.imageExtent);
			region.imageOffset = new VkOffset3D();

			// TODO
//			public long bufferOffset;
//			public int bufferRowLength;			// 0 or >= imageExtent.width
//			public int bufferImageHeight;		// 0 or >= imageExtent.height

			// Create copy command
			return new ImageCopyCommand(image, buffer, new VkBufferImageCopy[]{region}, layout, toImage);
		}
	}
}
