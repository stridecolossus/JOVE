package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Function;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.DepthClearValue;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.*;

/**
 * The <i>depth stencil attachment</i> specifies the properties of the depth-stencil attachment.
 * @author Sarge
 */
public class DepthStencilAttachment extends AbstractAttachment {
	/**
	 * Commonly supported image formats for the depth-stencil attachment.
	 */
	public static final List<VkFormat> IMAGE_FORMATS = List.of(VkFormat.D32_SFLOAT, VkFormat.D32_SFLOAT_S8_UINT, VkFormat.D24_UNORM_S8_UINT);

	/**
	 * Helper.
	 * Selects the image format for a depth-stencil attachment.
	 * @param provider		Format provider
	 * @param formats		Image formats in order of preference
	 * @return Image format
	 * @see #IMAGE_FORMATS
	 */
	public static VkFormat format(Function<VkFormat, VkFormatProperties> provider, List<VkFormat> formats) {
		final var filter = new FormatFilter(provider, true, Set.of(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT));
		final var selector = new PrioritySelector<>(filter);
		return selector.select(formats);
	}

	private final VkFormat format;
	private final Allocator allocator;

	/**
	 * Constructor.
	 * @param format			Image format
	 * @param description		Depth attachment descriptor
	 * @param allocator			Memory allocator for the depth-stencil image
	 * @see #format(Function, List)
	 */
	public DepthStencilAttachment(VkFormat format, AttachmentDescription description, Allocator allocator) {
		super(AttachmentType.DEPTH, description);
		this.format = requireNonNull(format);
		this.allocator = requireNonNull(allocator);
		super.clear(DepthClearValue.DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 * @see #format(Function, List)
	 */
	@Override
	public VkFormat format() {
		return format;
	}

	@Override
	public Attachment.Reference reference() {
		return new Attachment.Reference(this, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
	}

	/**
	 * Sets the clear depth for this attachment.
	 * @param depth Clear depth
	 */
	public void clear(Percentile depth) {
		super.clear(new DepthClearValue(depth));
	}

	@Override
	protected List<View> views(LogicalDevice device, Dimensions extents) {
		// Build depth image descriptor
		final var descriptor = new Image.Descriptor.Builder()
				.aspect(VkImageAspectFlags.DEPTH)
				.format(format)
				.extents(extents)
				.build();

		// The depth buffer is device-local
		final var properties = new MemoryProperties.Builder<VkImageUsageFlags>()
				.usage(VkImageUsageFlags.DEPTH_STENCIL_ATTACHMENT)
				.required(VkMemoryPropertyFlags.DEVICE_LOCAL)
				.build();

		// Create depth buffer image
		final Image image = new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(properties)
		        .tiling(VkImageTiling.OPTIMAL)
				.build(allocator);

		// Wrap as a view
		final View view = new View.Builder()
				.release()
				.build(device, image);

		return List.of(view);
	}
}
