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

	private final RenderPass pass;
	private final List<View> attachments;
	private final Dimensions extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param pass				Render pass
	 * @param attachments		Image attachments
	 * @param extents			Image extents
	 */
	FrameBuffer(Pointer handle, DeviceContext dev, RenderPass pass, List<View> attachments, Dimensions extents) {
		super(handle, dev);
		this.extents = notNull(extents);
		this.attachments = List.copyOf(notEmpty(attachments));
		this.pass = notNull(pass);
	}

	/**
	 * @return Image attachments
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
	 * Builder for a <b>group</b> of frame buffers.
	 */
	public static class Builder {
		private RenderPass pass;
		private Dimensions extents;
		private final List<View> attachments = new ArrayList<>();

		/**
		 * Sets the render pass for the frame buffers.
		 * @param pass Render pass
		 */
		public Builder pass(RenderPass pass) {
			this.pass = notNull(pass);
			return this;
		}

		/**
		 * Sets the extents of the frame buffers.
		 * @param extents Extents
		 */
		public Builder extents(Dimensions extents) {
			this.extents = notNull(extents);
			return this;
		}

		/**
		 * Adds an attachment to all frame buffers.
		 * @param attachment Frame buffer attachment
		 */
		public Builder attachment(View attachment) {
			attachments.add(notNull(attachment));
			return this;
		}

		/**
		 * Constructs this frame buffer.
		 * @param attachments Attachments
		 * @return New frame buffer
		 */
		private FrameBuffer create(List<View> attachments) {
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
				final ImageDescriptor descriptor = view.image().descriptor();
				if(attachment.format() != descriptor.format()) {
					throw new IllegalArgumentException(String.format("Invalid attachment %d format: expected=%s actual=%s", n, attachment.format(), descriptor.format()));
				}

				// Validate attachment contains frame-buffer extents
				final Dimensions dim = descriptor.extents().size();
				if(extents.compareTo(dim) > 0) {
					throw new IllegalArgumentException(String.format("Attachment %d extents must be same or larger than framebuffer: attachment=%s framebuffer=%s", n, dim, extents));
				}
			}
			// TODO - samples, aspects(?), layers

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

		/**
		 * Constructs this frame buffer.
		 * @return New frame buffer
		 */
		public FrameBuffer build() {
			return create(this.attachments);
		}

		/**
		 * Constructs multiple frame buffers with the given attachments.
		 * @param attachments Attachments
		 * @return New frame buffers
		 */
		public List<FrameBuffer> build(List<View> attachments) {
			return attachments
					.stream()
					.map(this::concat)
					.map(this::create)
					.toList();
		}

		/**
		 * Adds the given attachment.
		 */
		private List<View> concat(View attachment) {
			final List<View> list = new ArrayList<>(attachments);
			list.add(attachment);
			return list;
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
		 * @return Result code
		 */
		int vkCreateFramebuffer(DeviceContext device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);

		/**
		 * Destroys a frame buffer.
		 * @param device			Logical device
		 * @param framebuffer		Frame buffer
		 * @param pAllocator		Allocator
		 */
		void vkDestroyFramebuffer(DeviceContext device, FrameBuffer framebuffer, Pointer pAllocator);

		// TODO
		void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
	}
}
