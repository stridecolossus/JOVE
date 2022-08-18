package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 */
public class FrameBuffer extends AbstractVulkanObject {
	/**
	 * Command to end a render pass on this frame-buffer.
	 */
	public static final Command END = (api, buffer) -> api.vkCmdEndRenderPass(buffer);

	/**
	 * Creates a frame buffer.
	 * @param pass				Render pass
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 * @return New frame buffer
	 * @throws IllegalArgumentException if the number of attachments is not the same as the render pass
	 * @throws IllegalArgumentException if an attachment is not of the expected format
	 * @throws IllegalArgumentException if an attachment is smaller than the given extents
	 */
	public static FrameBuffer create(RenderPass pass, Dimensions extents, List<View> attachments) {
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
			if(extents.compareTo(dim) > 0) {
				throw new IllegalArgumentException(String.format("Attachment %d extents must be same or larger than framebuffer: attachment=%s framebuffer=%s", n, dim, extents));
			}
		}

		// Build descriptor
		final var info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = attachments.size();
		info.pAttachments = NativeObject.array(attachments);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO - layers?

		// Allocate frame buffer
		final DeviceContext dev = pass.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference buffer = dev.factory().pointer();
		check(lib.vkCreateFramebuffer(dev, info, null, buffer));

		// Create frame buffer
		return new FrameBuffer(buffer.getValue(), dev, pass, attachments, extents);
	}

	private final RenderPass pass;
	private final List<View> attachments;
	private final Dimensions extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param pass				Render pass
	 * @param attachments		Attachments
	 * @param extents			Image extents
	 */
	FrameBuffer(Pointer handle, DeviceContext dev, RenderPass pass, List<View> attachments, Dimensions extents) {
		super(handle, dev);
		this.extents = notNull(extents);
		this.attachments = List.copyOf(notEmpty(attachments));
		this.pass = notNull(pass);
	}

	/**
	 * @return Attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * Creates a command to begin rendering.
	 * @return Begin rendering command
	 * @see #END
	 */
	public Command begin() {
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.renderPass = pass.handle();
		info.framebuffer = this.handle();

		// Populate rendering area
		final VkExtent2D ext = info.renderArea.extent;
		ext.width = extents.width();
		ext.height = extents.height();
		// TODO - offset

		// Map attachments to clear values
		final Collection<ClearValue> clear = attachments
				.stream()
				.map(View::clear)
				.flatMap(Optional::stream)
				.toList();

		// Init clear values
		info.clearValueCount = clear.size();
		info.pClearValues = StructureHelper.pointer(clear, VkClearValue::new, ClearValue::populate);

		// Create command
		return (lib, cmd) -> lib.vkCmdBeginRenderPass(cmd, info, VkSubpassContents.INLINE);
	}

	@Override
	protected Destructor<FrameBuffer> destructor(VulkanLibrary lib) {
		return lib::vkDestroyFramebuffer;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("pass", pass)
				.append("extents", extents)
				.append("attachments", attachments)
				.build();
	}

	/**
	 * A <i>frame set</i> is a convenience aggregation of a group of frame buffers for a given swapchain.
	 */
	public static class Group implements TransientObject {
		private final Swapchain swapchain;
		private final List<FrameBuffer> buffers;

		/**
		 * Constructor.
		 * @param swapchain			Swapchain
		 * @param pass				Render pass
		 * @param additional		Additional attachments
		 */
		public Group(Swapchain swapchain, RenderPass pass, List<View> additional) {
			// Init buffers
			final List<View> images = swapchain.attachments();
			this.buffers = new ArrayList<>(images.size());
			this.swapchain = notNull(swapchain);

			// Create buffers
			final Dimensions extents = swapchain.extents();
			for(View image : images) {
				// Enumerate attachments
				final var attachments = new ArrayList<View>();
				attachments.add(image);
				attachments.addAll(additional);

				// Create buffer
				final FrameBuffer buffer = create(pass, extents, attachments);
				buffers.add(buffer);
			}
		}

		/**
		 * @return Swapchain
		 */
		public Swapchain swapchain() {
			return swapchain;
		}

		/**
		 * Retrieves a frame buffer by swapchain index.
		 * @param index Index
		 * @return Frame buffer
		 * @throws IndexOutOfBoundsException for an invalid index
		 */
		public FrameBuffer buffer(int index) {
			return buffers.get(index);
		}

		@Override
		public void destroy() {
			for(FrameBuffer b : buffers) {
				b.destroy();
			}
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
		 * @param pFramebuffer		Returned frame buffer
		 * @return Result
		 */
		int vkCreateFramebuffer(DeviceContext device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);

		/**
		 * Destroys a frame buffer.
		 * @param device			Logical device
		 * @param framebuffer		Frame buffer
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFramebuffer(DeviceContext device, FrameBuffer framebuffer, Pointer pAllocator);

		//void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
	}
}
