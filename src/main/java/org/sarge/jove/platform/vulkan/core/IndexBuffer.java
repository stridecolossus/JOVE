package org.sarge.jove.platform.vulkan.core;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.util.BufferHelper;

/**
 * An <i>index buffer</i> binds an index to the pipeline.
 * <p>
 * An index buffer is represented as {@code short} or {@code int} values depending on the length of the data, specified by {@link VkIndexType}.
 * <p>
 * The {@link #IndexBuffer(VulkanBuffer)} constructor assumes that a buffer that is longer than an <b>unsigned short</b> has a {@link VkIndexType#UINT32} data type.
 * <p>
 * This behaviour can be overridden by the alternative {@link #IndexBuffer(VulkanBuffer, VkIndexType)} constructor.
 * <p>
 * @see BufferHelper#isShortIndex(long)
 * @author Sarge
 */
public class IndexBuffer extends VulkanBuffer {
	private final VkIndexType type;

	/**
	 * Constructor given a specific index data type.
	 * @param buffer		Buffer
	 * @param type			Index type
	 * @throws IllegalStateException if the given buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 * @throws IllegalArgumentException if {@link #type} is invalid
	 * @throws IllegalArgumentException if {@link #type} is {@link VkIndexType#UINT16} and the index is larger than this data type
	 */
	public IndexBuffer(VulkanBuffer buffer, VkIndexType type) {
		super(buffer);
		this.type = notNull(type);
		require(VkBufferUsageFlag.INDEX_BUFFER);
		validate();
	}

	private void validate() {
		if(type == VkIndexType.NONE_NV) {
			throw new UnsupportedOperationException("Invalid index type: " + type);
		}

		if((type == VkIndexType.UINT16) && (type(this) == VkIndexType.UINT32)) {
			throw new IllegalArgumentException("Index is too large for short data type: " + this);
		}
	}

	/**
	 * Constructor.
	 * @param buffer Buffer
	 * @throws IllegalStateException if the given buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 */
	public IndexBuffer(VulkanBuffer buffer) {
		this(buffer, type(buffer));
	}

	/**
	 * Determines the index data type depending on the length of the given buffer.
	 * @param buffer Buffer
	 * @return Index data type
	 */
	private static VkIndexType type(VulkanBuffer buffer) {
		return BufferHelper.isShortIndex(buffer.length() / Short.BYTES) ? VkIndexType.UINT16 : VkIndexType.UINT32;
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
		final int max = this.device().limits().maxDrawIndexedIndexValue;		// TODO - cloned!!!

		// Ignore maximum unsigned integer value
		if(max == -1) {
			return;
		}

		// Validate size of this index
		final long count = this.length() / Integer.BYTES;
		if(count > max) {
			throw new IllegalStateException(String.format("Index too large: count=%d max=%d index=%s", count, max, this));
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
