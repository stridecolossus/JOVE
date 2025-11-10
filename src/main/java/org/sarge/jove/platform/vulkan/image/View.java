package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;
import org.sarge.jove.util.EnumMask;

/**
 * An <i>image view</i> is a reference to an {@link Image} used as a frame buffer <i>attachment</i>.
 * @see FrameBuffer
 * @author Sarge
 */
public class View extends VulkanObject {
	private final Image image;
	private ClearValue clear;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param dev		Logical device
	 * @param image		Underlying image
	 */
	public View(Handle handle, LogicalDevice dev, Image image) {
		super(handle, dev);
		this.image = requireNonNull(image);
	}

	/**
	 * @return Underlying image
	 */
	public Image image() {
		return image;
	}

	/**
	 * Clear value for this view attachment.
	 * @return Clear value
	 */
	public Optional<ClearValue> clear() {
		return Optional.ofNullable(clear);
	}

	/**
	 * Sets the clear value for this view attachment.
	 * @param clear Clear value or {@code null} if not cleared
	 * @throws IllegalArgumentException if the clear value is incompatible with this view
	 */
	public View clear(ClearValue clear) {
		if(Objects.nonNull(clear)) {
			final VkImageAspect aspect = clear.aspect();
			final boolean valid = image.descriptor().aspects().contains(aspect);
			if(!valid) {
				throw new IllegalArgumentException("Invalid clear value for this view: clear=%s view=%s".formatted(clear, this));
			}
		}

		this.clear = clear;

		return this;
	}

	@Override
	protected Destructor<View> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyImageView;
	}

	/**
	 * Builder for an image view.
	 */
	public static class Builder {
		/**
		 * Helper - Maps an image type to the corresponding view type.
		 * @param type Image type
		 * @return View type
		 */
		private static VkImageViewType type(Image image) {
			return switch(image.descriptor().type()) {
				case ONE_D		-> VkImageViewType.ONE_D;
				case TWO_D		-> VkImageViewType.TWO_D;
				case THREE_D	-> VkImageViewType.THREE_D;
			};
		}

		private VkImageViewType type;
		private ComponentMapping mapping = ComponentMapping.IDENTITY;
		private Subresource subresource;

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			this.type = type;
			return this;
		}
		// TODO - is this ALWAYS that of the image?

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
		 * Sets the image sub-resource for this view.
		 * @param subresource Image sub-resource
		 */
		public Builder subresource(Subresource subresource) {
			this.subresource = subresource;
			return this;
		}

		/**
		 * Constructs this image view.
		 * The image type and sub-resource range are initialised to the given {@link #image} if not already populated.
		 * @param device	Logical device
		 * @param image		Underlying image
		 * @return New image view
		 */
		public View build(LogicalDevice device, Image image) {
			// Build view descriptor
			final var info = new VkImageViewCreateInfo();
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
			return new View(pointer.get(), device, image);
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
