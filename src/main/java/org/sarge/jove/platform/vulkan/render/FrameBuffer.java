package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class FrameBuffer extends VulkanObject {
	/**
	 * Command to end the render pass on this frame buffer.
	 */
	public static final Command END = (lib, buffer) -> lib.vkCmdEndRenderPass(buffer);

	private final RenderPass pass;
	private final List<View> attachments;
	private final Rectangle extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param pass				Render pass
	 * @param attachments		Attachments
	 * @param extents			Image extents
	 */
	FrameBuffer(Handle handle, LogicalDevice dev, RenderPass pass, List<View> attachments, Rectangle extents) {
		super(handle, dev);
		this.pass = requireNonNull(pass);
		this.attachments = List.copyOf(requireNotEmpty(attachments));
		this.extents = requireNonNull(extents);
	}

	/**
	 * @return Attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * Creates a command to begin rendering to this frame buffer.
	 * @return Rendering command
	 * @see #END
	 */
	public Command begin(VkSubpassContents contents) {
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.renderPass = pass.handle();
		info.framebuffer = this.handle();

		// Populate rendering area
		info.renderArea.extent.width = extents.width();
		info.renderArea.extent.height = extents.height();
		info.renderArea.offset.x = extents.x();
		info.renderArea.offset.y = extents.y();

		// Build attachment clear operations
		info.pClearValues = attachments
				.stream()
				.map(View::clear)
				.flatMap(Optional::stream)
				.map(FrameBuffer::populate)
				.toArray(VkClearValue[]::new);

		info.clearValueCount = info.pClearValues.length;

		// Create command
		return (lib, cmd) -> lib.vkCmdBeginRenderPass(cmd, info, contents);
	}

	private static VkClearValue populate(ClearValue clear) {
		final var descriptor = new VkClearValue();
		clear.populate(descriptor);
		return descriptor;
	}

	@Override
	protected Destructor<FrameBuffer> destructor(VulkanLibrary lib) {
		return lib::vkDestroyFramebuffer;
	}

	/**
	 * Creates a frame buffer.
	 * @param pass				Render pass
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 * @return New frame buffer
	 * @throws IllegalArgumentException if the number of {@link #attachments} is not the same as the render pass
	 * @throws IllegalArgumentException if an attachment is not of the expected format
	 * @throws IllegalArgumentException if an attachment is smaller than the given extents
	 */
	public static FrameBuffer create(RenderPass pass, Rectangle extents, List<View> attachments) {
		// Validate attachments
		final List<Attachment> expected = pass.attachments();
		final int size = expected.size();
		if(attachments.size() != size) {
			throw new IllegalArgumentException(String.format("Number of attachments does not match the render pass: actual=%d expected=%d", attachments.size(), expected.size()));
		}
		for(int n = 0; n < size; ++n) {
			// Validate matching format
			final Attachment attachment = expected.get(n);
			final View view = attachments.get(n);
			final Descriptor descriptor = view.image().descriptor();
			if(attachment.format() != descriptor.format()) {
				throw new IllegalArgumentException(String.format("Invalid attachment %d format: expected=%s actual=%s", n, attachment.format(), descriptor.format()));
			}

			// Validate attachment contains frame-buffer extents
			final Dimensions dim = descriptor.extents().size();
			if(extents.dimensions().compareTo(dim) > 0) {
				throw new IllegalArgumentException(String.format("Attachment %d extents must be same or larger than framebuffer: attachment=%s framebuffer=%s", n, dim, extents));
			}
		}

		// Build descriptor
		final var info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = attachments.size();
		info.pAttachments = null; // TODO NativeObject.array(attachments);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO - layers?

		// Allocate frame buffer
		final LogicalDevice dev = pass.device();
		final VulkanLibrary vulkan = dev.vulkan();
		final Pointer ref = new Pointer();
		vulkan.vkCreateFramebuffer(dev, info, null, ref);

		// Create frame buffer
		return new FrameBuffer(ref.get(), dev, pass, attachments, extents);
	}

	/**
	 * Frame buffer API.
	 */
	interface Library {
		/**
		 * Creates a frame buffer.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pFramebuffer		Returned frame buffer handle
		 * @return Result
		 */
		VkResult vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pFramebuffer);

		/**
		 * Destroys a frame buffer.
		 * @param device			Logical device
		 * @param framebuffer		Frame buffer
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFramebuffer(LogicalDevice device, FrameBuffer framebuffer, Handle pAllocator);
	}
}
