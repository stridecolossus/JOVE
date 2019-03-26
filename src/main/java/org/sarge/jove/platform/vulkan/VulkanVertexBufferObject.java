package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Set;

import org.sarge.jove.model.VertexBufferObject;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan implementation.
 * @author Sarge
 * TODO - test
 */
class VulkanVertexBufferObject extends VulkanHandle implements VertexBufferObject {
	private final int size;
	private final Pointer mem;
	private final Pointer dev;

	/**
	 * Constructor.
	 * @param handle 		VBO handle
	 * @param size			Size of this VBO
	 * @param mem			VBO memory
	 * @param dev			Logical device
	 */
	protected VulkanVertexBufferObject(VulkanHandle handle, int size, Pointer mem, LogicalDevice dev) {
		super(handle);
		this.size = oneOrMore(size);
		this.mem = notNull(mem);
		this.dev = dev.handle();
	}

	/**
	 * @return Size of this VBO
	 */
	public int size() {
		return size;
	}

	@Override
	public void push(ByteBuffer buffer) {
		// Check buffer
		final int len = buffer.capacity();
		if(len > size) throw new IllegalArgumentException(String.format("Buffer exceeds VBO size: buffer=%d vbo=%d", buffer.capacity(), size));

		// Map VBO memory
		final Vulkan vulkan = Vulkan.instance();
		final VulkanLibrary lib = vulkan.library();
		final PointerByReference data = vulkan.factory().reference();
		lib.vkMapMemory(dev, mem, 0, len, 0, data);

		// Copy buffer to VBO memory
		final ByteBuffer bb = data.getValue().getByteBuffer(0, len);
		bb.put(buffer);

		// Cleanup
		lib.vkUnmapMemory(dev, mem);
	}

	@Override
	public Command bind() {
		return (lib, cmd) -> lib.vkCmdBindVertexBuffers(cmd, 0, 1, new Pointer[]{super.handle()}, new long[]{0});
	}

	/**
	 * Builder for a VBO.
	 */
	static class Builder {
		private final LogicalDevice dev;
		private final Set<VkBufferUsageFlag> usage = new StrictSet<>();
		private final Set<VkMemoryPropertyFlag> props = new StrictSet<>();
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
		private int size;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Sets the buffer size.
		 * @param size Buffer size (bytes)
		 */
		public Builder size(int size) {
			this.size = oneOrMore(size);
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
		 * Constructs this VBO.
		 * @return New VBO
		 */
		public VulkanVertexBufferObject build() {
			// Validate
			if(usage.isEmpty()) throw new IllegalArgumentException("No VBO usage flags specified");
			if(size == 0) throw new IllegalArgumentException("Cannot create an empty buffer");

			// Build VBO descriptor
			final VkBufferCreateInfo info = new VkBufferCreateInfo();
			info.usage = IntegerEnumeration.mask(usage);
			info.sharingMode = mode;
			info.size = size;
			// TODO - queue families

			// Allocate VBO
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference buffer = vulkan.factory().reference();
			check(lib.vkCreateBuffer(dev.handle(), info, null, buffer));

			// Query memory requirements for this VBO
			final Pointer handle = buffer.getValue();
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetBufferMemoryRequirements(dev.handle(), handle, reqs);

			// Determine memory type for this VBO
			final int type = dev.parent().selector().findMemoryType(props);

			// Allocate VBO memory
			final PointerByReference mem = vulkan.factory().reference();
			final VkMemoryAllocateInfo alloc = new VkMemoryAllocateInfo();
			alloc.allocationSize = reqs.size;
			alloc.memoryTypeIndex = type;
			check(lib.vkAllocateMemory(dev.handle(), alloc, null, mem));

			// Bind memory
			check(lib.vkBindBufferMemory(dev.handle(), handle, mem.getValue(), 0L));

			// Create VBO
			final Destructor destructor = () -> {
				lib.vkFreeMemory(dev.handle(), mem.getValue(), null);
				lib.vkDestroyBuffer(dev.handle(), handle, null);
			};
			return new VulkanVertexBufferObject(new VulkanHandle(handle, destructor), size, mem.getValue(), dev);
		}
	}
}
