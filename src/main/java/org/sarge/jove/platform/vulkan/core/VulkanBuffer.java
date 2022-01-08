package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
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
	 * Creates a vertex buffer.
	 * @param dev			Logical device
	 * @param len			Length (bytes)
	 * @param dev			Memory properties
	 * @return New vertex buffer
	 * @throws IllegalArgumentException if the buffer length is zero or the usage set is empty
	 */
	public static VulkanBuffer create(LogicalDevice dev, AllocationService allocator, long len, MemoryProperties<VkBufferUsageFlag> props) {
		// TODO
		if(props.mode() == VkSharingMode.CONCURRENT) throw new UnsupportedOperationException();
		// - VkSharingMode.VK_SHARING_MODE_CONCURRENT
		// - queue families (unique, < vkGetPhysicalDeviceQueueFamilyProperties)
		// - queueFamilyIndexCount

		// Build buffer descriptor
		final var info = new VkBufferCreateInfo();
		info.usage = IntegerEnumeration.mask(props.usage());
		info.sharingMode = props.mode();
		info.size = oneOrMore(len);
		// TODO - queue families

		// Allocate buffer
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = dev.factory().pointer();
		check(lib.vkCreateBuffer(dev, info, null, handle));

		// Query memory requirements
		final var reqs = new VkMemoryRequirements();
		lib.vkGetBufferMemoryRequirements(dev, handle.getValue(), reqs);

		// Allocate buffer memory
		final DeviceMemory mem = allocator.allocate(reqs, props);

		// Bind memory
		check(lib.vkBindBufferMemory(dev, handle.getValue(), mem, 0L));

		// Create buffer
		return new VulkanBuffer(handle.getValue(), dev, props.usage(), mem, len);
	}

	/**
	 * Helper - Creates and initialises a staging buffer containing the given data.
	 * @param dev		Logical device
	 * @param data		Data to write
	 * @return New staging buffer containing the given data
	 */
	public static VulkanBuffer staging(LogicalDevice dev, AllocationService allocator, Bufferable data) {
		// Init memory properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.required(VkMemoryProperty.HOST_COHERENT)
				.build();

		// Create staging buffer
		final int len = data.length();
		final VulkanBuffer buffer = create(dev, allocator, len, props);

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
	VulkanBuffer(Pointer handle, LogicalDevice dev, Set<VkBufferUsageFlag> usage, DeviceMemory mem, long len) {
		super(handle, dev);
		this.usage = Set.copyOf(notEmpty(usage));
		this.mem = notNull(mem);
		this.len = oneOrMore(len);
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
	 * Validates that this buffer supports the given usage flags.
	 * @throws IllegalStateException if this buffer does not support <b>any</b> of the given usage flags
	 */
	public void require(VkBufferUsageFlag... flags) {
		final Collection<VkBufferUsageFlag> required = Arrays.asList(flags);
		if(Collections.disjoint(required, usage)) {
			throw new IllegalStateException(String.format("Invalid usage for buffer: required=%s buffer=%s", required, this));
		}
	}
	// TODO - should this be ALL?

	/**
	 * Helper - Provides access to the underlying buffer (mapping the buffer memory as required).
	 * @return Underlying buffer
	 */
	public ByteBuffer buffer() {
		final Region region = mem.region().orElseGet(mem::map);
		return region.buffer();
	}

	/**
	 * Creates a descriptor set resource for this buffer of the given type.
	 * @param type			Resource type
	 * @param offset		Buffer offset
	 * @return Buffer resource
	 * @throws IllegalStateException if {@link #type} is not supported by this buffer
	 * @throws IllegalArgumentException if {@link #type} is invalid for buffers
	 * @throws IllegalArgumentException if {@link #offset} is invalid for this buffer
	 */
	public DescriptorResource resource(VkDescriptorType type, long offset) {
		// Map to buffer usage
		final VkBufferUsageFlag usage = switch(type) {
			case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC -> VkBufferUsageFlag.UNIFORM_BUFFER;
			case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> VkBufferUsageFlag.STORAGE_BUFFER;
			// TODO - other buffers, e.g. texel
			default -> throw new IllegalArgumentException("Invalid descriptor type: " + type);
		};

		// Validate
		require(usage);
		validate(offset);

		// Create resource wrapper
		return new DescriptorResource() {
			@Override
			public VkDescriptorType type() {
				return type;
			}

			@Override
			public VkDescriptorBufferInfo populate() {
				final var info = new VkDescriptorBufferInfo();
				info.buffer = handle();
				info.offset = offset;
				info.range = len;					// TODO - maxUniformBufferRange, needs to be a separate parameter otherwise offset MUST be zero!
				return info;
			}
		};
	}

	/**
	 * Creates a command to bind this buffer as a vertex buffer (VBO).
	 * @param binding Binding index
	 * @return Command to bind this buffer
	 * @throws IllegalStateException if this buffer is not a {@link VkBufferUsageFlag#VERTEX_BUFFER}
	 */
	public Command bindVertexBuffer(int binding) {
		return bindVertexBuffers(binding, List.of(this));
	}

	/**
	 * Creates a command to bind a collection of vertex buffers.
	 * @param start 		Start binding index
	 * @param buffers		Buffers to bind
	 * @return Command to bind the given buffers
	 * @throws IllegalStateException if any buffer is not a {@link VkBufferUsageFlag#VERTEX_BUFFER}
	 */
	public static Command bindVertexBuffers(int start, Collection<VulkanBuffer> buffers) {
		for(VulkanBuffer vbo : buffers) {
			vbo.require(VkBufferUsageFlag.VERTEX_BUFFER);
		}
		final Pointer array = NativeObject.array(buffers);
		return (api, cmd) -> api.vkCmdBindVertexBuffers(cmd, start, buffers.size(), array, new long[]{0});
	}
	// TODO - offsets

	/**
	 * Creates a command to bind this buffer as an index buffer.
	 * @param type 			Index data type
	 * @param offset		Buffer offset
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if this buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 */
	public Command bindIndexBuffer(VkIndexType type, long offset) {
		require(VkBufferUsageFlag.INDEX_BUFFER);
		validate(offset);
		// TODO - verify type is logical, but how? buffer memory does not enforce its 'layout'
		return (api, cmd) -> api.vkCmdBindIndexBuffer(cmd, this, offset, type);
	}

	/**
	 * Helper - Creates a command to copy this buffer to the given destination buffer.
	 * Note that this method does not enforce any restrictions on the <i>usage</i> of either buffer (other than they must be a valid source and destination).
	 * @param dest Destination buffer
	 * @return New copy command
	 * @throws IllegalArgumentException if the destination buffer is too small
	 * @throws IllegalStateException if this buffer is not a source or the given buffer is not a destination
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
		 * @return Result code
		 */
		int vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);

		/**
		 * Destroys a buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param pAllocator		Allocator
		 * @return Result code
		 */
		void vkDestroyBuffer(DeviceContext device, VulkanBuffer buffer, Pointer pAllocator);

		/**
		 * Queries the memory requirements of the given buffer.
		 * @param device					Logical device
		 * @param pBuffer					Buffer
		 * @param pMemoryRequirements		Returned memory requirements
		 * @return Result code
		 */
		void vkGetBufferMemoryRequirements(LogicalDevice device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);

		/**
		 * Binds the memory for the given buffer.
		 * @param device			Logical device
		 * @param pBuffer			Buffer
		 * @param memory			Memory
		 * @param memoryOffset		Offset
		 * @return Result code
		 */
		int vkBindBufferMemory(LogicalDevice device, Pointer buffer, DeviceMemory memory, long memoryOffset);

		/**
		 * Binds a vertex buffer.
		 * @param commandBuffer		Command
		 * @param firstBinding		First binding
		 * @param bindingCount		Number of bindings
		 * @param pBuffers			Buffer(s)
		 * @param pOffsets			Buffer offset(s)
		 */
		void vkCmdBindVertexBuffers(Command.Buffer commandBuffer, int firstBinding, int bindingCount, Pointer pBuffers, long[] pOffsets);

		/**
		 * Binds an index buffer.
		 * @param commandBuffer		Command
		 * @param buffer			Index buffer
		 * @param offset			Offset
		 * @param indexType			Index data-type
		 */
		void vkCmdBindIndexBuffer(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType);

		/**
		 * Command to copy a buffer.
		 * @param commandBuffer		Command buffer
		 * @param srcBuffer			Source
		 * @param dstBuffer			Destination
		 * @param regionCount		Number of regions
		 * @param pRegions			Region descriptor(s)
		 */
		void vkCmdCopyBuffer(Command.Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions);
	}
}
