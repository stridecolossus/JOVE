package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>Vulkan buffer</i> is used to store and move data on the hardware.
 * @author Sarge
 */
public class VulkanBuffer extends VulkanObject {
	/**
	 * Special case size for the whole of this buffer.
	 */
	public static final long VK_WHOLE_SIZE = (~0L);

	private final Set<VkBufferUsageFlag> usage;
	private final DeviceMemory memory;
	private final long length;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param device		Logical device
	 * @param usage			Usage flags
	 * @param memory		Buffer memory
	 * @param length		Length of this buffer (bytes)
	 */
	public VulkanBuffer(Handle handle, LogicalDevice device, Set<VkBufferUsageFlag> usage, DeviceMemory memory, long length) {
		super(handle, device);
		this.usage = Set.copyOf(usage);
		this.memory = requireNonNull(memory);
		this.length = requireOneOrMore(length);
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
	DeviceMemory memory() {
		return memory;
	}

	/**
	 * @return Length of this buffer
	 */
	public long length() {
		return length;
	}

	/**
	 * Helper.
	 * Validates the given offset for this buffer.
	 * @param offset Buffer offset
	 * @throws IllegalArgumentException if the {@link #offset} exceeds the {@link #length()} of this buffer
	 */
	public void checkOffset(long offset) {
		requireZeroOrMore(offset);
		if(offset >= length) {
			throw new IllegalArgumentException("Invalid buffer offset: offset=%d buffer=%s".formatted(offset, this));
		}
	}

	/**
	 * Validates that this buffer supports the given usage flag(s).
	 * @throws IllegalStateException if this buffer does not support <b>all</b> of the given required {@link #flags}
	 */
	public void require(VkBufferUsageFlag... flags) {
		if(!usage.containsAll(Set.of(flags))) {
			throw new IllegalStateException("Invalid usage for buffer: required=%s buffer=%s".formatted(List.of(flags), this));
		}
	}

	// TODO...

	/**
	 * Helper - Provides access to the underlying buffer (mapping the buffer memory as required).
	 * @return Underlying buffer
	 */
	public ByteBuffer buffer() {
		return memory
				.region()
				.orElseGet(() -> memory.map(0, length))
				.buffer(0, length);
	}

	// ...TODO

	/**
	 * Creates a command to copy the whole of this buffer to the given destination buffer.
	 * @param destination Destination buffer
	 * @return New copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if this buffer is not a source or the given buffer is not a valid destination
	 * @see BufferCopyCommand
	 */
	public BufferCopyCommand copy(VulkanBuffer destination) {
		return BufferCopyCommand.of(this, destination);
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
		// Validate
		require(VkBufferUsageFlag.TRANSFER_DST);
		checkOffset(offset);
		Vulkan.checkAlignment(offset);

		// Validate alignment
		if(size != VK_WHOLE_SIZE) {
			requireOneOrMore(size);
			Vulkan.checkAlignment(size);
		}

		// Create fill command
		final Library library = this.device().library();
		return buffer -> library.vkCmdFillBuffer(buffer, this, offset, size, value);
	}

	@Override
	protected final Destructor<VulkanBuffer> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyBuffer;
	}

	@Override
	protected void release() {
		if(memory.isDestroyed()) {
			return;
		}
		memory.destroy();
	}

//	@Override
//	public boolean equals(Object obj) {
//		return
//				(obj == this) ||
//				(obj instanceof VulkanBuffer that) &&
//				(this.length == that.length()) &&
//				this.usage.equals(that.usage()) &&
//				this.memory.equals(that.memory()) &&
//				super.equals(obj);
//	}

	/**
	 * Creates a buffer.
	 * @param device			Logical device
	 * @param allocator			Memory allocator
	 * @param length			Length (bytes)
	 * @param properties		Memory properties
	 * @return New buffer
	 * @throws IllegalArgumentException if the buffer length is zero or the usage set is empty
	 */
	public static VulkanBuffer create(LogicalDevice device, Allocator allocator, long length, MemoryProperties<VkBufferUsageFlag> properties) {
		// TODO
		if(properties.mode() == VkSharingMode.CONCURRENT) {
			throw new UnsupportedOperationException();
		}
		// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
		// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
		// - queueFamilyIndexCount

		// Build buffer descriptor
		final var info = new VkBufferCreateInfo();
		info.usage = new EnumMask<>(properties.usage());
		info.sharingMode = properties.mode();
		info.size = requireOneOrMore(length);
		// TODO - queue families

		// Allocate buffer
		final Library library = device.library();
		final Pointer pointer = new Pointer();
		library.vkCreateBuffer(device, info, null, pointer);

		// Query memory requirements
		final Handle handle = pointer.get();
		final var requirements = new VkMemoryRequirements();
		library.vkGetBufferMemoryRequirements(device, handle, requirements);

		// Allocate buffer memory
		final DeviceMemory memory = allocator.allocate(requirements, properties);

		// Bind memory
		library.vkBindBufferMemory(device, handle, memory, 0L);

		// Create buffer
		return new VulkanBuffer(handle, device, properties.usage(), memory, length);
	}

	/**
	 * Helper - Creates and initialises a staging buffer containing the given data.
	 * <p>
	 * The staging buffer is a {@link VkBufferUsageFlag#TRANSFER_SRC} with {@link VkMemoryProperty#HOST_VISIBLE} memory.
	 * <p>
	 * @param device			Logical device
	 * @param allocator			Memory allocator
	 * @param data				Data to stage
	 * @return New staging buffer
	 */
	public static VulkanBuffer staging(LogicalDevice device, Allocator allocator, ByteSizedBufferable data) {
		// Init memory properties
		final var propertiess = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();

		// Create staging buffer
		final VulkanBuffer buffer = create(device, allocator, data.length(), propertiess);

		// Write data to buffer
		// TODO...
		final ByteBuffer bb = buffer.buffer();
		data.buffer(bb);
		// ...TODO

		return buffer;
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
		 * @param pBuffer			Returned buffer handle
		 * @return Result
		 */
		VkResult vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Handle pAllocator, Pointer pBuffer);

		/**
		 * Destroys a buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param pAllocator		Allocator
		 */
		void vkDestroyBuffer(LogicalDevice device, VulkanBuffer pBuffer, Handle pAllocator);

		/**
		 * Queries the memory requirements of the given buffer.
		 * @param device					Logical device
		 * @param pBuffer					Buffer
		 * @param pMemoryRequirements		Returned memory requirements
		 */
		void vkGetBufferMemoryRequirements(LogicalDevice device, Handle pBuffer, @Updated VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds the memory for the given buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param memory			Memory
		 * @param memoryOffset		Offset
		 * @return Result
		 */
		VkResult vkBindBufferMemory(LogicalDevice device, Handle pBuffer, DeviceMemory memory, long memoryOffset);

		/**
		 * Binds a vertex buffer.
		 * @param commandBuffer		Command
		 * @param firstBinding		First binding
		 * @param bindingCount		Number of bindings
		 * @param pBuffers			Buffer(s)
		 * @param pOffsets			Buffer offset(s)
		 */
		void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets);

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
