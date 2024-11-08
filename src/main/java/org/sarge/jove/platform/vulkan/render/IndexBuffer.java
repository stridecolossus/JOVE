package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.model.IndexedMeshBuilder;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * An <i>index buffer</i> binds a drawing index to the pipeline.
 * <p>
 * Note that the index is represented as either {@code short} or {@code int} values depending on the length of the vertex data referred to, specified by {@link VkIndexType}.
 * <p>
 * @author Sarge
 */
public final class IndexBuffer extends VulkanBuffer {
	private final VkIndexType type;

	/**
	 * Constructor given a specific index data type.
	 * @param buffer		Buffer
	 * @param type			Index type
	 * @throws IllegalStateException if the {@link #buffer} cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 * @throws IllegalArgumentException if the given {@link #type} is invalid
	 */
	public IndexBuffer(VulkanBuffer buffer, VkIndexType type) {
		super(buffer);
		if(type == VkIndexType.NONE_NV) throw new IllegalArgumentException("Invalid index type: " + type);
		this.type = requireNonNull(type);
		require(VkBufferUsageFlag.INDEX_BUFFER);
	}

	/**
	 * Constructor that determines the index type for a given draw count.
	 * @param buffer		Buffer
	 * @param count			Index draw count
	 * @throws IllegalStateException if the given buffer cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 * @see IndexedMeshBuilder#isIntegerIndex(int)
	 */
	public IndexBuffer(VulkanBuffer buffer, int count) {
		this(buffer, IndexedMeshBuilder.isIntegerIndex(count) ? VkIndexType.UINT32 : VkIndexType.UINT16);
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
		checkOffset(offset);
		validateLimit();
		return (api, cmd) -> api.vkCmdBindIndexBuffer(cmd, this, offset, type);
	}

	/**
	 * @throws IllegalStateException if the index is larger than the hardware limit
	 */
	private void validateLimit() {
		// A short index is always supported
		if(type == VkIndexType.UINT16) {
			return;
		}

		// Lookup maximum index length
		final var limits = this.device().limits();
		final int max = limits.maxDrawIndexedIndexValue;

		// Ignore maximum unsigned integer value
		if(max == -1) {
			return;
		}

		// Validate size of this index
		final long count = this.length() / Integer.BYTES;
		if(count > max) {
			throw new IllegalStateException("Index too large: count=%d max=%d index=%s".formatted(count, max, this));
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
}
