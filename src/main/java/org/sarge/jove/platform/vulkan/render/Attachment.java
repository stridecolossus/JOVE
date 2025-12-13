package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.Objects;
import java.util.function.IntFunction;

import org.sarge.jove.common.Colour;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.*;
import org.sarge.jove.util.Percentile;

/**
 * An <i>attachment</i> is a target of the rendering process, such as a swapchain image or the depth-stencil.
 * <p>
 * Note there is no direct equivalent Vulkan type for this class.
 * <p>
 * @author Sarge
 */
public class Attachment {
	/**
	 * Types of attachment.
	 */
	public enum AttachmentType {
		COLOUR,
		DEPTH;

		/**
		 * @return Default clear value for this type of attachment
		 */
		private ClearValue clear() {
			return switch(this) {
				case COLOUR -> new ColourClearValue(Colour.BLACK);
				case DEPTH	-> new DepthClearValue(Percentile.ONE);
			};
		}

		/**
		 * @return Expected attachment type for the given clear value
		 */
		private static AttachmentType expected(ClearValue clear) {
			return switch(clear) {
				case ColourClearValue _		-> COLOUR;
				case DepthClearValue _		-> DEPTH;
			};
		}
	}

	private final AttachmentType type;
	private final AttachmentDescription description;
	private final IntFunction<View> views;

	private String name;
	private ClearValue clear;

	/**
	 * Constructor.
	 * @param type				Type of attachment
	 * @param description		Description
	 * @param views				Views provider
	 */
	public Attachment(AttachmentType type, AttachmentDescription description, IntFunction<View> views) {
		this.type = requireNonNull(type);
		this.description = requireNonNull(description);
		this.views = requireNonNull(views);
		this.clear = type.clear();
		name(type.name().toLowerCase());
		// TODO - validate description vs type?
	}

	/**
	 * @return Name of this attachment
	 */
	public String name() {
		return name;
	}

	/**
	 * Sets the name of this attachment.
	 * @param name Attachment name
	 */
	public void name(String name) {
		this.name = requireNotEmpty(name);
	}

	/**
	 * @return Type of attachment
	 */
	public AttachmentType type() {
		return type;
	}

	/**
	 * @return Attachment description
	 */
	public AttachmentDescription description() {
		return description;
	}

	/**
	 * Retrieves the attachment view for the given framebuffer index.
	 * @param index Framebuffer index
	 * @return Attachment view
	 */
	public View view(int index) {
		return views.apply(index);
	}

	/**
	 * @return Clear value for this attachment
	 */
	public ClearValue clear() {
		return clear;
	}

	/**
	 * Sets the clear value for this attachment.
	 * @param clear Clear value
	 * @throws IllegalArgumentException if {@link #clear} is invalid for this attachment
	 */
	public void clear(ClearValue clear) {
		if(type != AttachmentType.expected(clear)) {
			throw new IllegalArgumentException("Invalid clear value %s for attachment %s".formatted(clear, this));
		}

		this.clear = clear;
	}

	/**
	 * Creates a reference for this attachment with a default image layout.
	 * @return Attachment reference
	 */
	public AttachmentReference reference() {
		final VkImageLayout layout = switch(type) {
			case COLOUR -> VkImageLayout.COLOR_ATTACHMENT_OPTIMAL;
			case DEPTH	-> VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
		};
		return new AttachmentReference(this, layout);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, description);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Attachment that) &&
				(this.type == that.type()) &&
				this.name.equals(that.name()) &&
				this.description.equals(that.description());
	}

	@Override
	public String toString() {
		return name;
	}
}
