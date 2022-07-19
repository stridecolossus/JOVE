package org.sarge.jove.platform.vulkan.core;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;

/**
 * An <i>index buffer</i> binds a drawing index to the pipeline.
 * <p>
 * Note that the index is represented as either {@code short} or {@code int} values depending on the length of the vertex data referred to, specified by {@link VkIndexType}.
 * <p>
 * @author Sarge
 */
public class IndexBuffer extends VulkanBuffer {
	private final VkIndexType type;

	/**
	 * Constructor given a specific index data type.
	 * @param buffer		Buffer
	 * @param type			Index type
	 * @throws IllegalStateException if the given buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 * @throws IllegalArgumentException if the given type is invalid
	 */
	public IndexBuffer(VulkanBuffer buffer, VkIndexType type) {
		super(buffer);
		if(type == VkIndexType.NONE_NV) throw new IllegalArgumentException("Invalid index type: " + type);
		this.type = notNull(type);
		require(VkBufferUsageFlag.INDEX_BUFFER);
	}

	/**
	 * Constructor.
	 * @param buffer		Buffer
	 * @param integral		Whether the index is represented by {@code int} or {@code short} values
	 */
	public IndexBuffer(VulkanBuffer buffer, boolean integral) {
		this(buffer, integral ? VkIndexType.UINT32 : VkIndexType.UINT16);
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
	 * @throws IllegalStateException if the index is larger than the {@code maxDrawIndexedIndexValue} hardware limit
	 */
	public Command bind(long offset) {
		validate(offset);
		validateLimit();
		return (api, cmd) -> api.vkCmdBindIndexBuffer(cmd, this, offset, type);
	}

	/**
	 * @throws IllegalStateException if the index is larger than the hardware limit
	 */
	private void validateLimit() {
		// Ignore short index
		if(type == VkIndexType.UINT16) {
			return;
		}

		// Lookup maximum index length
		final long max = this.device().provider().property(new VulkanProperty.Key("maxDrawIndexedIndexValue")).get();

		// Ignore maximum unsigned integer value
		if(max == -1) {
			return;
		}

		// Validate size of this index
		final long count = this.length() / Integer.BYTES;
		if(count > max) {
			throw new IllegalStateException(String.format("Index too large: count=%d max=%s index=%s", count, max, this));
		}
		// TODO - mod by offset?
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
