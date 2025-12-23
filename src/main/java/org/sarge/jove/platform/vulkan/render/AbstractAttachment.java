package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkAttachmentLoadOp;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;

/**
 * Skeleton implementation that also manages recreation of the attachment image-views.
 * @author Sarge
 */
public abstract class AbstractAttachment extends AbstractTransientObject implements Attachment {
	private final AttachmentType type;
	private final AttachmentDescription description;
	private List<View> views;
	private ClearValue clear = new ClearValue.None();

	/**
	 * Constructor.
	 * @param type				Type of attachment
	 * @param description		Description
	 */
	protected AbstractAttachment(AttachmentType type, AttachmentDescription description) {
		this.type = requireNonNull(type);
		this.description = requireNonNull(description);
	}

	@Override
	public AttachmentType type() {
		return type;
	}

	@Override
	public AttachmentDescription description() {
		return description;
	}

	/**
	 * Creates a reference for this attachment with a default image layout.
	 * @return Attachment reference
	 */
	public abstract Attachment.Reference reference();

	@Override
	public View view(int index) {
		return switch(type) {
			case COLOUR -> views.get(index);
			default -> views.getFirst();
		};
	}

	@Override
	public ClearValue clear() {
		return clear;
	}
	// TODO - throw if none?

	/**
	 * Sets the clear value of this attachment.
	 * @param clear Clear value
	 * @throws IllegalStateException if this attachment is configured to be cleared
	 */
	protected void clear(ClearValue clear) {
		if(description.operation().load() != VkAttachmentLoadOp.CLEAR) {
			throw new IllegalStateException("Attachment is not cleared: " + this);
		}

		this.clear = requireNonNull(clear);
	}

	@Override
	public final void recreate(LogicalDevice device, Dimensions extents) {
		if(views != null) {
			release();
		}

		this.views = views(device, extents);
	}

	/**
	 * Builds the image-views for this attachment.
	 * @param allocator		Memory allocator for attachment image-views
	 * @param extents		Swapchain extents
	 * @return Attachment views
	 */
	protected abstract List<View> views(LogicalDevice device, Dimensions extents);

	@Override
	protected void release() {
		for(View view : views) {
			view.destroy();
		}
		views = null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, description);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof AbstractAttachment that) &&
				(this.type == that.type()) &&
				this.description.equals(that.description());
	}
}
