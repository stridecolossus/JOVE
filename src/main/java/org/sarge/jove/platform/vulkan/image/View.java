package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>image view</i> is a reference to an {@link Image} used as a frame buffer <i>attachment</i>.
 * @see FrameBuffer
 * @author Sarge
 */
public class View extends AbstractVulkanObject {
	/**
	 * Helper - Creates a view for the given image with default configuration.
	 * @param image Image
	 * @return New image view
	 */
	public static View of(Image image) {
		return new Builder(image).build();
	}

	private final Image image;
	private ClearValue clear;

	/**
	 * Constructor.
	 * @param handle 	Image view handle
	 * @param dev		Logical device
	 * @param image		Image
	 */
	View(Pointer handle, DeviceContext dev, Image image) {
		super(handle, dev);
		this.image = notNull(image);
	}

	/**
	 * @return Underlying image
	 */
	public Image image() {
		return image;
	}

	/**
	 * Clear value for this attachment.
	 * @return Clear value
	 */
	public Optional<ClearValue> clear() {
		return Optional.ofNullable(clear);
	}

	/**
	 * Sets the clear value for this attachment.
	 * @param clear Clear value or {@code null} if not cleared
	 * @throws IllegalArgumentException if the clear value is incompatible with this view
	 */
	public View clear(ClearValue clear) {
		if(clear != null) {
			final VkImageAspect expected = switch(clear) {
				case ColourClearValue col -> VkImageAspect.COLOR;
				case DepthClearValue depth -> VkImageAspect.DEPTH;
			};
			if(!image.descriptor().aspects().contains(expected)) {
				throw new IllegalArgumentException(String.format("Invalid clear value for this view: clear=%s view=%s", clear, this));
			}
		}
		this.clear = clear;
		return this;
	}

	@Override
	protected Destructor<View> destructor(VulkanLibrary lib) {
		return lib::vkDestroyImageView;
	}

	@Override
	protected void release() {
		if(image instanceof TransientObject obj) {
			obj.destroy();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("image", image)
				.append("clear", clear)
				.build();
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
		private VkComponentMapping mapping = ComponentMapping.identity();
		private SubResource subresource;

		/**
		 * Constructor.
		 * @param image Image
		 */
		public Builder(Image image) {
			this.image = notNull(image);
			this.type = type(image);
			this.subresource = image.descriptor();
		}

		/**
		 * Sets the view type of this image.
		 * @param type View type
		 */
		public Builder type(VkImageViewType type) {
			this.type = notNull(type);
			return this;
		}

		/**
		 * Sets the component mapping for the view (default is {@link ComponentMapping#IDENTITY}).
		 * @param mapping Component mapping
		 * @see ComponentMapping
		 */
		public Builder mapping(VkComponentMapping mapping) {
			this.mapping = notNull(mapping);
			return this;
		}

		/**
		 * Sets the image sub-resource for this view.
		 * @param subresource Image sub-resource
		 */
		public Builder subresource(SubResource subresource) {
			this.subresource = notNull(subresource);
			return this;
		}

		/**
		 * Constructs this image view.
		 * @return New image view
		 */
		public View build() {
			// Build view descriptor
			final var info = new VkImageViewCreateInfo();
			info.viewType = type;
			info.format = image.descriptor().format();
			info.image = image.handle();
			info.components = mapping;
			info.subresourceRange = SubResource.toRange(subresource);

			// Allocate image view
			final DeviceContext dev = image.device();
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
			check(lib.vkCreateImageView(dev, info, null, handle));

			// Create image view
			return new View(handle.getValue(), dev, image);
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
		 * @return Result code
		 */
		int vkCreateImageView(DeviceContext device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);

		/**
		 * Destroys an image view.
		 * @param device			Logical device
		 * @param imageView			Image view
		 * @param pAllocator		Allocator
		 */
		void vkDestroyImageView(DeviceContext device, View imageView, Pointer pAllocator);
	}
}
