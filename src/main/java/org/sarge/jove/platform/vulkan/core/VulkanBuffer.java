package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags.*;
import static org.sarge.jove.platform.vulkan.core.Vulkan.checkAlignment;
import static org.sarge.jove.util.Validation.*;

import java.lang.foreign.*;
import java.nio.*;
import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>Vulkan buffer</i> is used to transfer data to and from the hardware.
 * @author Sarge
 */
public class VulkanBuffer extends VulkanObject {
	/**
	 * Special case size for the whole of this buffer.
	 */
	public static final long VK_WHOLE_SIZE = (~0L);

	private final Set<VkBufferUsageFlags> usage;
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
	protected VulkanBuffer(Handle handle, LogicalDevice device, Set<VkBufferUsageFlags> usage, DeviceMemory memory, long length) {
		super(handle, device);
		this.usage = Set.copyOf(usage);
		this.memory = requireNonNull(memory);
		this.length = requireOneOrMore(length);
	}

	/**
	 * @return Usage flags for this buffer
	 */
	public Set<VkBufferUsageFlags> usage() {
		return usage;
	}

	/**
	 * @return Buffer memory
	 */
	public DeviceMemory memory() {
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
	 * Determines the maximum supported length of a buffer with the given usage.
	 * @param usage		Buffer usage
	 * @param limits	Device limits
	 * @return Maximum length
	 */
	public static int maximum(VkBufferUsageFlags usage, DeviceLimits limits) {
		return switch(usage) {
			case UNIFORM_BUFFER -> limits.get("maxUniformBufferRange");
			case STORAGE_BUFFER -> limits.get("maxStorageBufferRange");
			default -> Integer.MAX_VALUE;
		};
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
	 * @throws IllegalStateException if this buffer does not support <b>all</b> of the required {@link #flags}
	 */
	public void require(VkBufferUsageFlags... flags) {
		if(!usage.containsAll(Set.of(flags))) {
			throw new IllegalStateException("Invalid usage for buffer: required=%s buffer=%s".formatted(List.of(flags), this));
		}
	}

	/**
	 * Maps the memory of this buffer.
	 * @return Mapped buffer memory
	 */
	public MemorySegment map() {
		return memory
				.region()
				.orElseGet(memory::map);
	}

	/**
	 * Helper.
	 * Accesses the entire underlying buffer memory as an NIO buffer, mapping the device memory as required.
	 * @return Underlying byte buffer
	 * @see #map()
	 */
	public ByteBuffer buffer() {
		return this
				.map()
				.asByteBuffer()
				.order(ByteOrder.nativeOrder());
	}

	/**
	 * Helper.
	 * Writes the given data to this buffer.
	 * @param data Data to write
	 * @see #map()
	 */
	public void write(byte[] data) {
		final MemorySegment address = this.map();
		MemorySegment.copy(data, 0, address, ValueLayout.JAVA_BYTE, 0L, data.length);
	}

	/**
	 * Creates a command to copy the whole of this buffer to the given destination.
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
	 * Creates a command to fill this buffer with the given value.
	 * @param offset		Buffer offset
	 * @param size			Number of bytes to fill or {@link #VK_WHOLE_SIZE} to fill to the end of the buffer
	 * @param value			Value to fill
	 * @return Fill command
	 * @throws IllegalArgumentException if {@link #offset} and {@link #size} are larger than this buffer
	 * @throws IllegalArgumentException if {@link #offset} is not a multiple of 4 bytes
	 * @throws IllegalArgumentException if {@link #size} is not {@link #VK_WHOLE_SIZE} and is not a multiple of 4 bytes
	 * @throws IllegalStateException if this buffer was not created as a {@link VkBufferUsageFlags#TRANSFER_DST}
	 */
	public Command fill(long offset, long size, int value) {
		// Validate
		require(VkBufferUsageFlags.TRANSFER_DST);
		checkOffset(offset);
		checkAlignment(offset);

		// Validate alignment
		if(size != VK_WHOLE_SIZE) {
			requireOneOrMore(size);
			checkAlignment(size);
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

	/**
	 * Factory for Vulkan buffers.
	 */
	public record Factory(Allocator allocator) {
		/**
		 * Constructor.
		 * @param allocator Memory allocator
		 */
		public Factory {
			requireNonNull(allocator);
		}

		/**
		 * Creates a buffer.
		 * @param allocator			Memory allocator
		 * @param length			Length (bytes)
		 * @param properties		Memory properties
		 * @return New buffer
		 * @throws IllegalArgumentException if the buffer length is zero or the usage set is empty
		 */
		public VulkanBuffer create(long length, MemoryProperties<VkBufferUsageFlags> properties) {
			// TODO
			if(properties.mode() == VkSharingMode.CONCURRENT) {
				throw new UnsupportedOperationException();
			}
			// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
			// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
			// - queueFamilyIndexCount

			// Build buffer descriptor
			final var info = new VkBufferCreateInfo();
			info.sType = VkStructureType.BUFFER_CREATE_INFO;
			info.flags = new EnumMask<>();
			info.usage = new EnumMask<>(properties.usage());
			info.sharingMode = properties.mode();
			info.size = requireOneOrMore(length);
			// TODO - queue families

			// Allocate buffer
			final LogicalDevice device = allocator.device();
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateBuffer(device, info, null, pointer);

			// Query memory requirements
			final Handle handle = pointer.handle();
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
		 * Helper.
		 * Creates a staging buffer for data to be copied to {@link VkMemoryProperty#DEVICE_LOCAL} memory.
		 * <p>
		 * The buffer has the following properties:
		 * <ul>
		 * <li>{@link VkBufferUsageFlags#TRANSFER_SRC}</li>
		 * <li>{@link VkMemoryProperty#HOST_VISIBLE}</li>
		 * <li>{@link VkMemoryProperty#HOST_COHERENT}</li>
		 * <li>{@link VkMemoryProperty#DEVICE_LOCAL}</li>
		 * </ul>
		 * @param allocator		Memory allocator
		 * @param length		Buffer length
		 * @return New staging buffer
		 */
		public VulkanBuffer staging(long length) {
			final var properties = new MemoryProperties.Builder<VkBufferUsageFlags>()
					.usage(VkBufferUsageFlags.TRANSFER_SRC)
					.required(HOST_VISIBLE)
					.required(HOST_COHERENT)
					.optimal(DEVICE_LOCAL)
					.build();

			return create(length, properties);
		}

		/**
		 * Helper.
		 * Creates and populates a staging buffer.
		 * @param allocator		Memory allocator
		 * @param data			Data to be staged
		 * @return New staging buffer
		 * @see #staging(Allocator, long)
		 */
		public VulkanBuffer staging(MemorySegment data) {
			final var staging = staging(data.byteSize());
			staging.map().copyFrom(data);
			return staging;
		}
	}

	/**
	 * Vulkan buffer API.
	 */
	public interface Library {
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
