package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan buffer</i> is used to copy data to/from the hardware.
 * @author Sarge
 */
public class VulkanBuffer extends AbstractVulkanObject {
	/**
	 * Creates a buffer.
	 * @param dev			Logical device
	 * @param allocator		Memory allocator
	 * @param len			Length (bytes)
	 * @param dev			Memory properties
	 * @return New buffer
	 * @throws IllegalArgumentException if the buffer length is zero or the usage set is empty
	 */
	public static VulkanBuffer create(DeviceContext dev, long len, MemoryProperties<VkBufferUsageFlag> props) {
		// TODO
		if(props.mode() == VkSharingMode.CONCURRENT) throw new UnsupportedOperationException();
		// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
		// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
		// - queueFamilyIndexCount

		// Build buffer descriptor
		final var info = new VkBufferCreateInfo();
		info.usage = IntegerEnumeration.reduce(props.usage());
		info.sharingMode = props.mode();
		info.size = oneOrMore(len);
		// TODO - queue families

		// Allocate buffer
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreateBuffer(dev, info, null, ref));

		// Query memory requirements
		final var reqs = new VkMemoryRequirements();
		lib.vkGetBufferMemoryRequirements(dev, ref.getValue(), reqs);

		// Allocate buffer memory
		final DeviceMemory mem = dev.allocator().allocate(reqs, props);

		// Bind memory
		check(lib.vkBindBufferMemory(dev, ref.getValue(), mem, 0L));

		// Create buffer
		final Handle handle = new Handle(ref.getValue());
		return new VulkanBuffer(handle, dev, props.usage(), mem, len);
	}

	/**
	 * Helper - Creates and initialises a staging buffer containing the given data.
	 * @param dev			Logical device
	 * @param data			Data to write
	 * @return New staging buffer containing the given data
	 */
	public static VulkanBuffer staging(DeviceContext dev, Bufferable data) {
		// Init memory properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.required(VkMemoryProperty.HOST_COHERENT)
				.build();

		// Create staging buffer
		final int len = data.length();
		final VulkanBuffer buffer = create(dev, len, props);

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
	public void validate(long offset) {
		Check.zeroOrMore(offset);
		if(offset >= len) throw new IllegalArgumentException(String.format("Invalid buffer offset: offset=%d buffer=%s", offset, this));
	}

	/**
	 * Validates that this buffer supports the given usage flag(s).
	 * @throws IllegalStateException if this buffer does not support <b>all</b> of the given usage flags
	 */
	public void require(VkBufferUsageFlag... flags) {
		final Collection<VkBufferUsageFlag> required = Arrays.asList(flags);
		if(!usage.containsAll(required)) {
			throw new IllegalStateException(String.format("Invalid usage for buffer: required=%s buffer=%s", required, this));
		}
	}

	/**
	 * Helper - Provides access to the underlying buffer (mapping the buffer memory as required).
	 * @return Underlying buffer
	 */
	public ByteBuffer buffer() {
		final Region region = mem.region().orElseGet(mem::map);
		return region.buffer();
	}

	/**
	 * Helper - Creates a command to copy this buffer to the given destination buffer.
	 * Note that this method does not enforce any restrictions on the <i>usage</i> of either buffer (other than they must be a valid source and destination).
	 * @param dest Destination buffer
	 * @return New copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if this buffer is not a source or the given buffer is not a valid destination
	 */
	public BufferCopyCommand copy(VulkanBuffer dest) {
		return BufferCopyCommand.of(this, dest);
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
		void vkDestroyBuffer(DeviceContext device, VulkanBuffer buffer, Pointer pAllocator);

		/**
		 * Queries the memory requirements of the given buffer.
		 * @param device					Logical device
		 * @param pBuffer					Buffer
		 * @param pMemoryRequirements		Returned memory requirements
		 */
		void vkGetBufferMemoryRequirements(DeviceContext device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds the memory for the given buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param memory			Memory
		 * @param memoryOffset		Offset
		 * @return Result
		 */
		int vkBindBufferMemory(DeviceContext device, Pointer buffer, DeviceMemory memory, long memoryOffset);

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
	}
}
