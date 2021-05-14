package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle.HandleArray;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;

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
	 * @throws IllegalArgumentException if the buffer length is zero
	 */
	public static VulkanBuffer create(LogicalDevice dev, long len, MemoryProperties<VkBufferUsageFlag> props) {
		// TODO
		if(props.mode() == VkSharingMode.VK_SHARING_MODE_CONCURRENT) throw new UnsupportedOperationException();
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
		check(lib.vkCreateBuffer(dev.handle(), info, null, handle));

		// Query memory requirements
		final var reqs = new VkMemoryRequirements();
		lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);

		// Allocate buffer memory
		final DeviceMemory mem = dev.allocate(reqs, props);

		// Bind memory
		check(lib.vkBindBufferMemory(dev.handle(), handle.getValue(), mem.handle(), 0L));

		// Create buffer
		return new VulkanBuffer(handle.getValue(), dev, props.usage(), mem);
	}

	/**
	 * Helper - Creates a staging buffer.
	 * @param dev Logical device
	 * @param len Buffer length (bytes)
	 * @return New staging buffer
	 */
	public static VulkanBuffer staging(LogicalDevice dev, long len) {
		// Init memory properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
				.required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
				.build();

		// Create staging buffer
		return create(dev, len, props);
	}

	private final Set<VkBufferUsageFlag> usage;
	private final DeviceMemory mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param usage			Usage flags
	 * @param mem			Buffer memory
	 */
	VulkanBuffer(Pointer handle, LogicalDevice dev, Set<VkBufferUsageFlag> usage, DeviceMemory mem) {
		super(handle, dev);
		this.usage = Set.copyOf(notEmpty(usage));
		this.mem = notNull(mem);
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
	protected Destructor destructor(VulkanLibrary lib) {
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
				.append("mem", mem)
				.append("usage", usage)
				.build();
	}
}
