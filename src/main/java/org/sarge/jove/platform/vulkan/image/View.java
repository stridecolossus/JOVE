package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.nio.Buffer;
import java.util.Optional;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> is a reference to an {@link Image} used as a frame buffer <i>attachment</i>.
 * @see FrameBuffer
 * @author Sarge
 */
public final class View extends VulkanObject {
	private final Image image;
	private ClearValue clear;
	private boolean auto = true;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param dev		Logical device
	 * @param image		Underlying image
	 */
	View(Handle handle, DeviceContext dev, Image image) {
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
		if(!isValid(clear)) throw new IllegalArgumentException("Invalid clear value for this view: clear=%s view=%s".formatted(clear, this));
		this.clear = clear;
		return this;
	}

	private boolean isValid(ClearValue clear) {
		if(clear == null) {
			return true;
		}
		else {
			return image.descriptor().aspects().contains(clear.aspect());
		}
	}

	/**
	 * Sets whether the underlying image is automatically released when this view is destroyed (default is {@code true}).
	 * @param auto Whether to automatically destroy the underlying image
	 */
	public void setDestroyImage(boolean auto) {
		this.auto = auto;
	}

	@Override
	protected Destructor<View> destructor(VulkanLibrary lib) {
		return lib::vkDestroyImageView;
	}

	@Override
	protected void release() {
		if(auto && (image instanceof TransientObject obj) && !obj.isDestroyed()) {
			obj.destroy();
		}
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

		private final Image image;
		private VkImageViewType type;
		private ComponentMapping mapping = ComponentMapping.IDENTITY;
		private SubResource subresource;

		/**
		 * Constructor.
		 * @param image Image
		 */
		public Builder(Image image) {
			this.image = requireNonNull(image);
			this.type = type(image);
			this.subresource = image.descriptor();
		}

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			this.type = requireNonNull(type);
			return this;
		}

		/**
		 * Sets the component mapping for the view (default is {@link ComponentMapping#IDENTITY}).
		 * @param mapping Component mapping
		 */
		public Builder mapping(ComponentMapping mapping) {
			this.mapping = requireNonNull(mapping);
			return this;
		}

		/**
		 * Sets the image sub-resource for this view.
		 * @param subresource Image sub-resource
		 */
		public Builder subresource(SubResource subresource) {
			this.subresource = requireNonNull(subresource);
			return this;
		}

		/**
		 * Constructs this image view.
		 * @param dev Logical device
		 * @return New image view
		 */
		public View build(DeviceContext dev) {
			// Build view descriptor
			final var info = new VkImageViewCreateInfo();
			info.viewType = type;
			info.format = image.descriptor().format();
			info.image = image.handle();
			info.components = mapping.build();
			info.subresourceRange = SubResource.toRange(subresource);

			// Allocate image view
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateImageView(dev, info, null, ref));

			// Create image view
			return new View(new Handle(ref), dev, image);
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
		 * @param pView				Returned image view
		 * @return Result
		 */
		int vkCreateImageView(DeviceContext device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);

		/**
		 * Destroys an image view.
		 * @param device			Logical device
		 * @param imageView			Image view
		 * @param pAllocator		Allocator
		 */
		void vkDestroyImageView(DeviceContext device, View imageView, Pointer pAllocator);

		/**
		 * Clears a colour attachment.
		 * @param commandBuffer		Command buffer
		 * @param image				Image to clear
		 * @param imageLayout		Image layout
		 * @param pColor			Clear colour
		 * @param rangeCount		Number of sub-resource ranges
		 * @param pRanges			Sub-resource ranges
		 */
		void vkCmdClearColorImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange pRanges);
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
		void vkCmdClearDepthStencilImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
		// TODO
	}
}
