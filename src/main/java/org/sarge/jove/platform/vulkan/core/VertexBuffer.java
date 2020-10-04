package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkBufferCopy;
import org.sarge.jove.platform.vulkan.VkBufferCreateInfo;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>vertex buffer</i> is used to copy data to the hardware.
 * @author Sarge
 */
public class VertexBuffer extends AbstractVulkanObject {
	/**
	 * Helper - Creates a staging buffer.
	 * @param dev Logical device
	 * @param len Buffer length (bytes)
	 * @return New staging buffer
	 */
	public static VertexBuffer staging(LogicalDevice dev, int len) {
		return new VertexBuffer.Builder(dev)
				.length(len)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();
	}

	private final long len;
	private final Pointer mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param len			Length (bytes)
	 * @param mem			Memory handle
	 */
	VertexBuffer(Pointer handle, LogicalDevice dev, long len, Pointer mem) {
		super(handle, dev, dev.library()::vkDestroyBuffer);
		this.len = oneOrMore(len);
		this.mem = notNull(mem);
	}

	/**
	 * @return Length of this buffer (bytes)
	 */
	public long length() {
		return len;
	}

	/**
	 * Loads the given source buffer to this vertex buffer.
	 * @param src Source buffer
	 * @throws IllegalStateException if the given buffer exceeds the size of this vertex buffer
	 */
	public void load(ByteBuffer src) {
		// Check buffer
		final int actual = src.remaining();
		if(actual > len) throw new IllegalStateException(String.format("Buffer exceeds length of this data buffer: len=%d max=%d", actual, len));

		// Map buffer memory
		final LogicalDevice dev = this.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference data = lib.factory().pointer();
		check(lib.vkMapMemory(dev.handle(), mem, 0, actual, 0, data));

		// Copy to memory
		final ByteBuffer bb = data.getValue().getByteBuffer(0, actual);
		bb.put(src);

		// Cleanup
		lib.vkUnmapMemory(dev.handle(), mem);
	}

	/**
	 * @return Command to bind this buffer
	 */
	public Command bind() {
		final Pointer handles = Handle.memory(new Handle[]{this.handle()});
		return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, handles, new long[]{0});
	}

	/**
	 * Creates a command to copy this buffer to the given buffer.
	 * @param dest Destination buffer
	 * @return Copy command
	 */
	public Command copy(VertexBuffer dest) {
		final VkBufferCopy region = new VkBufferCopy();
		region.size = len;
		return (api, cb) -> api.vkCmdCopyBuffer(cb, this.handle(), dest.handle(), 1, new VkBufferCopy[]{region});
	}

	@Override
	public synchronized void destroy() {
		final LogicalDevice dev = super.device();
		dev.library().vkFreeMemory(dev.handle(), mem, null);
		super.destroy();
	}

	/**
	 * Builder for a vertex buffer.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final Set<VkBufferUsageFlag> usage = new HashSet<>();
		private final Set<VkMemoryPropertyFlag> props = new HashSet<>();
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
		private long len;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Adds a usage flag for this buffer.
		 * @param usage Usage flag
		 */
		public Builder usage(VkBufferUsageFlag usage) {
			Check.notNull(usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Adds a memory property for this buffer.
		 * @param prop Memory property
		 */
		public Builder property(VkMemoryPropertyFlag prop) {
			Check.notNull(props);
			this.props.add(prop);
			return this;
		}

		/**
		 * Sets the sharing mode.
		 * @param mode Sharing mode
		 * @return
		 */
		public Builder mode(VkSharingMode mode) {
			this.mode = notNull(mode);
			return this;
		}

		/**
		 * Sets the length of this buffer.
		 * @param len Buffer length (bytes)
		 */
		public Builder length(int len) {
			this.len = oneOrMore(len);
			return this;
		}

		/**
		 * Constructs this vertex buffer.
		 * @return New vertex buffer
		 * @throws IllegalArgumentException if the buffer length is zero or no usage flags are specified
		 */
		public VertexBuffer build() {
			// Validate
			if(usage.isEmpty()) throw new IllegalArgumentException("No buffer usage flags specified");
			if(len == 0) throw new IllegalArgumentException("Cannot create an empty buffer");

			// TODO
			if(mode == VkSharingMode.VK_SHARING_MODE_CONCURRENT) throw new UnsupportedOperationException();
			// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
			// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
			// - queueFamilyIndexCount

			// Build buffer descriptor
			final VkBufferCreateInfo info = new VkBufferCreateInfo();
			info.usage = IntegerEnumeration.mask(usage);
			info.sharingMode = mode;
			info.size = len;
			// TODO - queue families

			// Allocate buffer
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateBuffer(dev.handle(), info, null, handle));

			// Query memory requirements
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);

			// Allocate buffer memory
			final Pointer mem = dev.allocate(reqs, props);

			// Bind memory
			check(lib.vkBindBufferMemory(dev.handle(), handle.getValue(), mem, 0L));

			// Create buffer
			return new VertexBuffer(handle.getValue(), dev, len, mem);
		}
	}
}
