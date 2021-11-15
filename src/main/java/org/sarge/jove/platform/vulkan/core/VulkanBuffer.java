package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan buffer</i> is used to copy data to/from the hardware.
 * TODO - doc
 * @author Sarge
 */
public class VulkanBuffer extends AbstractVulkanObject {
	/**
	 * Creates a vertex buffer.
	 * @param dev			Logical device
	 * @param len			Length (bytes)
	 * @param dev			Memory properties
	 * @return New vertex buffer
	 * @throws IllegalArgumentException if the buffer length is zero
	 */
	public static VulkanBuffer create(LogicalDevice dev, AllocationService allocator, long len, MemoryProperties<VkBufferUsage> props) {
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
		final PointerByReference handle = lib.factory().pointer();
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
		final var props = new MemoryProperties.Builder<VkBufferUsage>()
				.usage(VkBufferUsage.TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.required(VkMemoryProperty.HOST_COHERENT)
				.build();

		// Create staging buffer
		final int len = data.length();
		final VulkanBuffer buffer = create(dev, allocator, len, props);

		// Write data to buffer
		final ByteBuffer bb = buffer.memory().map().buffer();
		data.buffer(bb);

		return buffer;
	}

	private final Set<VkBufferUsage> usage;
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
	VulkanBuffer(Pointer handle, LogicalDevice dev, Set<VkBufferUsage> usage, DeviceMemory mem, long len) {
		super(handle, dev);
		this.usage = Set.copyOf(notEmpty(usage));
		this.mem = notNull(mem);
		this.len = oneOrMore(len);
	}

	/**
	 * @return Usage flags for this buffer
	 */
	public Set<VkBufferUsage> usage() {
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
	 * Helper - Provides access to the underlying buffer (mapping the buffer memory as required).
	 * @return Underlying buffer
	 */
	public ByteBuffer buffer() {
		final Region region = mem.region().orElseGet(mem::map);
		return region.buffer();
	}

	/**
	 * A <i>uniform buffer</i> is a descriptor set resource and provides various helpers for updating the uniform buffer object.
	 */
	public class UniformBuffer implements DescriptorResource {
		private final ByteBuffer buffer = buffer();

		@Override
		public VkDescriptorType type() {
			return VkDescriptorType.UNIFORM_BUFFER;
		}

		@Override
		public void populate(VkWriteDescriptorSet write) {
			final var info = new VkDescriptorBufferInfo();
			info.buffer = handle();
			info.offset = 0;
			info.range = len;
			write.pBufferInfo = info;
		}

		/**
		 * Rewinds this uniform buffer.
		 */
		public UniformBuffer rewind() {
			buffer.rewind();
			return this;
		}

		/**
		 * Adds the given data to this uniform buffer.
		 * @param data Data
		 */
		public UniformBuffer append(Bufferable data) {
			data.buffer(buffer);
			return this;
		}

		/**
		 * Inserts a data <i>element</i> into this uniform buffer.
		 * @param index			Element index
		 * @param data			Data
		 */
		public UniformBuffer insert(int index, Bufferable data) {
			final int pos = index * data.length();
			buffer.position(pos);
			data.buffer(buffer);
			return this;
		}
	}

	/**
	 * @return This buffer as a uniform buffer resource
	 * @throws IllegalStateException if this buffer is not a {@link VkBufferUsage#UNIFORM_BUFFER}
	 */
	public UniformBuffer uniform() {
		require(VkBufferUsage.UNIFORM_BUFFER);
		return new UniformBuffer();
	}

	/**
	 * Creates a command to bind this buffer as a vertex buffer (VBO).
	 * @return Command to bind this buffer
	 * @throws IllegalStateException if this buffer cannot be used as a VBO
	 * @see VkBufferUsage#VERTEX_BUFFER
	 */
	public Command bindVertexBuffer() {
		require(VkBufferUsage.VERTEX_BUFFER);
		final Pointer array = NativeObject.toArray(List.of(this));
		return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
		// TODO - batch
	}

	/**
	 * Creates a command to bind this buffer as an index buffer.
	 * @param type Index type
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if this buffer cannot be used as an index
	 * @see VkBufferUsage#INDEX_BUFFER
	 */
	public Command bindIndexBuffer(VkIndexType type) {
		require(VkBufferUsage.INDEX_BUFFER);
		return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this, 0, type);
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

	/**
	 * @throws IllegalStateException if this buffer does not support <b>any</b> of the given usage flags
	 */
	public void require(VkBufferUsage... flags) {
		final boolean contains = Arrays.stream(flags).anyMatch(usage::contains);
		if(!contains) {
			throw new IllegalStateException(String.format("Invalid usage for buffer: usage=%s buffer=%s", Arrays.asList(flags), this));
		}
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
				.append("handle", this.handle())
				.append("len", len)
				.append("usage", usage)
				.append("mem", mem)
				.build();
	}
}
