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

	/**
	 * Creates a frame buffer.
	 * @param pass				Render pass
	 * @param extents			Image extents
	 * @param views				Attachments
	 * @return New frame buffer
	 * @throws IllegalArgumentException if the number of images is not the same as the number of attachments in the render pass
	 * @throws IllegalArgumentException if an image is not of the expected format of the corresponding attachment
	 * @throws IllegalArgumentException if an image is smaller than the given extents
	 */
	public static FrameBuffer create(RenderPass pass, Dimensions extents, List<View> views) {
		// Validate attachments
		final List<Attachment> attachments = pass.attachments();
		final int size = attachments.size();
		if(views.size() != size) {
			throw new IllegalArgumentException(String.format("Number of attachments does not match the render pass: actual=%d expected=%d", views.size(), attachments.size()));
		}
		for(int n = 0; n < size; ++n) {
			// Validate matching format
			final Attachment attachment = attachments.get(n);
			final View view = views.get(n);
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

		// Build descriptor
		final var info = new VkFramebufferCreateInfo();
		info.renderPass = pass.handle();
		info.attachmentCount = views.size();
		info.pAttachments = NativeObject.array(views);
		info.width = extents.width();
		info.height = extents.height();
		info.layers = 1; // TODO - layers?

		// Allocate frame buffer
		final DeviceContext dev = pass.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference buffer = dev.factory().pointer();
		check(lib.vkCreateFramebuffer(dev, info, null, buffer));

		// Create frame buffer
		return new FrameBuffer(buffer.getValue(), dev, pass, views, extents);
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

		// TODO
		void vkCmdClearDepthStencilImage(Command.Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
	}
}
