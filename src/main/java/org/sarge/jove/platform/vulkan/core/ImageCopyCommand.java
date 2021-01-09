package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkBufferImageCopy;
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

	/**
	 * Constructor.
	 * @param image			Image
	 * @param buffer		Data buffer
	 * @param region		Region descriptor
	 * @param layout		Image layout
	 */
	protected ImageCopyCommand(Image image, VulkanBuffer buffer, VkBufferImageCopy[] regions, VkImageLayout layout) {
		Check.notEmpty(regions);
		this.image = notNull(image);
		this.buffer = notNull(buffer);
		this.regions = Arrays.copyOf(regions, regions.length);
		this.layout = notNull(layout);
	}

	@Override
	public void execute(VulkanLibrary lib, Handle handle) {
		lib.vkCmdCopyBufferToImage(handle, buffer.handle(), image.handle(), layout, regions.length, regions);
	}

	/**
	 * Inverts this command to copy <b>from</b> the image to the buffer.
	 * @return Inverted copy command
	 */
	public Command invert() {
		return (api, handle) -> api.vkCmdCopyImageToBuffer(handle, image.handle(), layout, buffer.handle(), regions.length, regions);
	}

	/**
	 * Builder for an image copy command.
	 */
	public static class Builder {
		private final VkBufferImageCopy region = new VkBufferImageCopy();
		private final SubResourceBuilder<Builder> subresource;
		private VulkanBuffer buffer;
		private Image image;
		private VkImageLayout layout;

		/**
		 * Constructor.
		 * @param image Image
		 */
		public Builder(Image image) {
			this.image = notNull(image);
			this.subresource = image.descriptor().builder(this);
		}

		/**
		 * Sets the buffer.
		 * @param buffer Data buffer
		 */
		public Builder buffer(VulkanBuffer buffer) {
			this.buffer = notNull(buffer);
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
		 */
		public SubResourceBuilder<Builder> subresource() {
			return subresource;
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
			return new ImageCopyCommand(image, buffer, new VkBufferImageCopy[]{region}, layout);
		}
	}
}
