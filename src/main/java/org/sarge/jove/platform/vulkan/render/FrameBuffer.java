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
import org.sarge.jove.util.StructureCollector;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
	private final Dimensions extents;

	/**
	 * Constructor.
	 * @param handle 			Handle
	 * @param dev				Logical device
	 * @param pass				Render pass
	 * @param attachments		Attachments
	 * @param extents			Image extents
	 */
	FrameBuffer(Handle handle, DeviceContext dev, RenderPass pass, List<View> attachments, Dimensions extents) {
		super(handle, dev);
		this.pass = notNull(pass);
		this.attachments = List.copyOf(notEmpty(attachments));
		this.extents = notNull(extents);
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
	public Command begin() {
		// Create descriptor
		final var info = new VkRenderPassBeginInfo();
		info.renderPass = pass.handle();
		info.framebuffer = this.handle();

		// Populate rendering area
		final VkExtent2D ext = info.renderArea.extent;
		ext.width = extents.width();
		ext.height = extents.height();
		// TODO - offset => extents is rectangle

		// Build attachment clear operations
		final Collection<ClearValue> clear = attachments
				.stream()
				.map(View::clear)
				.flatMap(Optional::stream)
				.toList();

		// Init clear values
		info.clearValueCount = clear.size();
		info.pClearValues = StructureCollector.pointer(clear, new VkClearValue(), ClearValue::populate);

		// Create command
		return (lib, cmd) -> lib.vkCmdBeginRenderPass(cmd, info, VkSubpassContents.INLINE); // SECONDARY_COMMAND_BUFFERS);
	}
	// TODO - VkSubpassContents as parameter

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
	 * Creates a frame buffer.
	 * @param pass				Render pass
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 * @return New frame buffer
	 * @throws IllegalArgumentException if the number of {@link #attachments} is not the same as the render pass
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
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreateFramebuffer(dev, info, null, ref));

		// Create frame buffer
		return new FrameBuffer(new Handle(ref), dev, pass, attachments, extents);
	}

	/**
	 * A <i>frame buffer group</i> aggregates a set of frame buffers used during rendering.
	 */
	public static class Group implements TransientObject {
		private final List<FrameBuffer> buffers;

    	/**
    	 * Constructor.
    	 * @param pass				Render pass
    	 * @param swapchain			Swapchain
    	 * @param additional		Additional attachments
    	 */
    	public Group(RenderPass pass, Swapchain swapchain, List<View> additional) {
    		final Dimensions extents = swapchain.extents();
    		this.buffers = swapchain
    				.attachments()
    				.stream()
    				.map(col -> join(col, additional))
    				.map(list -> create(pass, extents, list))
    				.toList();
    	}

    	/**
    	 * Convenience constructor for a group of frame buffers with an additional depth-stencil attachment.
    	 * @param pass				Render pass
    	 * @param swapchain			Swapchain
    	 * @param depth				Depth-stencil attachment
    	 */
    	public Group(RenderPass pass, Swapchain swapchain, View depth) {
    		this(pass, swapchain, List.of(depth));
    	}

    	private static List<View> join(View col, List<View> additional) {
    		final List<View> list = new ArrayList<>();
    		list.add(col);
    		list.addAll(additional);
    		return list;
    	}

    	/**
    	 * @return Frame buffers in this group
    	 */
    	public List<FrameBuffer> buffers() {
			return buffers;
		}

    	@Override
    	public void destroy() {
    		for(var b : buffers) {
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
	}
}
