package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle.HandleArray;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.Request;
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
//	private final long len;
	private final DeviceMemory mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param usage			Usage flags
//	 * @param len			Length (bytes)
	 * @param mem			Buffer memory
	 */
	VulkanBuffer(Pointer handle, LogicalDevice dev, Set<VkBufferUsageFlag> usage, /*long len,*/ DeviceMemory mem) {
		super(handle, dev, dev.library()::vkDestroyBuffer);
		this.usage = Set.copyOf(notEmpty(usage));
//		this.len = oneOrMore(len);
		this.mem = notNull(mem);
	}

	/**
	 * @return Usage flags for this buffer
	 */
	public Set<VkBufferUsageFlag> usage() {
		return usage;
	}

//	/**
//	 * @return Length of this buffer (bytes)
//	 */
//	public long length() {
//		return len;
//	}

	/**
	 * @return Buffer memory
	 */
	public DeviceMemory memory() {
		return mem;
	}

//	/**
//	 * A <i>writable</i> is a segment of this buffer that can be written to.
//	 */
//	public final class Writable {
//		private final Pointer ptr;
//		private final long segment;
//		private final long offset;
//
//		private boolean released;
//
//		/**
//		 * Constructor.
//		 * @param ptr			Mapped memory pointer
//		 * @param segment		Length of this writable segment
//		 * @param offset		Offset
//		 */
//		private Writable(Pointer ptr, long segment, long offset) {
//			this.ptr = notNull(ptr);
//			this.segment = oneOrMore(segment);
//			this.offset = zeroOrMore(offset);
//		}
//
//		/**
//		 * Writes the given array to this buffer.
//		 * @param data Data array
//		 * @throws IllegalArgumentException if the given data array is larger than this segment
//		 * @throws IllegalStateException if this writable has been released
//		 */
//		public void write(byte[] data) {
//			check();
//			check(data.length);
//			ptr.write(offset, data, 0, data.length);
//		}
//
//		/**
//		 * Writes the given byte-buffer to this buffer.
//		 * @param data Byte-buffer
//		 * @throws IllegalArgumentException if the remaining data in the given buffer is larger than this segment
//		 * @throws IllegalStateException if this writable has been released
//		 */
//		public void write(ByteBuffer data) {
//			// Validate
//			final int len = data.remaining();
//			check();
//			check(len);
//
//			// Copy buffer
//			final ByteBuffer dest = ptr.getByteBuffer(offset, len);
//			dest.put(data);
//		}
//
//		/**
//		 * @throws IllegalStateException if this writable has been released
//		 */
//		private void check() {
//			if(released) throw new IllegalStateException("Writable has been released");
//		}
//
//		/**
//		 * @throws IllegalArgumentException if the data is larger than this segment
//		 */
//		private void check(int len) {
//			if(len > segment) throw new IllegalArgumentException(String.format("Data is large than the writable segment: len=%d data=%d", segment, len));
//		}
//
//		/**
//		 * @return Whether this writable has been released
//		 */
//		public boolean isReleased() {
//			return released;
//		}
//
//		/**
//		 * Releases (i.e. unmaps) this writable segment.
//		 * @throws IllegalStateException if this segment has already been released
//		 */
//		public void release() {
//			check();
//			final LogicalDevice dev = device();
//			final VulkanLibrary lib = dev.library();
//			lib.vkUnmapMemory(dev.handle(), mem.handle());
//			released = true;
//		}
//
//		@Override
//		public String toString() {
//			return new ToStringBuilder(this)
//					.append(VulkanBuffer.this)
//					.append("length", segment)
//					.append("offset", offset)
//					.build();
//		}
//	}
//
//	/**
//	 * Maps a segment of this buffer ready for writing.
//	 * <p>
//	 * TODO - release
//	 * <p>
//	 * @param len			Segment length (bytes)
//	 * @param offset		Offset
//	 * @return Writable segment of this buffer
//	 * @throws IllegalArgumentException if the combined segment length and offset is larger than this buffer
//	 */
//	public Writable map(long len, long offset) {
//		// Validate
//		Check.oneOrMore(len);
//		Check.zeroOrMore(offset);
//		if(len + offset > this.len) throw new IllegalArgumentException(String.format("Writable length exceeds buffer: length=%d offset=%d this=%s", len, offset, this));
//
//		// Map buffer memory
//		final LogicalDevice dev = this.device();
//		final VulkanLibrary lib = dev.library();
//		final PointerByReference data = lib.factory().pointer();
//		check(lib.vkMapMemory(dev.handle(), mem.handle(), offset, len, 0, data));
//
//		// Create writable segment
//		return new Writable(data.getValue(), len, offset);
//	}
//
//	/**
//	 * Helper - Maps the entire buffer.
//	 * @return Writable buffer
//	 * @see #map(long, long)
//	 */
//	public Writable map() {
//		return map(len, 0);
//	}

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
				info.range = mem.size();
				write.pBufferInfo = info;
			}
		};
	}

	/**
	 * Creates a command to bind this buffer as a vertex buffer (VBO).
	 * @return Command to bind this buffer
	 * @throws IllegalStateException if this buffer cannot be used as a VBO
	 * @see VkBufferUsageFlag#VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
	 */
	public Command bindVertexBuffer() {
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		// TODO - support binding multiple VBO
		final HandleArray array = Handle.toArray(List.of(this));
		return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
	}

	/**
	 * Creates a command to bind this buffer as an index buffer.
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if this buffer cannot be used as an index
	 * @see VkBufferUsageFlag#VK_BUFFER_USAGE_INDEX_BUFFER_BIT
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
//		if(len > dest.length()) throw new IllegalStateException(String.format("Destination buffer is too small: this=%s dest=%s", this, dest));
		if(mem.size() > dest.memory().size()) throw new IllegalStateException(String.format("Destination buffer is too small: this=%s dest=%s", this, dest));
		require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		dest.require(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT);

		// Build copy descriptor
		final VkBufferCopy region = new VkBufferCopy();
		region.size = mem.size();

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
				.append("mem", mem)
				.append("usage", usage)
				.build();
	}

	/**
	 * Builder for a vertex buffer.
	 */
	public static class Builder {
		private final LogicalDevice dev;
//		private long len;
		private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
		private final Set<VkBufferUsageFlag> usage = new HashSet<>();
//		private final Request.Builder request = new Request.Builder();
		private Request req;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
//			this.request = dev.allocator().request();
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

//		/**
//		 * Adds an <i>optimal</i> memory property.
//		 * @param prop Optimal memory property
//		 */
//		public Builder optimal(VkMemoryPropertyFlag prop) {
//			request.optimal(prop);
//			return this;
//		}
//
//		/**
//		 * Adds a <i>required</i> memory property.
//		 * @param prop Required memory property
//		 */
//		public Builder required(VkMemoryPropertyFlag prop) {
//			request.required(prop);
//			return this;
//		}

		/**
		 * Sets the sharing mode.
		 * @param mode Sharing mode
		 * @return
		 */
		public Builder mode(VkSharingMode mode) {
			this.mode = notNull(mode);
			return this;
		}

//		/**
//		 * Sets the length of this buffer.
//		 * @param len Buffer length (bytes)
//		 */
//		public Builder length(long len) {
//			this.len = oneOrMore(len);
//			return this;
//		}

		public Builder memory(Request req) {
			this.req = notNull(req);
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
//			if(len == 0) throw new IllegalArgumentException("Cannot create an empty buffer");
			if(req == null) throw new IllegalArgumentException("Cannot create an empty buffer"); // TODO

			// TODO
			if(mode == VkSharingMode.VK_SHARING_MODE_CONCURRENT) throw new UnsupportedOperationException();
			// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
			// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
			// - queueFamilyIndexCount

			// Build buffer descriptor
			final VkBufferCreateInfo info = new VkBufferCreateInfo();
			info.usage = IntegerEnumeration.mask(usage);
			info.sharingMode = mode;
			info.size = req.size();
			// TODO - queue families

			// Allocate buffer
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateBuffer(dev.handle(), info, null, handle));

			// Query memory requirements
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);

			// Allocate buffer memory

			// TODO
			final DeviceMemory mem = new DeviceMemory(null, dev, 0);
//			final DeviceMemory mem = request.init(reqs).allocate();

			// Bind memory
			check(lib.vkBindBufferMemory(dev.handle(), handle.getValue(), mem.handle(), 0L));

			// Create buffer
			return new VulkanBuffer(handle.getValue(), dev, usage, mem);
		}
	}
}
