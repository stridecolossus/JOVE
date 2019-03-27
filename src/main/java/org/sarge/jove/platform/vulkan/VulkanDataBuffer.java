package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Set;

import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.model.IndexBuffer;
import org.sarge.jove.model.VertexBuffer;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Vulkan implementation.
 * @author Sarge
 */
class VulkanDataBuffer extends VulkanHandle implements DataBuffer {
	private final long len;
	private final Pointer mem;
	private final Pointer dev;

	/**
	 * Constructor.
	 * @param handle 		VBO handle
	 * @param len			Length of this VBO
	 * @param mem			VBO memory
	 * @param dev			Logical device
	 */
	protected VulkanDataBuffer(VulkanHandle handle, long len, Pointer mem, LogicalDevice dev) {
		super(handle);
		this.len = oneOrMore(len);
		this.mem = notNull(mem);
		this.dev = dev.handle();
	}

	/**
	 * @return Length of this VBO
	 */
	public long length() {
		return len;
	}

	@Override
	public void push(ByteBuffer buffer) {
		// Check buffer
		final int actual = buffer.capacity();
		if(actual > len) throw new IllegalArgumentException(String.format("Buffer exceeds VBO size: len=%d max=%d", actual, len));

		// Map VBO memory
		final Vulkan vulkan = Vulkan.instance();
		final VulkanLibrary lib = vulkan.library();
		final PointerByReference data = vulkan.factory().reference();
		lib.vkMapMemory(dev, mem, 0, actual, 0, data);

		// Copy buffer to VBO memory
		final ByteBuffer bb = data.getValue().getByteBuffer(0, actual);
		bb.put(buffer);

		// Cleanup
		lib.vkUnmapMemory(dev, mem);
	}


	@Override
	public VertexBuffer toVertexBuffer() {
		return new VertexBuffer() {
			@Override
			public void push(ByteBuffer buffer) {
				push(buffer);
			}

			@Override
			public Command bind() {
				return (api, cb) -> api.vkCmdBindVertexBuffers(cb, 0, 1, new Pointer[]{handle()}, new long[]{0});
			}

			@Override
			public void destroy() {
				VulkanDataBuffer.this.destroy();
			}
		};
	}

	@Override
	public IndexBuffer toIndexBuffer() {
		return new IndexBuffer() {
			@Override
			public void push(ByteBuffer bb) {
				push(bb);
			}

			@Override
			public Command bind() {
				return (api, cb) -> api.vkCmdBindIndexBuffer(cb, handle(), 0L, VkIndexType.VK_INDEX_TYPE_UINT32); // TODO - both sizes
			}

			@Override
			public void destroy() {
				VulkanDataBuffer.this.destroy();
			}
		};
	}

	/**
	 * Builder for a VBO.
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

			// Build VBO descriptor
			final VkBufferCreateInfo info = new VkBufferCreateInfo();
			info.usage = IntegerEnumeration.mask(usage);
			info.sharingMode = mode;
			info.size = len;
			// TODO - queue families

			// Allocate VBO
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference buffer = vulkan.factory().reference();
			final Pointer logical = dev.handle();
			check(lib.vkCreateBuffer(logical, info, null, buffer));

			// Query memory requirements for this VBO
			final Pointer handle = buffer.getValue();
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetBufferMemoryRequirements(logical, handle, reqs);

			// Determine memory type for this VBO
			final int type = dev.parent().selector().findMemoryType(props);

			// Allocate VBO memory
			final PointerByReference mem = vulkan.factory().reference();
			final VkMemoryAllocateInfo alloc = new VkMemoryAllocateInfo();
			alloc.allocationSize = reqs.size;
			alloc.memoryTypeIndex = type;
			check(lib.vkAllocateMemory(logical, alloc, null, mem));

			// Bind memory
			check(lib.vkBindBufferMemory(logical, handle, mem.getValue(), 0L));

			// Create VBO
			final Destructor destructor = () -> {
				lib.vkFreeMemory(logical, mem.getValue(), null);
				lib.vkDestroyBuffer(logical, handle, null);
			};
			return new VulkanDataBuffer(new VulkanHandle(handle, destructor), len, mem.getValue(), dev);
		}
	}
}
