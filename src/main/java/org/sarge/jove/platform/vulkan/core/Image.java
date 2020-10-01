package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkExtent3D;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;

/**
 * An <i>image</i> is a descriptor for a Vulkan image.
 * @author Sarge
 */
public class Image extends AbstractVulkanObject {
	/**
	 * Image extents.
	 */
	public record Extents(int width, int height, int depth) {
		/**
		 * Creates a 2D image extents from the given dimensions.
		 * @param dim Image dimensions
		 * @return New extents
		 */
		public static Extents of(Dimensions dim) {
			return new Extents(dim.width(), dim.height());
		}

		/**
		 * Constructor.
		 * @param width
		 * @param height
		 * @param depth
		 */
		public Extents {
			Check.oneOrMore(width);
			Check.zeroOrMore(height);
			Check.oneOrMore(depth);
		}

		/**
		 * Constructor for 2D extents.
		 * @param width
		 * @param height
		 */
		public Extents(int width, int height) {
			this(width, height, 1);
		}

		/**
		 * @return New Vulkan descriptor for this image extents
		 */
		public VkExtent3D create() {
			final VkExtent3D extent = new VkExtent3D();
			extent.width = width;
			extent.height = height;
			extent.depth = depth;
			return extent;
		}
	}

	private final VkFormat format;
	private final Extents extents;
	private final Set<VkImageAspectFlag> aspect;

	private VkImageLayout layout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;

	/**
	 * Constructor.
	 * @param handle		Image handle
	 * @param dev			Logical device
	 * @param format		Image format
	 * @param extents		Image extents
	 * @param aspect		Image aspect(s)
	 */
	public Image(Pointer handle, LogicalDevice dev, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspect) {
		super(handle, dev, dev.library()::vkDestroyImage);
		this.format = notNull(format);
		this.extents = notNull(extents);
		this.aspect = Set.copyOf(aspect);
	}

	/**
	 * @return Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Image extents
	 */
	public Extents extents() {
		return extents;
	}

	/**
	 * @return Image aspect(s)
	 */
	public Set<VkImageAspectFlag> aspect() {
		return aspect;
	}

	/**
	 * @return Current layout of this image
	 */
	public VkImageLayout layout() {
		return layout;
	}

	/**
	 * Creates a view for this image.
	 * @return New view of this image
	 */
	public View view() {
		return new View.Builder().image(this).build();
	}
}
