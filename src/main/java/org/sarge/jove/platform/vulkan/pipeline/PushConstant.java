package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.core.Vulkan.checkAlignment;
import static org.sarge.jove.util.Validation.*;

import java.lang.foreign.*;
import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>push constant</i> is an alternative to uniform buffers for transferring small amounts of data to a shader.
 * <p>
 * A push constant is comprised of a set of {@link Range} that specify one-or-more memory segments used by a pipeline shader stage.
 * The push constant has a <i>backing buffer</i> that can be populated via {@link #data()} or {@link #data(Range)}.
 * <p>
 * Push constant data is written to the pipeline <i>during</i> rendering by an {@link UpdateCommand}.
 * <p>
 * @author Sarge
 */
public class PushConstant {
    /**
     * A <i>push constant range</i> specifies a segment of the backing buffer of this push constant.
     */
	public record Range(int offset, int size, Set<VkShaderStageFlags> stages) {
    	/**
    	 * Constructor.
    	 * @param offset		Buffer offset
    	 * @param size			Size of this range (bytes)
    	 * @param stages		Pipeline stages
    	 * @throws IllegalArgumentException if {@link #stages} is empty
    	 */
    	public Range {
    		requireZeroOrMore(offset);
    		requireOneOrMore(size);
    		checkAlignment(offset);
    		checkAlignment(size);
    		requireNotEmpty(stages);
    		stages = Set.copyOf(stages);
    	}

    	/**
    	 * @return Length of this range
    	 */
    	public int length() {
    		return offset + size;
    	}

    	/**
    	 * @return Descriptor for this push constant
    	 */
    	VkPushConstantRange populate() {
    		final var range = new VkPushConstantRange();
    		range.stageFlags = new EnumMask<>(stages);
    		range.offset = offset;
    		range.size = size;
    		return range;
    	}
    }

	private final List<Range> ranges;
	private final MemorySegment data;

	/**
	 * Constructor.
	 * @param ranges			Push constant ranges
	 * @param allocator			Off-heap allocator for the backing buffer
	 * @throws IllegalArgumentException if {@link #ranges} does not cover the entire backing buffer or has overlapping pipeline stages
	 */
	public PushConstant(List<Range> ranges, SegmentAllocator allocator) {
		// Order ranges by offset
		this.ranges = ranges
    			.stream()
    			.sorted(Comparator.comparingInt(Range::offset))
    			.toList();

		// Validate ranges
		checkCoverage();
		checkStages();

		// Allocate backing buffer
		final Range last = this.ranges.getLast();
		final MemoryLayout layout = MemoryLayout.sequenceLayout(last.length(), ValueLayout.JAVA_BYTE);
		this.data = allocator.allocate(layout);
	}

    /**
     * Checks that the ranges cover the entire backing buffer.
     */
    private void checkCoverage() {
    	int offset = 0;
    	for(var range : ranges) {
    		if(range.offset > offset) {
    			throw new IllegalArgumentException("Unused segment before push constant %s at offset %d".formatted(range, offset));
    		}
    		offset = range.length();
    	}
    }

    /**
     * Checks that the shader stages are unique for each range.
     */
    private void checkStages() {
    	final Set<VkShaderStageFlags> stages = new HashSet<>();
    	for(var range : ranges) {
    		if(!Collections.disjoint(range.stages, stages)) {
    			throw new IllegalArgumentException("Overlapping shader stages: " + range);
    		}
    		stages.addAll(range.stages);
    	}
    }

    /**
     * @throws IllegalArgumentException if any range of this push constant exceeds the hardware limit
     */
	protected void validate(LogicalDevice device) {
		final int max = device.limits().get("maxPushConstantsSize");
		for(Range range : ranges) {
			if(range.size() > max) {
				throw new IllegalArgumentException("Push constant range %s is larger than device limit %d".formatted(range, max));
			}
		}
	}
	// TODO - introduce factory, including arena/allocator, logical device -> validation

	/**
	 * @return Ranges of this push constant
	 */
	public List<Range> ranges() {
		return ranges;
	}

	/**
	 * @return Mutable backing buffer for this push constant
	 */
	public MemorySegment data() {
		return data;
	}

	/**
	 * @param range Range of this push constant
	 * @return Segment of the backing buffer for the given push constant range
	 * @throws IllegalArgumentException if {@link #range} is not a member of this push constant
	 */
	public MemorySegment data(Range range) {
		if(!ranges.contains(range)) {
			throw new IllegalArgumentException();
		}
		return data.asSlice(range.offset, range.size);
	}

	/**
	 * Creates a command to update the given range of this push constant.
	 * @param range			Range
	 * @param layout		Pipeline layout
	 * @return Update command
	 * @throws IllegalArgumentException if this constant does not belong to the given layout
	 * @throws IllegalArgumentException if {@link #range} is not a member of this push constant
	 */
	public Command update(Range range, PipelineLayout layout) {
		if(!ranges.contains(range)) {
			throw new IllegalArgumentException("Invalid push constant range: " + range);
		}
		check(layout);

		return new UpdateCommand(range, layout, data);
	}

	/**
	 * Creates a command to update the whole of this push constant.
	 * @param layout Pipeline layout
	 * @return Update command
	 * @throws IllegalArgumentException if this constant does not belong to the given layout
	 */
	public Command update(PipelineLayout layout) {
		check(layout);

		// Short-cut for a single range
		if(ranges.size() == 1) {
			return update(ranges.getFirst(), layout);
		}

		// Enumerate shader stages across all ranges
		final Set<VkShaderStageFlags> stages = ranges
				.stream()
				.map(Range::stages)
				.flatMap(Set::stream)
				.distinct()
				.collect(toSet());

		// Build update command for the whole buffer
		final Range range = new Range(0, (int) data.byteSize(), stages);
		return new UpdateCommand(range, layout, data);
	}

	private void check(PipelineLayout layout) {
		if(layout.constant().filter(this::equals).isEmpty()) {
			throw new IllegalArgumentException("Invalid pipeline layout %s for constant %s".formatted(layout, this));
		}
	}

	/**
	 * Command to update a write a portion of the backing buffer of this push constant to the pipeline.
	 */
	private record UpdateCommand(Range range, PipelineLayout layout, MemorySegment data) implements Command {
		@Override
		public void execute(Buffer buffer) {
			final PipelineLayout.Library library = layout.device().library();
			final var stages = new EnumMask<>(range.stages);
			library.vkCmdPushConstants(buffer, layout, stages, range.offset, range.size, new Handle(data));
		}
	}
	// TODO - use MemorySegment directly rather than Handle?
}
