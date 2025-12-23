package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Supplier;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.present.Swapchain;

/**
 * A <i>colour attachment</i> specifies the properties of the swapchain colour images.
 * @author Sarge
 */
public class ColourAttachment extends AbstractAttachment {
	private final Supplier<Swapchain> swapchain;

	/**
	 * Constructor.
	 * @param description		Colour attachment description
	 * @param swapchain			Swapchain provider
	 */
	public ColourAttachment(AttachmentDescription description, Supplier<Swapchain> swapchain) {
		super(AttachmentType.COLOUR, description);
		this.swapchain = requireNonNull(swapchain);
		clear(Colour.BLACK);
	}

	@Override
	public VkFormat format() {
		return swapchain.get().format();
	}

	@Override
	public Attachment.Reference reference() {
		return new Attachment.Reference(this, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
	}

	/**
	 * Sets the clear colour for this attachment.
	 * @param colour Clear colour
	 */
	public void clear(Colour colour) {
		super.clear(new ColourClearValue(colour));
	}

	@Override
	protected List<View> views(LogicalDevice device, Dimensions extents) {
		return swapchain
				.get()
				.attachments()
				.stream()
				.map(image -> View.of(device, image))
				.toList();
	}
}
