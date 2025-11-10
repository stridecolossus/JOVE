package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.*;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class FrameBuffer extends VulkanObject {
	private final RenderPass pass;
	private final List<View> attachments;
	private final Rectangle extents;
	private final Library library;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param device			Logical device
	 * @param pass				Render pass
	 * @param attachments		Attachments
	 * @param extents			Image extents
	 * @param library			Frame buffer library
	 */
	FrameBuffer(Handle handle, LogicalDevice device, RenderPass pass, List<View> attachments, Rectangle extents, Library library) {
		super(handle, device);
		this.pass = requireNonNull(pass);
		this.attachments = List.copyOf(attachments);
		this.extents = requireNonNull(extents);
		this.library = requireNonNull(library);
	}

	/**
	 * @return Attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * Creates a command to start a render pass with this frame buffer.
	 * @param contents Subpass contents
	 * @return Begin render pass command
	 */
	public Command begin(VkSubpassContents contents) {
		final VkRenderPassBeginInfo info = begin();
		return buffer -> library.vkCmdBeginRenderPass(buffer, info, contents);
	}

	/**
	 * @return End render pass command
	 */
	public Command end() {
		return library::vkCmdEndRenderPass;
	}

	/**
	 * @return Render pass descriptor
	 */
	private VkRenderPassBeginInfo begin() {
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
		info.clearValueCount = info.pClearValues.length;
		info.pClearValues = attachments
				.stream()
				.map(View::clear)
				.flatMap(Optional::stream)
				.map(FrameBuffer::populate)
				.toArray(VkClearValue[]::new);

		return info;
	}

	private static VkClearValue populate(ClearValue clear) {
		final var descriptor = new VkClearValue();
		clear.populate(descriptor);
		return descriptor;
	}

	@Override
	protected Destructor<FrameBuffer> destructor() {
		return library::vkDestroyFramebuffer;
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
		/*
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
			final Dimensions dimensions = descriptor.extents().size();
			if(!extents.dimensions().contains(dimensions)) {
				throw new IllegalArgumentException(String.format("Attachment %d extents must be same or larger than framebuffer: attachment=%s framebuffer=%s", n, dimensions, extents));
			}
		}
		*/

		// Build descriptor
		final var info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = attachments.size();
		info.pAttachments = NativeObject.handles(attachments);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO - layers?

		// Allocate frame buffer
		final LogicalDevice device = pass.device();
		final Library library = device.library();
		final Pointer pointer = new Pointer();
		library.vkCreateFramebuffer(device, info, null, pointer);

		// Create frame buffer
		return new FrameBuffer(pointer.get(), device, pass, attachments, extents, library);
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

		/**
		 * Begins a render pass.
		 * @param commandBuffer			Command buffer
		 * @param pRenderPassBegin		Descriptor
		 * @param contents				Subpass contents
		 */
		void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);

		/**
		 * Ends a render pass.
		 * @param commandBuffer Command buffer
		 */
		void vkCmdEndRenderPass(Buffer commandBuffer);
	}
}
