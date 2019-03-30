package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Set;

import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan implementation.
 * <p>
 * Note that this class does not differentiate between index buffers and vertex buffers.
 * The user is responsible for selecting the correct bind command, either {@link #bindVertexBuffer()} or {@link #bindIndexBuffer()}.
 * @author Sarge
 */
class VulkanDataBuffer extends LogicalDeviceHandle implements DataBuffer {
	private final long len;
	private final Pointer mem;

	/**
	 * Constructor.
	 * @param handle 		Buffer handle
	 * @param dev			Logical device
	 * @param len			Length of this buffer (bytes)
	 * @param mem			Buffer memory
	 */
	protected VulkanDataBuffer(Pointer handle, LogicalDevice dev, long len, Pointer mem) {
		super(handle, dev, lib -> lib::vkDestroyBuffer);
		this.len = oneOrMore(len);
		this.mem = notNull(mem);
	}

	/**
	 * @return Length of this buffer
	 */
	public long length() {
		return len;
	}

	@Override
	public void push(ByteBuffer buffer) {
		// Check buffer
		final int actual = buffer.capacity();
		if(actual > len) throw new IllegalArgumentException(String.format("Buffer exceeds length of this data buffer: len=%d max=%d", actual, len));

		// Map buffer memory
		final Vulkan vulkan = dev.parent().vulkan();
		final VulkanLibraryMemory lib = vulkan.library();
		final PointerByReference data = vulkan.factory().reference();
		lib.vkMapMemory(dev.handle(), mem, 0, actual, 0, data);

		// Copy to buffer memory
		final ByteBuffer bb = data.getValue().getByteBuffer(0, actual);
		bb.put(buffer);

		// Cleanup
		lib.vkUnmapMemory(dev.handle(), mem);
	}

	// TODO - add a flag to builder to indicate the buffer purpose?

	/**
	 * Command - Binds this index buffer.
	 * @return Command to bind this index buffer
	 */
	public Command bindVertexBuffer() {
		return (api, cb) -> api.vkCmdBindVertexBuffers(cb, 0, 1, new Pointer[]{super.handle()}, new long[]{0});
	}

	/**
	 * Command - Binds this vertex buffer.
	 * @return Command to bind this vertex buffer
	 * @see #bindIndexBuffer(int)
	 */
	public Command bindIndexBuffer() {
		return bindIndexBuffer(Integer.BYTES);
	}

	/**
	 * Command - Binds this vertex buffer.
	 * @param size Size of the indices
	 * @return Command to bind this vertex buffer
	 * @throws IllegalArgumentException if the size is not {@link Integer#SIZE} or {@link Short#SIZE}
	 * @see #bindIndexBuffer()
	 */
	public Command bindIndexBuffer(int size) {
		if((size != Integer.BYTES) && (size != Short.BYTES)) throw new IllegalArgumentException("Invalid index buffer component size: " + size);
		final VkIndexType type = size == Integer.BYTES ? VkIndexType.VK_INDEX_TYPE_UINT32 : VkIndexType.VK_INDEX_TYPE_UINT16;
		return (api, cb) -> api.vkCmdBindIndexBuffer(cb, super.handle(), 0L, type);
	}

	@Override
	protected void cleanup() {
		final VulkanLibraryMemory lib = dev.parent().vulkan().library();
		lib.vkFreeMemory(dev.handle(), mem, null);
	}

	/**
	 * Builder for a data buffer.
	 */
	public static class Builder {
		protected final LogicalDevice dev;

		private final Set<VkBufferUsageFlag> usage = new StrictSet<>();
		private final Set<VkMemoryPropertyFlag> props = new StrictSet<>();
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
		private long len;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		protected Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Sets the length of this buffer.
		 * @param len Buffer length (bytes)
		 */
		public Builder length(long len) {
			this.len = oneOrMore(len);
			return this;
		}

		/**
		 * Adds a usage flag.
		 * @param usage Usage flag
		 */
		public Builder usage(VkBufferUsageFlag usage) {
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			this.mode = notNull(mode);
			return this;
		}

		/**
		 * Adds a memory property.
		 * @param p Memory property
		 */
		public Builder property(VkMemoryPropertyFlag p) {
			props.add(p);
			return this;
		}

		/**
		 * Constructs this buffer.
		 * @return New data buffer
		 */
		public VulkanDataBuffer build() {
			// Validate
			if(usage.isEmpty()) throw new IllegalArgumentException("No buffer usage flags specified");
			if(len == 0) throw new IllegalArgumentException("Cannot create an empty buffer");

			// Build buffer descriptor
			final VkBufferCreateInfo info = new VkBufferCreateInfo();
			info.usage = IntegerEnumeration.mask(usage);
			info.sharingMode = mode;
			info.size = len;
			// TODO - queue families

			// Allocate buffer
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference buffer = vulkan.factory().reference();
			final Pointer logical = dev.handle();
			check(lib.vkCreateBuffer(logical, info, null, buffer));

			// Query memory requirements
			final Pointer handle = buffer.getValue();
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetBufferMemoryRequirements(logical, handle, reqs);

			// Determine memory type
			final int type = dev.parent().selector().findMemoryType(props);

			// Allocate buffer memory
			final PointerByReference mem = vulkan.factory().reference();
			final VkMemoryAllocateInfo alloc = new VkMemoryAllocateInfo();
			alloc.allocationSize = reqs.size;
			alloc.memoryTypeIndex = type;
			check(lib.vkAllocateMemory(logical, alloc, null, mem));

			// Bind memory
			check(lib.vkBindBufferMemory(logical, handle, mem.getValue(), 0L));

			// Create buffer
			return new VulkanDataBuffer(handle, dev, len, mem.getValue());
		}
	}
}
