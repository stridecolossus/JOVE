package org.sarge.jove.platform.vulkan.core;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkIndexType;

/**
 * An <i>index buffer</i> binds an index to the pipeline.
 * @author Sarge
 */
public class IndexBuffer extends VulkanBuffer {
	private final VkIndexType type;

	/**
	 * Constructor.
	 * @param buffer		Buffer
	 * @param type			Index type
	 * @throws IllegalStateException if the given buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 */
	public IndexBuffer(VulkanBuffer buffer, VkIndexType type) {
		super(buffer);
		this.type = notNull(type);
		require(VkBufferUsageFlag.INDEX_BUFFER);
	}

	/**
	 * @return Index type
	 */
	public VkIndexType type() {
		return type;
	}

	/**
	 * Creates a command to bind this buffer.
	 * @param offset Buffer offset
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if the index is larger than the hardware limit
	 */
	public Command bind(long offset) {
		// Validate index buffer
		validate(offset);

		// Validate draw count
		final long count = this.length() / ((type == VkIndexType.UINT32) ? Integer.BYTES : Short.BYTES);
		final long max = this.device().limits().maxDrawIndexedIndexValue;		// TODO - cloned!!!
		if((max > -1) && (count > max)) {
			throw new IllegalStateException(String.format("Index too large: count=%d max=%d index=%s", count, max, this));
		}
		// TODO - max is unsignedMaximum(32 bits) => -1 !!!
		// TODO - mod by offset?

		// Create command
		return (api, cmd) -> api.vkCmdBindIndexBuffer(cmd, this, offset, type);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IndexBuffer that) &&
				(this.type == that.type()) &&
				super.equals(obj);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(type).build();
	}
}
