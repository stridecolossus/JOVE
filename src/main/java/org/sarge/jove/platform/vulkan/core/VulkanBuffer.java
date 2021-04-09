package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.DeviceMemory;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle.HandleArray;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan buffer</i> is used to copy data to/from the hardware.
 * @author Sarge
 */
public class VulkanBuffer extends AbstractVulkanObject {
	/**
	 * Helper - Creates a staging buffer.
	 * @param dev Logical device
	 * @param len Buffer length (bytes)
	 * @return New staging buffer
	 */
	public static VulkanBuffer staging(LogicalDevice dev, long len) {
		return new VulkanBuffer.Builder(dev)
				.length(len)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();
	}

	private final Set<VkBufferUsageFlag> usage;
	private final long len;
	private final DeviceMemory mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param usage			Usage flags
	 * @param len			Length (bytes)
	 * @param mem			Memory handle
	 */
	VulkanBuffer(Pointer handle, LogicalDevice dev, Set<VkBufferUsageFlag> usage, long len, DeviceMemory mem) {
		super(handle, dev, dev.library()::vkDestroyBuffer);
		this.usage = Set.copyOf(notEmpty(usage));
		this.len = oneOrMore(len);
		this.mem = notNull(mem);
	}

	/**
	 * @return Usage flags for this buffer
	 */
	public Set<VkBufferUsageFlag> usage() {
		return usage;
	}

	/**
	 * @return Length of this buffer (bytes)
	 */
	public long length() {
		return len;
	}

	/**
	 * Loads the given NIO buffer to this buffer.
	 * @param bb Source buffer
	 */
	public void load(ByteBuffer bb) {
		load(Bufferable.of(bb));
	}

	/**
	 * Loads the given bufferable object to this buffer.
	 * @param buffer Data buffer
	 * @throws IllegalStateException if the size of the object exceeds the length of this vertex buffer
	 */
	public void load(Bufferable obj) {
		load(obj, obj.length(), 0);
	}

	/**
	 * Loads the given bufferable object to this buffer at the specified offset.
	 * @param buffer 		Data buffer
	 * @param offset		Offset into this vertex buffer (bytes)
	 * @throws IllegalStateException if the size of the given object exceeds the length of this vertex buffer
	 */
	public void load(Bufferable obj, long offset) {
		load(obj, obj.length(), offset);
	}

	/**
	 * Loads the given bufferable object to this buffer at the specified offset.
	 * @param buffer 		Bufferable object
	 * @param len			Length of the object (bytes)
	 * @param offset		Offset into this vertex buffer (bytes)
	 * @throws IllegalStateException if the length of given bufferable object exceeds the length of this vertex buffer
	 */
	private void load(Bufferable obj, long len, long offset) {
		// Check buffer
		Check.zeroOrMore(offset);
		if(offset + len > this.len) {
			throw new IllegalStateException(String.format("Buffer exceeds size of this VBO: length=%d offset=%d this=%s", len, offset, this));
		}

		// Map buffer memory
		final LogicalDevice dev = this.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference data = lib.factory().pointer();
		check(lib.vkMapMemory(dev.handle(), mem.handle(), offset, len, 0, data));

		try {
			// Copy to memory
			final ByteBuffer bb = data.getValue().getByteBuffer(0, len);
			obj.buffer(bb);
		}
		finally {
			// Cleanup
			lib.vkUnmapMemory(dev.handle(), mem.handle());
		}
	}

	/**
	 * @return This buffer as a uniform buffer resource
	 */
	public DescriptorSet.Resource uniform() {
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);

		return new DescriptorSet.Resource() {
			@Override
			public VkDescriptorType type() {
				return VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
			}

			@Override
			public void populate(VkWriteDescriptorSet write) {
				final var info = new VkDescriptorBufferInfo();
				info.buffer = handle();
				info.offset = 0;
				info.range = len;
				write.pBufferInfo = info;
			}
		};
	}

	/**
	 * @return Command to bind this buffer
	 */
	public Command bindVertexBuffer() {
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		// TODO - support binding multiple VBO
		final HandleArray array = Handle.toArray(List.of(this));
		return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
	}

	/**
	 * @return Command to bind this index buffer
	 */
	public Command bindIndexBuffer() {
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
		return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
		// TODO - 16/32 depending on size
	}

	/**
	 * Creates a command to copy this buffer to the given buffer.
	 * Note that this method does not enforce any restrictions on the <i>usage</i> of either buffer (other than being a valid source and destination).
	 * @param dest Destination buffer
	 * @return New copy command
	 * @throws IllegalStateException if this buffer is not a source, the given buffer is not a destination, or it is too small
	 */
	public Command copy(VulkanBuffer dest) {
		// Validate
		if(len > dest.length()) throw new IllegalStateException(String.format("Destination buffer is too small: this=%s dest=%s", this, dest));
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		dest.require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT);

		// Build copy descriptor
		final VkBufferCopy region = new VkBufferCopy();
		region.size = len;

		// Create copy command
		return (api, buffer) -> api.vkCmdCopyBuffer(buffer, VulkanBuffer.this.handle(), dest.handle(), 1, new VkBufferCopy[]{region});
	}

	/**
	 * @throws IllegalStateException if this buffer does not support the given usage flag
	 */
	void require(VkBufferUsageFlag flag) {
		if(!usage.contains(flag)) {
			throw new IllegalStateException(String.format("Invalid usage for buffer: usage=%s buffer=%s", flag, this));
		}
	}

	@Override
	protected void release() {
		final LogicalDevice dev = super.device();
		dev.library().vkDestroyBuffer(dev.handle(), this.handle(), null);
		if(!mem.isDestroyed()) {
			mem.destroy();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", this.handle())
				.append("len", len)
				.append("mem", mem.handle())
				.append("usage", usage)
				.build();
	}

	/**
	 * Builder for a vertex buffer.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private long len;
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
		private final Set<VkBufferUsageFlag> usage = new HashSet<>();
		private final VulkanAllocator.Request request;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			this.request = dev.allocator().request();
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
		 * Adds an <i>optimal</i> memory property.
		 * @param prop Optimal memory property
		 */
		public Builder optimal(VkMemoryPropertyFlag prop) {
			request.optimal(prop);
			return this;
		}

		/**
		 * Adds a <i>required</i> memory property.
		 * @param prop Required memory property
		 */
		public Builder required(VkMemoryPropertyFlag prop) {
			request.required(prop);
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
		public Builder length(long len) {
			this.len = oneOrMore(len);
			return this;
		}

		/**
		 * Constructs this vertex buffer.
		 * @return New vertex buffer
		 * @throws IllegalArgumentException if the buffer length is zero or no usage flags are specified
		 */
		public VulkanBuffer build() {
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
			final DeviceMemory mem = request.init(reqs).allocate();

			// Bind memory
			check(lib.vkBindBufferMemory(dev.handle(), handle.getValue(), mem.handle(), 0L));

			// Create buffer
			return new VulkanBuffer(handle.getValue(), dev, usage, len, mem);
		}
	}
}
