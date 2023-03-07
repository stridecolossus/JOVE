package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.BitMask;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan buffer</i> is used to store arbitrary data on the hardware and to perform copy operations.
 * @author Sarge
 */
public class VulkanBuffer extends VulkanObject {
	/**
	 * Special case size for the whole of this buffer.
	 */
	public static final long VK_WHOLE_SIZE = (~0);

	/**
	 * Creates a buffer.
	 * @param dev			Logical device
	 * @param allocator		Memory allocator
	 * @param len			Length (bytes)
	 * @param props			Memory properties
	 * @return New buffer
	 * @throws IllegalArgumentException if the buffer length is zero or the usage set is empty
	 */
	public static VulkanBuffer create(DeviceContext dev, AllocationService allocator, long len, MemoryProperties<VkBufferUsageFlag> props) {
		// TODO
		if(props.mode() == VkSharingMode.CONCURRENT) throw new UnsupportedOperationException();
		// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
		// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
		// - queueFamilyIndexCount

		// Build buffer descriptor
		final var info = new VkBufferCreateInfo();
		info.usage = BitMask.reduce(props.usage());
		info.sharingMode = props.mode();
		info.size = oneOrMore(len);
		// TODO - queue families

		// Allocate buffer
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreateBuffer(dev, info, null, ref));

		// Query memory requirements
		final Handle handle = new Handle(ref);
		final var reqs = new VkMemoryRequirements();
		lib.vkGetBufferMemoryRequirements(dev, handle, reqs);

		// Allocate buffer memory
		final DeviceMemory mem = allocator.allocate(reqs, props);

		// Bind memory
		check(lib.vkBindBufferMemory(dev, handle, mem, 0L));

