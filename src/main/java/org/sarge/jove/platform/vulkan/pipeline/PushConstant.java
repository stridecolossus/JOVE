package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.io.BufferHelper;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>push constant</i> comprises the ranges and backing data buffer for the push constants of a pipeline.
 * <p>
 * Notes:
 * <ul>
 * <li>The offset and size of each {@link Range} must adhere to the alignment rules for push constants</li>
 * <li>The overall length of the push constant buffer is restricted by the hardware, see {@link PipelineLayout.Builder#build(LogicalDevice)}</li>
 * <li>The push constant ranges must cover the entire buffer</li>
 * </ul>
 * <p>
 * Example:
 * <p>
 * <pre>
 * // Create push constant
 * Range range = new Range(0, 4, Set.of(VkShaderStage.VERTEX));
 * var push = new PushConstant(List.of(range, ...));
 *
 * // Create command to update a range of the push constant
 * PipelineLayout layout = ...
 * Command cmd = push.update(range, layout);
 *
 * // Apply update in the render sequence
 * push.buffer().put(...);
 * cmd.execute(library, commandBuffer);
 * <p>
 * @see PipelineLayout
 * @author Sarge
 */
public final class PushConstant {
	/**
	 * Empty push constant.
	 */
	public static final PushConstant NONE = new PushConstant();

	private final ByteBuffer data;
	private final List<Range> ranges;

	/**
	 * Constructor.
	 * @param ranges Ranges of this push constant
	 * @throws IllegalArgumentException if any two elements of {@link #ranges} contains the same shader stage
	 * @throws IllegalArgumentException if any segment of the push constant is not covered by at least one range
	 */
	PushConstant(List<Range> ranges) {
   		final int len = ranges.stream().mapToInt(Range::max).reduce(0, Integer::max);
   		this.data = BufferHelper.allocate(len);
		this.ranges = List.copyOf(ranges);
   		validateStages();
   		validateCoverage();
	}

	/**
	 * Constructor for the empty push constant.
	 */
	private PushConstant() {
		this.data = null;
		this.ranges = List.of();
	}

	/**
	 * Checks that each range contains unique shader stages across this constant.
	 */
	private void validateStages() {
		final var used = new HashSet<>();
		for(Range r : ranges) {
			if(!Collections.disjoint(r.stages, used)) throw new IllegalArgumentException("Shader stages of each range must be unique");
			used.addAll(r.stages);
		}
	}

	/**
	 * Checks that the entire backing buffer is covered by the ranges of this constant.
	 */
	private void validateCoverage() {
		// Order ranges by increasing offset
		final List<Range> list = new ArrayList<>(ranges);
		Collections.sort(list, Comparator.comparingInt(Range::offset));

		// Check each range overlaps or is contiguous
		int prev = 0;
		for(Range r : list) {
			if(r.offset > prev) throw new IllegalArgumentException("Push constant buffer not covered by ranges");
			prev = r.max();
		}
		assert prev == length();
	}

	/**
	 * @return Ranges of this push constant
	 */
	public List<Range> ranges() {
		return ranges;
	}

	/**
	 * @return Overall length of the backing buffer for this push constant (bytes)
	 */
	public int length() {
		if(data == null) {
			return 0;
		}
		else {
			return data.capacity();
		}
	}

	/**
	 * @return Push constant data buffer
	 * @throws IllegalStateException if this push constant is empty
	 */
	public ByteBuffer buffer() {
		if(data == null) throw new IllegalStateException("Push constant is empty: " + this);
		return data;
	}

	/**
	 * Creates a command to update a range of this push constant.
	 * @param range			Range
	 * @param layout		Pipeline layout
	 * @return Update command
	 * @throws IllegalArgumentException if the given range is not a member of this push constant
	 * @throws IllegalArgumentException if this push constant does not belong to the given pipeline layout
	 */
	public Command update(Range range, PipelineLayout layout) {
		if(!ranges.contains(range)) throw new IllegalArgumentException("Invalid range for this push constant");
		if(layout.push() != this) throw new IllegalArgumentException("Invalid pipeline layout for this push constant");
		return new UpdateCommand(range, layout);
	}

	/**
	 * Convenience method to creates an update command for the whole of this push constant.
	 * @param layout Pipeline layout
	 * @return Update command
	 * @throws IllegalArgumentException if this push constant does not have exactly one range
	 */
	public Command update(PipelineLayout layout) {
		if(ranges.size() != 1) throw new IllegalArgumentException("Expected single push constant range");
		return update(ranges.get(0), layout);
	}

	@Override
	public int hashCode() {
		return ranges.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PushConstant that) &&
				this.ranges.equals(that.ranges());
	}

	/**
	 * A <i>push constant range</i> specifies a segment of this push constant.
	 * @see <A href="https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkPushConstantRange.html">Push Constants</a>
	 */
	public record Range(int offset, int size, Set<VkShaderStage> stages) {
		/**
		 * Constructor.
		 * @param offset		Offset (bytes)
		 * @param size			Size of this range (bytes)
		 * @param stages		Shader stages
		 * @throws IllegalArgumentException if {@link #offset} or {@link #size} do not satisfy the alignment rules for push constants
		 */
		public Range {
			requireZeroOrMore(offset);
			requireOneOrMore(size);
			VulkanLibrary.checkAlignment(offset);
			VulkanLibrary.checkAlignment(size);
			requireNotEmpty(stages);
			stages = Set.copyOf(stages);
		}

		/**
		 * @return Maximum size of the backing buffer to support this range
		 */
		int max() {
			return offset + size;
		}

		/**
		 * @return Descriptor
		 */
		VkPushConstantRange populate() {
			final var range = new VkPushConstantRange();
			range.stageFlags = new EnumMask<>(stages);
			range.size = size;
			range.offset = offset;
			return range;
		}
	}

	/**
	 * A <i>push constant update command</i> is used to the update a segment of this push constant.
	 */
	private final class UpdateCommand implements Command {
		private final Range range;
		private final PipelineLayout layout;
		private final EnumMask<VkShaderStage> stages;

		/**
		 * Constructor.
		 * @param range			Push constant range to update
		 * @param layout		Pipeline layout
		 */
		private UpdateCommand(Range range, PipelineLayout layout) {
			this.range = requireNonNull(range);
			this.layout = requireNonNull(layout);
			this.stages = new EnumMask<>(range.stages);
			assert ranges.contains(range);
		}

		@Override
		public void execute(VulkanLibrary lib, CommandBuffer buffer) {
			data.position(range.offset);
			lib.vkCmdPushConstants(buffer, layout, stages, range.offset, range.size, null); // TOOD data);
		}
	}
}
