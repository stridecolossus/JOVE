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
	private final Group group;

	/**
	 * Constructor.
	 * @param handle 		Handle
	 * @param device		Logical device
	 * @param group			Parent group
	 */
	Framebuffer(Handle handle, LogicalDevice device, Group group) {
		super(handle, device);
		this.group = requireNonNull(group);
	}

	public Group group() {
		return group;
	}

	/**
	 * Creates a command to start a render pass with this frame buffer.
	 * @param contents Subpass contents
	 * @return Begin render pass command
	 */
	public Command begin(VkSubpassContents contents) {
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.renderPass = group.pass.handle();
		info.framebuffer = this.handle();
		info.renderArea = VulkanUtility.rectangle(new Rectangle(group.extents));

		// Build attachment clear array
		final var clear = group.clear.values();
		info.clearValueCount = clear.size();
		info.pClearValues = clear.stream().map(ClearValue::populate).toArray(VkClearValue[]::new);

		// Create command
		return buffer -> group.library.vkCmdBeginRenderPass(buffer, info, contents);
	}

	/**
	 * @return End render pass command
	 */
	public Command end() {
		return group.library::vkCmdEndRenderPass;
	}

	@Override
	protected Destructor<Framebuffer> destructor() {
		return group.library::vkDestroyFramebuffer;
	}

//	public static Framebuffer create(RenderPass pass, Rectangle extents, List<View> attachments) {
//		/*
//		// Validate attachments
//		final List<Attachment> expected = pass.attachments();
//		final int size = expected.size();
//		if(attachments.size() != size) {
//			throw new IllegalArgumentException(String.format("Number of attachments does not match the render pass: actual=%d expected=%d", attachments.size(), expected.size()));
//		}
//		for(int n = 0; n < size; ++n) {
//			// Validate matching format
//			final Attachment attachment = expected.get(n);
//			final View view = attachments.get(n);
//			final Descriptor descriptor = view.image().descriptor();
//			if(attachment.format() != descriptor.format()) {
//				throw new IllegalArgumentException(String.format("Invalid attachment %d format: expected=%s actual=%s", n, attachment.format(), descriptor.format()));
//			}
//
//			// Validate attachment contains frame-buffer extents
//			final Dimensions dimensions = descriptor.extents().size();
//			if(!extents.dimensions().contains(dimensions)) {
//				throw new IllegalArgumentException(String.format("Attachment %d extents must be same or larger than framebuffer: attachment=%s framebuffer=%s", n, dimensions, extents));
//			}
//		}

	/**
	 * A <i>framebuffer group</i> is the set of framebuffers for a given swapchain.
	 */
	public static class Group implements TransientObject {
		private final RenderPass pass;
		private final Library library;
		private final View depth;
		private final List<Framebuffer> buffers = new ArrayList<>();
		private final Map<Attachment, ClearValue> clear = new LinkedHashMap<>();
		private Dimensions extents;

		/**
		 * Constructor.
		 * @param swapchain			Swapchain
		 * @param pass				Render pass
		 * @param depth				Optional depth-stencil attachment
		 */
		public Group(Swapchain swapchain, RenderPass pass, View depth) {
			this.pass = requireNonNull(pass);
			this.library = swapchain.device().library();
			this.depth = depth;

			for(Attachment attachment : pass.attachments()) {
				clear.put(attachment, new ClearValue.None());
			}

			build(swapchain);
		}
		// TODO - check 'depth' vs 'attachments'

		/**
		 * @return Framebuffers in this group
		 */
		public List<Framebuffer> buffers() {
			return Collections.unmodifiableList(buffers);
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
		 * Sets the clear value for the given attachment.
		 * @param clear Clear value
		 * @throws IllegalArgumentException if {@link #attachment} does not belong to this framebuffer group
		 * @throws IllegalStateException if the {@link #clear} value does not match the attachment
		 */
		public void clear(Attachment attachment, ClearValue clear) {
			if(!pass.attachments().contains(attachment)) {
				throw new IllegalArgumentException("Invalid attachment for render pass: " + attachment);
			}
			// TODO - validate clear vs attachment => need attachment 'type', also add index to Attachment?
			this.clear.put(attachment, clear);
		}

		/**
		 * Recreates this group of framebuffers when the swapchain has been invalidated.
		 * @param swapchain Swapchain
		 */
		public void recreate(Swapchain swapchain) {
			destroy();
			build(swapchain);
		}

		/**
		 * Builds the framebuffer group.
		 * @param swapchain Swapchain
		 */
		private void build(Swapchain swapchain) {
			final View[] colour = swapchain.attachments().toArray(View[]::new);
			this.extents = swapchain.extents();
			for(int n = 0; n < colour.length; ++n) {
				// Aggregate attachments for this framebuffer
				// TODO - assumes colour is 0, optional depth is 1
				final List<View> attachments = new ArrayList<>();
				attachments.add(colour[n]);
				if(depth != null) {
					attachments.add(depth);
				}

				// Create buffer
				final Framebuffer buffer = create(attachments, extents);
				buffers.add(buffer);
			}
		}

		/**
		 * Creates a new framebuffer.
		 * @param attachments		Attachments
		 * @param extents			Framebuffer extents
		 * @return New framebuffer
		 */
		private Framebuffer create(List<View> attachments, Dimensions extents) {
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
			return new Framebuffer(pointer.handle(), device, this);
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
