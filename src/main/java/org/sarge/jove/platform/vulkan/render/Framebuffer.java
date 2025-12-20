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
import org.sarge.jove.platform.vulkan.present.Swapchain;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class Framebuffer extends VulkanObject {
	private final Library library;
	private final RenderPass pass;
	private final VkRect2D extents;

	/**
	 * Constructor.
	 * @param handle 		Handle
	 * @param device		Logical device
	 * @param pass			Render pass
	 * @param extents		Framebuffer extents
	 */
	Framebuffer(Handle handle, LogicalDevice device, RenderPass pass, Dimensions extents) {
		super(handle, device);
		this.library = device.library();
		this.pass = requireNonNull(pass);
		this.extents = Vulkan.rectangle(new Rectangle(extents));
	}

	/**
	 * Creates a command to start a render pass with this frame buffer.
	 * @param contents Subpass contents
	 * @return Begin render pass command
	 */
	public Command begin(VkSubpassContents contents) {
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.sType = VkStructureType.RENDER_PASS_BEGIN_INFO;
		info.renderPass = pass.handle();
		info.framebuffer = this.handle();
		info.renderArea = extents;

		// Build attachment clear array
		info.pClearValues = clear();
		info.clearValueCount = info.pClearValues.length;

		// Create command
		return buffer -> library.vkCmdBeginRenderPass(buffer, info, contents);
	}

	/**
	 * @return Attachment clear values
	 */
	private VkClearValue[] clear() {
		return pass
				.attachments()
				.stream()
				.map(Attachment::clear)
				.map(ClearValue::populate)
				.toArray(VkClearValue[]::new);
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
	 * The <i>frame buffer factory</i> generates new framebuffer instances.
	 */
	public static class Factory implements TransientObject {
		private final RenderPass pass;
		private final List<Framebuffer> framebuffers = new ArrayList<>();

		/**
		 * Constructor.
		 * @param pass Render pass
		 */
		public Factory(RenderPass pass) {
			this.pass = requireNonNull(pass);
		}

		/**
		 * Retrieves the framebuffer with the given index.
		 * @param index Framebuffer index
		 * @return Framebuffer
		 * @throws IndexOutOfBoundsException if the index is invalid
		 */
		public Framebuffer framebuffer(int index) {
			return framebuffers.get(index);
		}

		/**
		 * Recreates the framebuffers for the given swapchain.
		 * @param swapchain Swapchain
		 */
		public void build(Swapchain swapchain) {
			destroy();
			create(swapchain);
		}

		/**
		 * Recreates the framebuffers.
		 */
		private void create(Swapchain swapchain) {
			final int count = swapchain.attachments().size();
			final Dimensions extents = swapchain.extents();
			for(int n = 0; n < count; ++n) {
				final List<View> views = views(n);
				final Framebuffer buffer = create(views, extents);
				framebuffers.add(buffer);
			}
		}

		/**
		 * @return Attachment views for the given framebuffer index
		 */
		private List<View> views(int index) {
			return pass
					.attachments()
					.stream()
					.map(attachment -> attachment.view(index))
					.toList();
		}

		/**
		 * Creates a new framebuffer.
		 */
		private Framebuffer create(List<View> views, Dimensions extents) {
			// Build descriptor
			final var info = new VkFramebufferCreateInfo();
			info.sType = VkStructureType.FRAMEBUFFER_CREATE_INFO;
			info.flags = new EnumMask<>();
			info.renderPass = pass.handle();
			info.attachmentCount = views.size();
			info.pAttachments = NativeObject.handles(views);
			info.width = extents.width();
			info.height = extents.height();
			info.layers = 1; // TODO - layers?

			// Allocate frame buffer
			final LogicalDevice device = pass.device();
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateFramebuffer(device, info, null, pointer);

			// Create frame buffer
			return new Framebuffer(pointer.handle(), device, pass, extents);
		}

		@Override
		public void destroy() {
			for(Framebuffer buffer : framebuffers) {
				buffer.destroy();
			}
			framebuffers.clear();
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
