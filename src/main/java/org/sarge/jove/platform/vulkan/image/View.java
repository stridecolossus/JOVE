package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

/**
 * An <i>image view</i> is the entry-point for operations on an image.
 * @author Sarge
 */
public class View extends VulkanObject {
	private final Image image;
	private final boolean release;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param device	Logical device
	 * @param image		Underlying image
	 * @param release	Whether the image is also destroyed when this view is released
	 */
	public View(Handle handle, LogicalDevice device, Image image, boolean release) {
		super(handle, device);
		this.image = requireNonNull(image);
		this.release = release;
	}

	/**
	 * @return Underlying image
	 */
	public Image image() {
		return image;
	}

	@Override
	protected Destructor<View> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyImageView;
	}

	@Override
	protected void release() {
		if(release && !image.isDestroyed()) {
			image.destroy();
		}
	}

	@Override
	public String toString() {
		return String.format("View[image=%s]", image);
	}

	/**
	 * Builder for an image view.
	 */
	public static class Builder {
		private VkImageViewType type;
		private ComponentMapping mapping = ComponentMapping.IDENTITY;
		private Subresource subresource;
		private boolean release;

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			this.type = type;
			return this;
		}

		/**
		 * Sets the component mapping for the view.
		 * @param mapping Component mapping
		 * @see ComponentMapping#IDENTITY
		 */
		public Builder mapping(ComponentMapping mapping) {
			this.mapping = mapping;
			return this;
		}

		/**
		 * Sets the image subresource for this view.
		 * @param subresource Image subresource
		 */
		public Builder subresource(Subresource subresource) {
			this.subresource = subresource;
			return this;
		}

		/**
		 * Sets the image to be automatically destroyed when the view is released.
		 */
		public Builder release() {
			this.release = true;
			return this;
		}

		/**
		 * Constructs this image view.
		 * The image type and subresource range are initialised to the given {@link #image} if not configured.
		 * @param device	Logical device
		 * @param image		Underlying image
		 * @return Image view
		 */
		public View build(LogicalDevice device, Image image) {
			// Validate
			if(release && !(image instanceof TransientNativeObject)) {
				throw new IllegalStateException("Only default images can be released");
			}

			// Build view descriptor
			final var info = new VkImageViewCreateInfo();
			info.sType = VkStructureType.IMAGE_VIEW_CREATE_INFO;
			info.flags = new EnumMask<>();
			info.viewType = Objects.requireNonNullElseGet(type, () -> type(image));
			info.format = image.descriptor().format();
			info.image = image.handle();
			info.components = mapping.build();
			info.subresourceRange = Subresource.range(Objects.requireNonNullElseGet(subresource, image::descriptor));

			// Allocate image view
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateImageView(device, info, null, pointer);

			// Create image view
			return new View(pointer.handle(), device, image, release);
		}

		/**
		 * Helper.
		 * Maps an image type to the corresponding view type.
		 * @param type Image type
		 * @return View type
		 */
		private static VkImageViewType type(Image image) {
			return switch(image.descriptor().type()) {
				case TYPE_1D -> VkImageViewType.TYPE_1D;
				case TYPE_2D -> VkImageViewType.TYPE_2D;
				case TYPE_3D -> VkImageViewType.TYPE_3D;
				default		 -> throw new RuntimeException();
			};
		}
	}

	/**
	 * Image view API.
	 */
	interface Library {
		/**
		 * Creates an image view.
		 * @param device			Logical device
		 * @param pCreateInfo		Image view descriptor
		 * @param pAllocator		Allocator
		 * @param pView				Returned image view handle
		 * @return Result
		 */
		VkResult vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, Pointer pView);

		/**
		 * Destroys an image view.
		 * @param device			Logical device
		 * @param imageView			Image view
		 * @param pAllocator		Allocator
		 */
		void vkDestroyImageView(LogicalDevice device, View imageView, Handle pAllocator);

		/**
		 * Clears a colour attachment.
		 * @param commandBuffer		Command buffer
		 * @param image				Image to clear
		 * @param imageLayout		Image layout
		 * @param pColor			Clear colour
		 * @param rangeCount		Number of sub-resource ranges
		 * @param pRanges			Sub-resource ranges
		 */
		void vkCmdClearColorImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange[] pRanges);
		// TODO
		// TODO - these can only be done outside of a render pass? what are they for?

		/**
		 * Clears the depth-stencil attachment.
		 * @param commandBuffer		Command buffer
		 * @param image				Image to clear
		 * @param imageLayout		Image layout
		 * @param pDepthStencil		Depth clear value
		 * @param rangeCount		Number of sub-resource ranges
		 * @param pRanges			Sub-resource ranges
		 */
		void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange[] pRanges);
		// TODO
	}
}
