package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.image.*;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class Framebuffer extends VulkanObject {
	private final Handle pass;
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
	Framebuffer(Handle handle, LogicalDevice device, Handle pass, List<View> attachments, Rectangle extents, Library library) {
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
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.renderPass = pass;
		info.framebuffer = this.handle();
		info.renderArea = VulkanUtility.rectangle(extents);

		// Build attachment clear operations
		info.pClearValues = clear();
		info.clearValueCount = info.pClearValues.length;

		// Create command
		return buffer -> library.vkCmdBeginRenderPass(buffer, info, contents);
	}

	/**
	 * @return Clear attachment array
	 */
	private VkClearValue[] clear() {
		return attachments
				.stream()
				.map(View::clear)
				.flatMap(Optional::stream)
				.map(Framebuffer::populate)
				.toArray(VkClearValue[]::new);
	}
	// TODO - needs to have entries even if not cleared?

	/**
	 * Populates an attachment clear descriptor.
	 */
	private static VkClearValue populate(ClearValue clear) {
		final var descriptor = new VkClearValue();
		clear.populate(descriptor);
		return descriptor;
	}

	/**
	 * @return End render pass command
	 */
	public Command end() {
		return library::vkCmdEndRenderPass;
	}

	@Override
	protected Destructor<Framebuffer> destructor() {
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
	public static Framebuffer create(RenderPass pass, Rectangle extents, List<View> attachments) {
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
		final Pointer handle = new Pointer();
		library.vkCreateFramebuffer(device, info, null, handle);

		// Create frame buffer
		return new Framebuffer(handle.get(), device, pass.handle(), attachments, extents, library);
	}

	/**
	 * A <i>framebuffer group</i> is the set of framebuffers for a given swapchain.
	 */
	public static class Group implements TransientObject {
		private final Swapchain swapchain;
		private final RenderPass pass;
		private final List<View> additional;
		private final List<Framebuffer> buffers = new ArrayList<>();

		/**
		 * Constructor.
		 * @param swapchain			Swapchain
		 * @param pass				Render pass
		 * @param additional		Additional attachments
		 */
		public Group(Swapchain swapchain, RenderPass pass, List<View> additional) {
			this.swapchain = requireNonNull(swapchain);
			this.pass = requireNonNull(pass);
			this.additional = List.copyOf(additional);
			build();
		}
		// TODO - swapchain -> factory

		/**
		 * @return Number of frame buffers in this group, i.e. the number of swapchain attachments
		 */
		public int size() {
			return buffers.size();
		}

		/**
		 * Retrieves a framebuffer.
		 * @param index Index
		 * @return Framebuffer
		 * @throws IndexOutOfBoundsException for an invalid index or if this group has been destroyed
		 */
		public Framebuffer get(int index) {
			return buffers.get(index);
		}

		/**
		 * Recreates this group of framebuffers when the swapchain has been invalidated.
		 */
		public void recreate() {
			destroy();
			build();
		}
		// TODO - pass in new swapchain

		/**
		 * Builds the framebuffer group.
		 */
		private void build() {
			final Rectangle extents = swapchain.extents().rectangle();
			final View[] colour = swapchain.attachments().toArray(View[]::new);
			for(int n = 0; n < colour.length; ++n) {
    			// Aggregate attachments
				final List<View> attachments = new ArrayList<>();
    			attachments.add(colour[n]);
    			attachments.addAll(additional);

    			// Create buffer
    			final var buffer = Framebuffer.create(pass, extents, attachments);
    			buffers.add(buffer);
    		}
		}

		@Override
		public void destroy() {
			for(Framebuffer buffer : buffers) {
				buffer.destroy();
			}
			buffers.clear();
		}
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
		void vkDestroyFramebuffer(LogicalDevice device, Framebuffer framebuffer, Handle pAllocator);

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
