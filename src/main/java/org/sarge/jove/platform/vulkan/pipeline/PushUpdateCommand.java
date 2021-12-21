package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout.PushConstantRange;
import org.sarge.jove.util.BufferHelper;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * A <i>push update command</i> is used to populate a segment of push constants.
 * @author Sarge
 */
public class PushUpdateCommand implements Command {
	/**
	 * Helper - Creates a data buffer for the whole of the push constants of the given pipeline layout.
	 * @param layout Pipeline layout
	 * @return New push constants data buffer
	 * @see PipelineLayout#max()
	 */
	public static ByteBuffer data(PipelineLayout layout) {
		return BufferHelper.allocate(layout.max());
	}

	/**
	 * Helper - Creates a push constants update command and backing data buffer for the given pipeline layout.
	 * @param layout Pipeline layout
	 * @return New update command
	 * @see #data(PipelineLayout)
	 */
	public static PushUpdateCommand of(PipelineLayout layout) {
		final ByteBuffer data = data(layout);
		return new PushUpdateCommand(layout, 0, data, layout.stages());
	}

	private final PipelineLayout layout;
	private final int offset;
	private final ByteBuffer data;
	private final int stages;

	/**
	 * Constructor.
	 * @param layout		Pipeline layout
	 * @param offset		Offset (bytes)
	 * @param data			Data buffer
	 * @param stages		Pipeline shader stage(s) that can access this updated data
	 * @throws IllegalArgumentException if {@link #offset} and size of the {@link #data} buffer exceed the {@link PipelineLayout#max()} length of the push constants
	 * @throws IllegalArgumentException if {@link #offset} and {@link #data} buffer are not correctly aligned
	 * @throws IllegalArgumentException if {@link #stages} is empty or is not a valid subset of the pipeline layout
	 */
	public PushUpdateCommand(PipelineLayout layout, int offset, ByteBuffer data, Set<VkShaderStage> stages) {
		this.layout = notNull(layout);
		this.offset = zeroOrMore(offset);
		this.data = notNull(data);
		this.stages = IntegerEnumeration.mask(stages);
		validate();
		Check.notEmpty(stages);
		if(!layout.stages().containsAll(stages)) {
			throw new IllegalArgumentException(String.format("Invalid pipeline stages for this layout: stages=%s layout=%s", stages, layout.stages()));
		}
	}

	private void validate() {
		// Check data buffer
		final int size = data.capacity();
		if(size == 0) throw new IllegalArgumentException("Buffer cannot be empty");
		if(offset + size > layout.max()) {
			throw new IllegalArgumentException(String.format("Buffer exceeds maximum push constants length: max=%s buffer=%s", layout.max(), data));
		}

		// Check alignment
		PushConstantRange.validate(offset);
		PushConstantRange.validate(size);
	}

	/**
	 * @return Data buffer for this update
	 */
	public ByteBuffer data() {
		return data;
	}

	@Override
	public void execute(VulkanLibrary lib, Buffer buffer) {
		data.rewind();
		lib.vkCmdPushConstants(buffer, layout, stages, offset, data.limit(), data);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PushUpdateCommand that) &&
				(this.layout == that.layout) &&
				(this.offset == that.offset) &&
				(this.stages == that.stages) &&
				(this.data.capacity() == that.data.capacity());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("offset", offset)
				.append("data", data)
				.append("stages", stages)
				.build();
	}

	/**
	 * Builder for a push constants update command.
	 * <p>
	 * Usage:
	 * <p>
	 * <pre>
	 * 	PipelineLayout layout = ...
	 * 	ByteBuffer buffer = ...
	 * 	PushUpdateCommand update = new Builder()
	 * 		.data(buffer)
	 * 		.stage(VkPipelineStage.FRAGMENT_SHADER)
	 * 		.build(layout);
	 * </pre>
	 * <p>
	 * This implementation provides overloaded variants of the {@link #data(ByteBuffer)} method to specify the backing data buffer of the update command:
	 * <ul>
	 * <li>{@link #data(ByteBuffer, int, int)} specifies an arbitrary <i>slice</i> of a buffer to be used for the command</li>
	 * <li>{@link #data(ByteBuffer, PushConstantRange)} slices a buffer for the corresponding push constants range</li>
	 * </ul>
	 * Note that the convenience {@link PushUpdateCommand#data()} factory method can be used to create a buffer for the push constants of a given layout.
	 */
	public static class Builder {
		private int offset;
		private ByteBuffer data;
		private final Set<VkShaderStage> stages = new HashSet<>();

		/**
		 * Sets the offset of this update.
		 * @param offset Offset (bytes)
		 */
		public Builder offset(int offset) {
			this.offset = zeroOrMore(offset);
			return this;
		}

		/**
		 * Sets the data buffer for this update.
		 * @param data Data buffer
		 */
		public Builder data(ByteBuffer data) {
			this.data = notNull(data);
			return this;
		}

		/**
		 * Helper - Sets the data buffer for this update as a <i>slice</i> of the given buffer.
		 * @param data 		Data buffer
		 * @param offset	Buffer offset
		 * @param size		Update size
		 * @see ByteBuffer#slice(int, int)
		 * @throws IndexOutOfBoundsException if the slice is invalid for the given data buffer
		 */
		public Builder data(ByteBuffer data, int offset, int size) {
			this.data = data.slice(offset, size);
			return this;
		}

		/**
		 * Helper - Sets the data buffer for this update as a <i>slice</i> corresponding to the given push constant range.
		 * @param data 		Data buffer
		 * @param range		Push constant range
		 * @see #data(ByteBuffer, int, int)
		 * @throws IndexOutOfBoundsException if the slice is invalid for the given push constant range
		 */
		public Builder data(ByteBuffer data, PushConstantRange range) {
			return data(data, range.offset(), range.size());
		}

		/**
		 * Adds a pipeline stage for this update.
		 * @param stage Pipeline shader stage
		 */
		public Builder stage(VkShaderStage stage) {
			stages.add(notNull(stage));
			return this;
		}

		/**
		 * Constructs this update command.
		 * @param layout Pipeline layout
		 * @return New update command
		 * @throws IllegalArgumentException if no data buffer has been specified for this update
		 * @see PushUpdateCommand#PushUpdateCommand(PipelineLayout, int, ByteBuffer, Set)
		 */
		public PushUpdateCommand build(PipelineLayout layout) {
			if(data == null) throw new IllegalArgumentException("No data buffer specified");
			return new PushUpdateCommand(layout, offset, data, stages);
		}
	}
}
