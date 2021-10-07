package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
import org.sarge.jove.platform.vulkan.VkBufferUsage;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkOffset3D;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.lib.util.Check;

/**
 * An <i>image copy command</i> is used to copy an image to/from a vertex buffer.
 * @author Sarge
 */
public class ImageCopyCommand implements Command {
	private final Image image;
	private final VulkanBuffer buffer;
	private final VkBufferImageCopy[] regions;
	private final VkImageLayout layout;
	private final boolean bufferToImage;

	/**
	 * Constructor.
	 * @param image				Image
	 * @param buffer			Buffer
	 * @param regions			Copy region(s)
	 * @param layout			Image layout
	 * @param bufferToImage		Whether copying <i>to</i> or <i>from</i> the image
	 * @throws IllegalStateException if the image or buffer is not a valid source/destination for this operation
	 * @see VulkanBuffer#require(VkBufferUsage)
	 */
	private ImageCopyCommand(Image image, VulkanBuffer buffer, VkBufferImageCopy[] regions, VkImageLayout layout, boolean bufferToImage) {
		Check.notEmpty(regions);
		this.image = notNull(image);
		this.buffer = notNull(buffer);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.layout = notNull(layout);
		this.bufferToImage = bufferToImage;
		validate();
	}

	/**
	 * @throws IllegalStateException for an invalid source/destination configuration.
	 */
	private void validate() {
		final VkBufferUsage flag = bufferToImage ? VkBufferUsage.TRANSFER_SRC : VkBufferUsage.TRANSFER_DST;
		buffer.require(flag);
	}

	// TODO
	// dstImageLayout must be VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL, or VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR
	// srcImageLayout must be VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL, or VK_IMAGE_LAYOUT_SHARED_PRESENT_KHR

	/**
	 * Test helper.
	 */
	protected VkBufferImageCopy[] regions() {
		return regions;
	}
	// TODO

	@Override
	public void execute(VulkanLibrary lib, Command.Buffer cb) {
		if(bufferToImage) {
			lib.vkCmdCopyBufferToImage(cb, buffer, image, layout, regions.length, regions);
		}
		else {
			lib.vkCmdCopyImageToBuffer(cb, image, layout, buffer, regions.length, regions);
		}
	}

	/**
	 * Builder for an image copy.
	 */
	public static class Builder {
		private Image image;
		private VkImageLayout layout;
		private VkOffset3D offset = new VkOffset3D();
		private SubResource subresource;
		private VulkanBuffer buffer;
		private boolean bufferToImage = true;

		/**
		 * Sets the image to be copied.
		 * @param image Image
		 */
		public Builder image(Image image) {
			this.image = notNull(image);
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
		 * Sets the image offset (default is no offset).
		 * @param offset Image offset
		 */
		public Builder offset(VkOffset3D offset) {
			this.offset = notNull(offset);
			return this;
		}

		/**
		 * Sets the sub-resource for this copy.
		 * @param subresource Sub-resource
		 */
		public Builder subresource(SubResource subresource) {
			this.subresource = notNull(subresource);
			return this;
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
			bufferToImage = !bufferToImage;
			return this;
		}

		/**
		 * Constructs this copy command.
		 * @return New copy command
		 * @throws IllegalArgumentException if the image, buffer or image layout have not been populated
		 */
		public ImageCopyCommand build() {
			// Validate
			Check.notNull(image);
			Check.notNull(buffer);
			Check.notNull(layout);

			// Populate descriptor
			final VkBufferImageCopy copy = new VkBufferImageCopy();
			copy.imageSubresource = SubResource.of(image.descriptor(), subresource).toLayers();
			copy.imageExtent = image.descriptor().extents().toExtent3D();
			copy.imageOffset = offset;
			// TODO
//			public long bufferOffset;
//			public int bufferRowLength;			// 0 or >= imageExtent.width
//			public int bufferImageHeight;		// 0 or >= imageExtent.height

			// Create copy command
			return new ImageCopyCommand(image, buffer, new VkBufferImageCopy[]{copy}, layout, bufferToImage);
		}
	}
}