		// Create buffer
		return new VulkanBuffer(handle, dev, props.usage(), mem, len);
	}

	/**
	 * Creates and initialises a staging buffer containing the given data.
	 * @param dev			Logical device
	 * @param allocator		Memory allocator
	 * @param data			Data to stage
	 * @return New staging buffer
	 */
	public static VulkanBuffer staging(DeviceContext dev, AllocationService allocator, ByteSizedBufferable data) {
		// Init memory properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();

		// Create staging buffer
		final VulkanBuffer buffer = create(dev, allocator, data.length(), props);

		// Write data to buffer
		final ByteBuffer bb = buffer.buffer();
		data.buffer(bb);

		return buffer;
	}

	private final Set<VkBufferUsageFlag> usage;
	private final DeviceMemory mem;
	private final long len;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param usage			Usage flags
	 * @param mem			Buffer memory
	 * @param len			Length of this buffer (bytes)
	 */
	protected VulkanBuffer(Handle handle, DeviceContext dev, Set<VkBufferUsageFlag> usage, DeviceMemory mem, long len) {
		super(handle, dev);
		this.usage = Set.copyOf(notEmpty(usage));
		this.mem = notNull(mem);
		this.len = oneOrMore(len);
	}

	/**
	 * Copy constructor.
	 * @param buffer Buffer to copy
	 */
	protected VulkanBuffer(VulkanBuffer buffer) {
		this(buffer.handle(), buffer.device(), buffer.usage(), buffer.memory(), buffer.length());
	}

	/**
	 * @return Usage flags for this buffer
	 */
	public Set<VkBufferUsageFlag> usage() {
		return usage;
	}

	/**
	 * @return Buffer memory
	 */
	public DeviceMemory memory() {
		return mem;
	}

	/**
	 * @return Length of this buffer
	 */
	public long length() {
		return len;
	}

	/**
	 * Helper - Validates the given offset for this buffer.
	 * @param offset Buffer offset
	 * @throws IllegalArgumentException if the {@link #offset} exceeds the {@link #length()} of this buffer
	 */
	public void checkOffset(long offset) {
		Check.zeroOrMore(offset);
		if(offset >= len) throw new IllegalArgumentException("Invalid buffer offset: offset=%d buffer=%s".formatted(offset, this));
	}

	/**
	 * Validates that this buffer supports the given usage flag(s).
	 * @throws IllegalStateException if this buffer does not support <b>all</b> of the given required {@link #flags}
	 */
	public final void require(VkBufferUsageFlag... flags) {
		final var required = Set.of(flags);
		if(!usage.containsAll(required)) {
			throw new IllegalStateException("Invalid usage for buffer: required=%s buffer=%s".formatted(required, this));
		}
	}

	/**
	 * Helper - Provides access to the underlying buffer (mapping the buffer memory as required).
	 * @return Underlying buffer
	 */
	protected final ByteBuffer buffer() {
		return mem
				.region()
				.orElseGet(mem::map)
				.buffer();
	}

	/**
	 * Helper - Creates a command to copy the whole of this buffer to the given destination buffer.
	 * @param dest Destination buffer
	 * @return New copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if this buffer is not a source or the given buffer is not a valid destination
	 */
	public BufferCopyCommand copy(VulkanBuffer dest) {
		return BufferCopyCommand.of(this, dest);
	}

	/**
	 * Creates a command to fill this buffer with a given value.
	 * @param offset		Buffer offset
	 * @param size			Number of bytes to fill or {@link #VK_WHOLE_SIZE} to fill to the end of the buffer
	 * @param value			Value to fill
	 * @return Fill command
	 * @throws IllegalArgumentException if {@link #offset} and {@link #size} are larger than this buffer
	 * @throws IllegalArgumentException if {@link #offset} is not a multiple of 4 bytes
	 * @throws IllegalArgumentException if {@link #size} is not {@link #VK_WHOLE_SIZE} and is not a multiple of 4 bytes
	 * @throws IllegalStateException if this buffer was not created as a {@link VkBufferUsageFlag#TRANSFER_DST}
	 */
	public Command fill(long offset, long size, int value) {
		checkOffset(offset);
		VulkanLibrary.checkAlignment(offset);
		if(size != VK_WHOLE_SIZE) {
			Check.oneOrMore(size);
			VulkanLibrary.checkAlignment(size);
		}
		require(VkBufferUsageFlag.TRANSFER_DST);

		return (lib, cmd) -> lib.vkCmdFillBuffer(cmd, this, offset, size, value);
	}

	@Override
	protected Destructor<VulkanBuffer> destructor(VulkanLibrary lib) {
		return lib::vkDestroyBuffer;
	}

	@Override
	protected void release() {
		if(!mem.isDestroyed()) {
			mem.destroy();
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof VulkanBuffer that) &&
				(this.len == that.length()) &&
				this.usage.equals(that.usage()) &&
				this.mem.equals(that.memory()) &&
				super.equals(obj);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("len", len)
				.append("usage", usage)
				.append("mem", mem)
				.build();
	}

	/**
	 * Vulkan buffer API.
	 */
	interface Library {
		/**
		 * Creates a buffer.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pBuffer			Returned buffer
		 * @return Result
		 */
		int vkCreateBuffer(DeviceContext device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);

		/**
		 * Destroys a buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param pAllocator		Allocator
		 */
		void vkDestroyBuffer(DeviceContext device, VulkanBuffer pBuffer, Pointer pAllocator);

		/**
		 * Queries the memory requirements of the given buffer.
		 * @param device					Logical device
		 * @param pBuffer					Buffer
		 * @param pMemoryRequirements		Returned memory requirements
		 */
		void vkGetBufferMemoryRequirements(DeviceContext device, Handle pBuffer, VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds the memory for the given buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param memory			Memory
		 * @param memoryOffset		Offset
		 * @return Result
		 */
		int vkBindBufferMemory(DeviceContext device, Handle pBuffer, DeviceMemory memory, long memoryOffset);

		/**
		 * Binds a vertex buffer.
		 * @param commandBuffer		Command
		 * @param firstBinding		First binding
		 * @param bindingCount		Number of bindings
		 * @param pBuffers			Buffer(s)
		 * @param pOffsets			Buffer offset(s)
		 */
		void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, Pointer pBuffers, long[] pOffsets);

		/**
		 * Binds an index buffer.
		 * @param commandBuffer		Command
		 * @param buffer			Index buffer
		 * @param offset			Offset
		 * @param indexType			Index data-type
		 */
		void vkCmdBindIndexBuffer(Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType);

		/**
		 * Copies a buffer.
		 * @param commandBuffer		Command buffer
		 * @param srcBuffer			Source
		 * @param dstBuffer			Destination
		 * @param regionCount		Number of regions
		 * @param pRegions			Region descriptor(s)
		 */
		void vkCmdCopyBuffer(Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions);

		/**
		 * Fills a buffer with a given value.
		 * @param commandBuffer		Command buffer
		 * @param dstBuffer			Buffer to fill
		 * @param dstOffset			Offset
		 * @param size				Number of bytes to fill
		 * @param data				Value to fill
		 */
		void vkCmdFillBuffer(Buffer commandBuffer, VulkanBuffer dstBuffer, long dstOffset, long size, int data);
	}
}
